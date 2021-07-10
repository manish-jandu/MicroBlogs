package com.scaler.microblogs.data

import com.scaler.libconduit.requests.UserUpdateData
import com.scaler.libconduit.requests.UserUpdateRequest
import com.scaler.microblogs.di.AuthModule


class AuthRepository() {
    private val api = AuthModule.authApi

    suspend fun getCurrentUser() = api.getCurrentUser()

    suspend fun updateUserDetails(
        username: String,
        password: String,
        email: String,
        imageUrl: String?,
        bio: String?
    ) = api.updateUserDetails(
        UserUpdateRequest(UserUpdateData(bio, email, imageUrl, password, username))
    )


}