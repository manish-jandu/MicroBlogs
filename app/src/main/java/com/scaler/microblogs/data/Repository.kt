package com.scaler.microblogs.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.requests.UserLoginData
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupData
import com.scaler.libconduit.requests.UserSignupRequest
import com.scaler.microblogs.adapters.pagingadapters.FavouriteFeedPagingSource
import com.scaler.microblogs.adapters.pagingadapters.GlobalFeedPagingSource
import com.scaler.microblogs.adapters.pagingadapters.ProfileFeedPagingSource
import com.scaler.microblogs.adapters.pagingadapters.TagsFeedPagingSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val api: ConduitApi,
) {
    suspend fun getTags() = api.getTags()

    suspend fun signup(userName: String, email: String, password: String) =
        api.registerUser(UserSignupRequest(UserSignupData(email, password, userName)))

    suspend fun login(email: String, password: String) =
        api.loginUser(UserLoginRequest(UserLoginData(email, password)))

    suspend fun getArticleBySlug(slug: String) = api.getArticleBySlug(slug)

    suspend fun getCommentsBySlug(slug: String) = api.getComments(slug)

    suspend fun getProfileByUserName(userName: String) = api.getProfileByUserName(userName)

    fun getFeedByTag(tag: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            TagsFeedPagingSource(api, tag)
        }
    ).liveData

    fun getFeedByUserName(userName: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            ProfileFeedPagingSource(
                api,
                userName = userName,
            )
        }
    ).liveData

    fun getFeedByUserFavourite(userName: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            FavouriteFeedPagingSource(
                api,
                userName = userName,
            )
        }
    ).liveData

    fun getFeeds() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GlobalFeedPagingSource(
                    api,
                )
            }
        ).liveData
}