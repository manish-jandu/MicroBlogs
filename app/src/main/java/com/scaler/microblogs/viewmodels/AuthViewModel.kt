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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _user = MutableLiveData<NetworkResult<User>>()
    val user: LiveData<NetworkResult<User>> = _user

    private val authEventChannel = Channel<AuthEvent>()
    val authEvent = authEventChannel.receiveAsFlow()

    fun login(email: String?, password: String?) = viewModelScope.launch(Dispatchers.IO) {
        _user.postValue(NetworkResult.Loading())
        if (hasInternetConnection()) {
            try {
                if (checkEmailFormat(email) && checkPasswordFormat(password)) {
                    val response = repo.remote.login(email!!, password!!)
                    _user.postValue(handleLoginResponse(response))
                }
            } catch (e: Exception) {
                _user.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _user.postValue(NetworkResult.Error("No Internet Connection."))
        }
    }

    private fun handleLoginResponse(response: Response<UserResponse>): NetworkResult<User> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.code() == 422 -> {
                NetworkResult.Error("Email or Password is wrong.")
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

    private fun checkPasswordFormat(password: String?): Boolean {
        return if (password.isNullOrEmpty()) {
            _user.postValue(NetworkResult.Error("Password cannot be empty."))
            false
        } else {
            true
        }
    }

    private fun checkEmailFormat(email: String?): Boolean {
        return if (email != null && email.isNotEmpty()) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                true
            } else {
                _user.postValue(NetworkResult.Error("Email is not in correct format."))
                false
            }
        } else {
            _user.postValue(NetworkResult.Error("Email cannot be empty."))
            false
        }
    }

    fun setUserDataInPreferences(user: User) = viewModelScope.launch(Dispatchers.IO) {
        AuthModule.authToken = user.token
        appPrefStorage.setUserToken(user.token!!)
        appPrefStorage.setUserName(user.username!!)
        authEventChannel.send(AuthEvent.SetUserDataSuccess)
    }

    private fun hasInternetConnection(): Boolean {
        if (isInternetAvailable != null) {
            return isInternetAvailable as Boolean
        }
        return false
    }

    sealed class AuthEvent {
        object SetUserDataSuccess : AuthEvent()
    }
}