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
import com.scaler.microblogs.utils.CurrentUserStatus
import com.scaler.microblogs.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage,
) : ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _currentUser = MutableLiveData<NetworkResult<User>>()
    val currentUser: LiveData<NetworkResult<User>> = _currentUser

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _currentUserStatus = MutableLiveData<CurrentUserStatus>()
    val currentUserStatus: LiveData<CurrentUserStatus> get() = _currentUserStatus

    fun updateCurrentUserStatus() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _currentUserStatus.postValue(CurrentUserStatus.LoggedOut)
        } else {
            AuthModule.authToken = token
            _currentUserStatus.postValue(CurrentUserStatus.LoggedIn)
        }
    }

    fun getCurrentUser() = viewModelScope.launch {
        _currentUser.postValue(NetworkResult.Loading())
        if (hasInternetConnection()) {
            try {
                val result = repo.authRemote.getCurrentUser()
                _currentUser.postValue(handleCurrentUserResponse(result))
            } catch (e: Exception) {
                _currentUser.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _currentUser.postValue(NetworkResult.Error("No Internet Connection."))
        }
    }

    fun getUserName() {
        _userName.postValue(appPrefStorage.getUserName())
    }

    fun signOut() {
        appPrefStorage.deleteToken()
        appPrefStorage.deleteUserName()
        _currentUserStatus.postValue(CurrentUserStatus.LoggedOut)
    }

    private fun handleCurrentUserResponse(response: Response<UserResponse>): NetworkResult<User> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()!!.user == null -> {
                NetworkResult.Error("User not Found")
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

    private fun hasInternetConnection(): Boolean {
        if (isInternetAvailable != null) {
            return isInternetAvailable as Boolean
        }
        return false
    }


}