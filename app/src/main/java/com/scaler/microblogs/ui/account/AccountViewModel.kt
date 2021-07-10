package com.scaler.microblogs.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.User
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.AuthRepository
import com.scaler.microblogs.di.AuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

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

    fun signOut() {
        appPrefStorage.deleteToken()
        getUserToken()
    }

    fun getCurrentUser() = viewModelScope.launch {

        val result = authRepo.getCurrentUser()
        if (result.isSuccessful) {
            result.body()?.let {
                _currentUser.postValue(it.user!!)
            }
        } else {
            accountEventChannel.send(AccountEvent.ErrorLoadingData)
        }
    }

    sealed class AccountEvent {
        data class LoggedIn(val token: String) : AccountEvent()
        object LoggedOut : AccountEvent()
        object ErrorLoadingData : AccountEvent()
    }

}