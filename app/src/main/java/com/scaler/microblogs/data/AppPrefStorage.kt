package com.scaler.microblogs.data

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

val PRIVATE_MODE = 0
val PREF_NAME = "user_token_key"

@Singleton
class AppPrefStorage @Inject constructor(@ApplicationContext context: Context) {
    val sharedPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)


    fun getUserToken(): String? {
        return sharedPref.getString(PREF_NAME, null)
    }

    @SuppressLint("CommitPrefEdits")
    fun setUserToken(token: String) {
        sharedPref.edit().putString(PREF_NAME, token).apply()
    }

}