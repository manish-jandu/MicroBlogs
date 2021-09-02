package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.scaler.libconduit.models.User
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import com.scaler.microblogs.utils.CurrentUserStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _currentUserStatus = MutableLiveData<CurrentUserStatus>()
    val currentUserStatus: LiveData<CurrentUserStatus> get() = _currentUserStatus

    private val accountEventChannel = Channel<AccountEvent>()
    val accountEvent = accountEventChannel.receiveAsFlow()

    fun getUserToken() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _currentUserStatus.postValue(CurrentUserStatus.LoggedOut)
        } else {
            AuthModule.authToken = token
            _currentUserStatus.postValue(CurrentUserStatus.LoggedIn)
        }
    }

    fun getCurrentUser() = viewModelScope.launch {
        val result = repo.authRemote.getCurrentUser()
        if (result.isSuccessful) {
            result.body()?.let {
                _currentUser.postValue(it.user!!)
            }
        } else {
            accountEventChannel.send(AccountEvent.ErrorLoadingData)
        }
    }

    fun getUserName()  {
        _userName.postValue(appPrefStorage.getUserName())
    }

    fun getUserFavouriteArticle(userName: String) = repo.remote.getFeedByUserFavourite(userName).cachedIn(viewModelScope)

    fun getUserArticles(userName: String) = repo.remote.getFeedByUserName(userName).cachedIn(viewModelScope)

    fun signOut() {
        appPrefStorage.deleteToken()
        appPrefStorage.deleteUserName()
    }

    sealed class AccountEvent {
        object ErrorLoadingData : AccountEvent()
    }

}