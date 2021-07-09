package com.scaler.microblogs.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.User
import com.scaler.libconduit.responses.UserResponse
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val authEventChannel = Channel<AuthEvent>()
    val authEvent = authEventChannel.receiveAsFlow()

    fun login(email: String?, password: String?) {
        viewModelScope.launch {
            if (isEmailCorrect(email) && isPasswordInCorrectFormat(password)) {
                val response = repo.login(email!!, password!!)

                if (isLoginSignUpSuccessful(response)) {
                    response.body()?.let {
                        _user.postValue(it.user)
                    }

                }
            }
        }
    }

    fun signUp(userName: String?, email: String?, password: String?) =
        viewModelScope.launch {
            if (isEmailCorrect(email) && isUserNamePasswordCorrect(userName, password)) {
                val response = repo.signup(userName!!, email!!, password!!)

                if (isLoginSignUpSuccessful(response)) {
                    response.body()?.let {
                        _user.postValue(it.user)
                    }

                }
            }
        }

    private suspend fun isUserNamePasswordCorrect(userName: String?, password: String?): Boolean {
        return if (userName.isNullOrEmpty() || password.isNullOrEmpty()) {
            authEventChannel.send(AuthEvent.ErrorInUserNameAndPassword)
            false
        } else {
            true
        }
    }

    private suspend fun isPasswordInCorrectFormat(password: String?): Boolean {
        return if (password.isNullOrEmpty()) {
            authEventChannel.send(AuthEvent.ErrorInLoginPassword)
            false
        } else {
            true
        }
    }

    private suspend fun isEmailCorrect(email: String?): Boolean {
        return if (isEmailValid(email)) {
            true
        } else {
            authEventChannel.send(AuthEvent.ErrorInEmail)
            false
        }
    }

    private fun isEmailValid(email: String?): Boolean {
        return !email.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    private suspend fun isLoginSignUpSuccessful(result: Response<UserResponse>): Boolean {
        return if (result.isSuccessful) {
            true
        } else {
            authEventChannel.send(AuthEvent.ErrorInLoginOrSignUp)
            false
        }
    }

    fun setNewUserToken(token: String) {
        appPrefStorage.setUserToken(token)
    }


    sealed class AuthEvent {
        object ErrorInEmail : AuthEvent()
        object ErrorInUserNameAndPassword : AuthEvent()
        object ErrorInLoginOrSignUp : AuthEvent()
        object ErrorInLoginPassword : AuthEvent()
    }
}