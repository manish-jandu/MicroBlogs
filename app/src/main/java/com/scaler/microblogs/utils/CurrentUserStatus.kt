package com.scaler.microblogs.utils

sealed class CurrentUserStatus {
    object LoggedIn:CurrentUserStatus()
    object LoggedOut:CurrentUserStatus()
}