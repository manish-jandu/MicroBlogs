package com.scaler.microblogs.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.scaler.libconduit.models.Article
import com.scaler.libconduit.models.Profile
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.AuthRepository
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val profileEventChannel = Channel<ProfileEvent>()
    val profileEvent = profileEventChannel.receiveAsFlow()

    lateinit var userArticles: LiveData<PagingData<Article>>
    lateinit var favouriteArticles: LiveData<PagingData<Article>>

    private fun getUserToken(): Boolean {
        val token = appPrefStorage.getUserToken()
        return if (token.isNullOrEmpty()) {
            false
        } else {
            AuthModule.authToken = token
            true
        }
    }


    fun getUser(userName: String) = viewModelScope.launch {
        getUserFavouriteArticle(userName)
        getUserArticles(userName)
        profileEventChannel.send(ProfileEvent.StartObserving)
    }

     fun getProfileFromRepo(userName: String) =viewModelScope.launch {
        val result = repo.getProfileByUserName(userName)
        if(result.isSuccessful){
            _profile.postValue(result.body()?.profile!!)
        }
    }

     fun getProfileFromAuthRepo(userName: String) =viewModelScope.launch {
        val result = authRepo.getProfileByUserName(userName)
        if(result.isSuccessful){
            _profile.postValue(result.body()?.profile!!)
        }
    }

    private fun getUserFavouriteArticle(userName: String) {
        favouriteArticles = repo.getFeedByUserFavourite(userName).cachedIn(viewModelScope)
    }

    private fun getUserArticles(userName: String) {
        userArticles = repo.getFeedByUserName(userName).cachedIn(viewModelScope)
    }

    fun checkIfLoggedIn() = viewModelScope.launch {
        if (getUserToken()) {
            val result = authRepo.getCurrentUser()
            if (result.isSuccessful) {
                _isLoggedIn.postValue(true)
            } else {
                _isLoggedIn.postValue(false)
            }
        } else {
            _isLoggedIn.postValue(false)
        }
    }

    fun followUnfollowAccount(userName: String,isFollowing:Boolean) =viewModelScope.launch{
        if(isFollowing){
            val result = authRepo.unfollowAccount(userName)
            if(result.isSuccessful){
                _profile.postValue(result.body()?.profile!!)
            }
        }else{
            val result = authRepo.followAccount(userName)
            if(result.isSuccessful){
                _profile.postValue(result.body()?.profile!!)
            }
        }
    }

    sealed class ProfileEvent {
        object StartObserving : ProfileEvent()
        object LoggedOut : ProfileEvent()

    }
}