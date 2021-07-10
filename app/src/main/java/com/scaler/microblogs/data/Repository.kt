package com.scaler.microblogs.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.requests.UserLoginData
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupData
import com.scaler.libconduit.requests.UserSignupRequest
import com.scaler.microblogs.adapters.FeedPagingSource
import com.scaler.microblogs.di.AuthModule
import com.scaler.microblogs.utils.FeedType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val api: ConduitApi,
) {
    val authApi = AuthModule.authApi

    fun getFeeds() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FeedPagingSource(api, authApi,feedType = FeedType.GLOBAL_FEED) }
        ).liveData

    suspend fun getTags() = api.getTags()

    fun getFeedByTag(tag: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { FeedPagingSource(api, authApi, tag,feedType = FeedType.TAG_FEED) }
    ).liveData

    fun getCurrentUseFeed() = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { FeedPagingSource(api, authApi, feedType = FeedType.CURRENT_USER_FEED) }
    ).liveData


    suspend fun signup(userName: String, email: String, password: String) =
        api.registerUser(UserSignupRequest(UserSignupData(email, password, userName)))

    suspend fun login(email: String, password: String) =
        api.loginUser(UserLoginRequest(UserLoginData(email, password)))


}