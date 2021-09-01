package com.scaler.microblogs.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.scaler.libconduit.models.Article
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
class FeedViewModel @Inject constructor(
    private val repo: Repository, private val authRepo: AuthRepository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _currentArticles = MutableLiveData<PagingData<Article>>()

    private val feedEventChannel = Channel<FeedEvent>()
    val feedEvent = feedEventChannel.receiveAsFlow()

    val globalArticles = repo.getFeeds().cachedIn(viewModelScope)
    val feedArticles = authRepo.getCurrentUseFeed().cachedIn(viewModelScope)

    fun getUserToken() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            feedEventChannel.send(FeedEvent.LoggedOut)
        } else {
            AuthModule.authToken = token
            feedEventChannel.send(FeedEvent.LoggedIn)
        }
    }

    sealed class FeedEvent {
        object LoggedIn  : FeedEvent()
        object LoggedOut : FeedEvent()
    }
}