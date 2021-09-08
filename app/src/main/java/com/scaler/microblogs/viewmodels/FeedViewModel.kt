package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentArticles = MutableLiveData<PagingData<Article>>()

    private val feedEventChannel = Channel<FeedEvent>()
    val feedEvent = feedEventChannel.receiveAsFlow()

    val globalArticles = repo.remote.getFeeds().cachedIn(viewModelScope)
    val feedArticles = repo.authRemote.getCurrentUseFeed().cachedIn(viewModelScope)

    fun updateCurrentUserStatus() {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _isLoggedIn.postValue(false)
        } else {
            AuthModule.authToken = token
            _isLoggedIn.postValue(true)
        }
    }

    sealed class FeedEvent {
        object LoggedIn : FeedEvent()
        object LoggedOut : FeedEvent()
    }
}