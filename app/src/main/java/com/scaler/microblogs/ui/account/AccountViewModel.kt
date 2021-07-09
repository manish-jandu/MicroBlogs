package com.scaler.microblogs.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _userToken = MutableLiveData<String>()
    val userToken: LiveData<String> = _userToken

    fun getUserToken() {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {

        } else {
            _userToken.postValue(token!!)

        }
    }



}