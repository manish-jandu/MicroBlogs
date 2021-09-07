package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Profile
import com.scaler.libconduit.responses.ProfileResponse
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
class ProfileViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _profile = MutableLiveData<NetworkResult<Profile>>()
    val profile: LiveData<NetworkResult<Profile>> = _profile

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val profileEventChannel = Channel<ProfileEvent>()
    val profileEvent = profileEventChannel.receiveAsFlow()

    fun checkIfLoggedIn() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _isLoggedIn.postValue(false)
        } else {
            AuthModule.authToken = token
            _isLoggedIn.postValue(true)
        }
    }

    fun getProfileFromRepo(userName: String) = viewModelScope.launch(Dispatchers.IO) {
        _profile.postValue(NetworkResult.Loading())
        try {
            val result = repo.remote.getProfileByUserName(userName)
            _profile.postValue(handleProfileResponse(result))
        } catch (e: Exception) {
            _profile.postValue(NetworkResult.Error(e.message.toString()))
        }
    }

    fun getProfileFromAuthRepo(userName: String) = viewModelScope.launch {
        _profile.postValue(NetworkResult.Loading())
        try {
            val result = repo.authRemote.getProfileByUserName(userName)
            _profile.postValue(handleProfileResponse(result))
        } catch (e: Exception) {
            _profile.postValue(NetworkResult.Error(e.message.toString()))
        }
    }

    private fun handleProfileResponse(response: Response<ProfileResponse>): NetworkResult<Profile> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()!!.profile == null -> {
                NetworkResult.Error("User not Found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.profile!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    fun followUnfollowAccount(userName: String, isFollowing: Boolean) {
        if (isFollowing) {
            followAccount(userName)
        } else {
            unFollowAccount(userName)
        }
    }

    private fun followAccount(userName: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = repo.authRemote.followAccount(userName)
            handleFollowUnfollowResponse(result)
        } catch (e: Exception) {
            profileEventChannel.send(ProfileEvent.ErrorFollowUnFollow(e.message.toString()))
        }
    }

    private fun unFollowAccount(userName: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = repo.authRemote.unfollowAccount(userName)
            handleFollowUnfollowResponse(result)
        } catch (e: Exception) {
            profileEventChannel.send(ProfileEvent.ErrorFollowUnFollow(e.message.toString()))
        }
    }

    private suspend fun handleFollowUnfollowResponse(response: Response<ProfileResponse>) {
        when {
            response.message().contains("timeout") -> {
                profileEventChannel.send(ProfileEvent.ErrorFollowUnFollow("timeout"))
            }
            response.body()!!.profile == null -> {
                profileEventChannel.send(ProfileEvent.ErrorFollowUnFollow("something went wrong"))
            }
            response.isSuccessful -> {
                val result = response.body()!!.profile!!
                _profile.postValue(NetworkResult.Success(result))
            }
            else -> {
                profileEventChannel.send(ProfileEvent.ErrorFollowUnFollow(response.message()))
            }
        }
    }

    sealed class ProfileEvent {
        data class ErrorFollowUnFollow(val errorMessage: String) : ProfileEvent()
    }
}