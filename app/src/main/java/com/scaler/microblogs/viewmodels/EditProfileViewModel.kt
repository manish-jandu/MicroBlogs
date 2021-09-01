package com.scaler.microblogs.viewmodels

import android.util.Log
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

private const val TAG = "EditProfileViewModel"

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val editProfileEventChannel = Channel<EditProfileEvent>()
    val editProfileEvent = editProfileEventChannel.receiveAsFlow()

    fun getUserToken() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            editProfileEventChannel.send(EditProfileEvent.ErrorLoadingData)
        } else {
            AuthModule.authToken = token
            getCurrentUser()
        }
    }

    fun getCurrentUser() = viewModelScope.launch {
        val result = authRepo.getCurrentUser()
        if (result.isSuccessful) {
            result.body()?.let {
                _currentUser.postValue(it.user!!)
            }
        } else {
            editProfileEventChannel.send(EditProfileEvent.ErrorLoadingData)
        }
    }

    fun updateUserData(
        username: String?,
        bio: String?,
        imageUrl: String?,
        email: String?,
        password: String?
    ) = viewModelScope.launch {
        try{
            if (isEmailCorrect(email) && isUserNamePasswordCorrect(username, password)) {
                val response =
                    authRepo.updateUserDetails(username!!, password!!, email!!, imageUrl, bio)

                if (response.isSuccessful) {
                    editProfileEventChannel.send(EditProfileEvent.SuccessFullyUpdatedData)
                }else{
                    editProfileEventChannel.send(EditProfileEvent.ErrorInUpdatingData)
                }
            }
        }catch (e:Exception){
            Log.i(TAG, "updateUserData:e $e ")
        }

    }


    private suspend fun isUserNamePasswordCorrect(userName: String?, password: String?): Boolean {
        return if (userName.isNullOrEmpty() || password.isNullOrEmpty()) {
            editProfileEventChannel.send(EditProfileEvent.ErrorInUserNameAndPassword)
            false
        } else {
            true
        }
    }


    private suspend fun isEmailCorrect(email: String?): Boolean {
        return if (isEmailValid(email)) {
            true
        } else {
            editProfileEventChannel.send(EditProfileEvent.ErrorInEmail)
            false
        }
    }

    private fun isEmailValid(email: String?): Boolean {
        return !email.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    sealed class EditProfileEvent {
        object ErrorLoadingData : EditProfileEvent()
        object ErrorInEmail : EditProfileEvent()
        object ErrorInUserNameAndPassword : EditProfileEvent()
        object SuccessFullyUpdatedData : EditProfileEvent()
        object ErrorInUpdatingData : EditProfileEvent()
    }

}