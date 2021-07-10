package com.scaler.microblogs.data

import com.scaler.microblogs.di.AuthModule


class AuthRepository()  {
    private val api = AuthModule.authApi

    suspend fun getCurrentUser() = api.getCurrentUser()

}