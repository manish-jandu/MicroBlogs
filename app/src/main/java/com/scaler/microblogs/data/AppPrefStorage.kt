package com.scaler.microblogs.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppPrefStorage @Inject constructor(@ApplicationContext context: Context) {
    private val PRIVATE_MODE = 0
    private val PREF_NAME = "user_token_key"
    private val USER_NAME = "user_name"
    private val sharedPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    fun getUserToken(): String? {
        return sharedPref.getString(PREF_NAME, null)
    }

    fun getUserName():String?{
        return sharedPref.getString(USER_NAME,null)
    }

    fun setUserToken(token: String) {
        sharedPref.edit().putString(PREF_NAME, token).apply()
    }

    fun setUserName(userName:String){
        sharedPref.edit().putString(USER_NAME,userName).apply()
    }

    fun deleteToken(){
        sharedPref.edit().remove(PREF_NAME).apply()
    }

    fun deleteUserName(){
        sharedPref.edit().remove(USER_NAME).apply()
    }
}