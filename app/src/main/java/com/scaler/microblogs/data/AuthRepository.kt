package com.scaler.microblogs.data

import com.scaler.libconduit.requests.*
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

    suspend fun createArticle(
        title: String, description: String, body: String, tagList: List<String>
    ) = api.createArticle(CreateArticleRequest(ArticleData(body, description, tagList, title)))

    suspend fun deleteArticle(slug: String) {
        api.deleteArticle(slug)
    }

    suspend fun createComment(slug: String, body: String) =
        api.createComment(slug, CreateComment(body))

    suspend fun likeArticle(slug: String) = api.likeArticle(slug)

    suspend fun unlikeArticle(slug: String) = api.unlikeArticle(slug)

    suspend fun getProfileByUserName(username: String) = api.getProfileByUsername(username)

    suspend fun followAccount(userName: String) = api.followUser(userName)

    suspend fun unfollowAccount(userName: String) = api.unfollowUser(userName)

    suspend fun getArticleBySlug(slug: String)= api.getArticleBySlug(slug)

}
