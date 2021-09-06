package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.User
import com.scaler.libconduit.responses.UserResponse
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import com.scaler.microblogs.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "EditProfileViewModel"

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _currentUser = MutableLiveData<NetworkResult<User>>()
    val currentUser: LiveData<NetworkResult<User>> = _currentUser

    private val _updatedUser = MutableLiveData<NetworkResult<User>>()
    val updatedUser: LiveData<NetworkResult<User>> = _updatedUser

    private val editProfileEventChannel = Channel<EditProfileEvent>()
    val editProfileEvent = editProfileEventChannel.receiveAsFlow()

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            _currentUser.postValue(NetworkResult.Loading())
            try {
                val result = repo.authRemote.getCurrentUser()
                _currentUser.postValue(handleCurrentUserResponse(result))
            } catch (e: Exception) {
                _currentUser.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _currentUser.postValue(NetworkResult.Error("No Internet Connection"))
        }
    }

    fun updateUserData(
        username: String?, bio: String?, imageUrl: String?, email: String?, password: String?
    ) = viewModelScope.launch {
        _updatedUser.postValue(NetworkResult.Loading())
        try {
            if (checkUserNameFormat(username) && checkEmailFormat(email)
                && checkPasswordFormat(password)) {
                val result = repo.authRemote.updateUserDetails(username!!, password!!, email!!, imageUrl, bio)
                _updatedUser.postValue(handleUpdateUserData(result))
            }
        } catch (e: Exception) {
            _updatedUser.postValue(NetworkResult.Error(e.message.toString()))
        }
    }

    private fun handleCurrentUserResponse(response: Response<UserResponse>): NetworkResult<User> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()?.user == null -> {
                NetworkResult.Error("User not found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.user!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleUpdateUserData(response: Response<UserResponse>): NetworkResult<User> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.code() == 422 -> {
                NetworkResult.Error("username or email already in use.")
            }
            response.body()?.user == null -> {
                NetworkResult.Error("User not found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.user!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    private fun checkUserNameFormat(userName: String?): Boolean {
        return if (userName.isNullOrEmpty()) {
            _updatedUser.postValue(NetworkResult.Error("User Name cannot be empty."))
            false
        } else {
            true
        }
    }

    private fun checkPasswordFormat(password: String?): Boolean {
        return when {
            password.isNullOrEmpty() -> {
                _updatedUser.postValue(NetworkResult.Error("Password cannot be empty."))
                false
            }
            password.length < 8 -> {
                _updatedUser.postValue(NetworkResult.Error("Password should be min. 8 char."))
                false
            }
            else -> {
                true
            }
        }
    }

    private fun checkEmailFormat(email: String?): Boolean {
        return if (email != null && email.isNotEmpty()) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                true
            } else {
                _updatedUser.postValue(NetworkResult.Error("Email is not in correct format."))
                false
            }
        } else {
            _updatedUser.postValue(NetworkResult.Error("Email cannot be empty."))
            false
        }
    }

    fun setUserDataInPreferences(user: User) = viewModelScope.launch(Dispatchers.IO) {
        AuthModule.authToken = user.token
        appPrefStorage.setUserToken(user.token!!)
        appPrefStorage.setUserName(user.username!!)
        editProfileEventChannel.send(EditProfileEvent.Success)
    }

    private fun hasInternetConnection(): Boolean {
        if (isInternetAvailable != null) {
            return isInternetAvailable as Boolean
        }
        return false
    }

    sealed class EditProfileEvent {
        object Success : EditProfileEvent()
    }
}