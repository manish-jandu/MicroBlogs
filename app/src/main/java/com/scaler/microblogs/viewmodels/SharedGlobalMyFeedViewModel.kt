package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedGlobalMyFeedViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun updateCurrentUserStatus() {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _isLoggedIn.postValue(false)
        } else {
            AuthModule.authToken = token
            _isLoggedIn.postValue(true)
        }
    }

    fun getGlobalFeed() = repo.remote.getFeeds().cachedIn(viewModelScope)

    fun getMyFeed() = repo.authRemote.getCurrentUseFeed().cachedIn(viewModelScope)
}