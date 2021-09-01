package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.scaler.libconduit.models.Article
import com.scaler.libconduit.models.User
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
class AccountViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    lateinit var userArticles: LiveData<PagingData<Article>>
    lateinit var favouriteArticles: LiveData<PagingData<Article>>

    private val accountEventChannel = Channel<AccountEvent>()
    val accountEvent = accountEventChannel.receiveAsFlow()

    fun getUserToken() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            accountEventChannel.send(AccountEvent.LoggedOut)
        } else {
            accountEventChannel.send(AccountEvent.LoggedIn(token))
            AuthModule.authToken = token

            getCurrentUser()
        }
    }

    private fun getUserFavouriteArticle(userName: String) {
        favouriteArticles = repo.getFeedByUserFavourite(userName).cachedIn(viewModelScope)
    }

    private fun getUserArticles(userName: String) {
        userArticles = repo.getFeedByUserName(userName).cachedIn(viewModelScope)
    }

    fun signOut() {
        appPrefStorage.deleteToken()
        getUserToken()
    }

    private fun getCurrentUser() = viewModelScope.launch {

        val result = authRepo.getCurrentUser()
        if (result.isSuccessful) {
            result.body()?.let {
                _currentUser.postValue(it.user!!)
                //get article both favourite and created
                getUserArticles(it.user!!.username!!)
                getUserFavouriteArticle(it.user!!.username!!)
                accountEventChannel.send(AccountEvent.GotUser)
            }
        } else {
            accountEventChannel.send(AccountEvent.ErrorLoadingData)
        }
    }

    sealed class AccountEvent {
        data class LoggedIn(val token: String) : AccountEvent()
        object LoggedOut : AccountEvent()
        object ErrorLoadingData : AccountEvent()
        object GotUser : AccountEvent()
    }

}