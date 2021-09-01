package com.scaler.microblogs.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.scaler.libconduit.requests.*
import com.scaler.microblogs.adapters.pagingadapters.CurrentUserFeedPagingSource
import com.scaler.microblogs.di.AuthModule

class AuthRemoteDataSource() {
    private val authApi = AuthModule.authApi

    suspend fun getCurrentUser() = authApi.getCurrentUser()

    suspend fun updateUserDetails(
        username: String,
        password: String,
        email: String,
        imageUrl: String?,
        bio: String?
    ) = authApi.updateUserDetails(
        UserUpdateRequest(UserUpdateData(bio, email, imageUrl, password, username))
    )

    suspend fun createArticle(
        title: String, description: String, body: String, tagList: List<String>
    ) = authApi.createArticle(CreateArticleRequest(ArticleData(body, description, tagList, title)))

    suspend fun deleteArticle(slug: String) {
        authApi.deleteArticle(slug)
    }

    suspend fun createComment(slug: String, body: String) =
        authApi.createComment(slug, CreateComment(body))

    suspend fun likeArticle(slug: String) = authApi.likeArticle(slug)

    suspend fun unlikeArticle(slug: String) = authApi.unlikeArticle(slug)

    suspend fun getProfileByUserName(username: String) = authApi.getProfileByUsername(username)

    suspend fun followAccount(userName: String) = authApi.followUser(userName)

    suspend fun unfollowAccount(userName: String) = authApi.unfollowUser(userName)

    suspend fun getArticleBySlug(slug: String)= authApi.getArticleBySlug(slug)

    fun getCurrentUseFeed() = Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            CurrentUserFeedPagingSource(
                authApi,
            )
        }
    ).liveData
}
