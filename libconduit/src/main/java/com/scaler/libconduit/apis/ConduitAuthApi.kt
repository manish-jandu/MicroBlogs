package com.scaler.libconduit.apis

import com.scaler.libconduit.models.Article
import com.scaler.libconduit.models.Comment
import com.scaler.libconduit.requests.CreateArticleRequest
import com.scaler.libconduit.requests.CreateComment
import com.scaler.libconduit.requests.UserUpdateRequest
import com.scaler.libconduit.responses.MultipleArticleResponse
import com.scaler.libconduit.responses.ProfileResponse
import com.scaler.libconduit.responses.SingleArticleResponse
import com.scaler.libconduit.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ConduitAuthApi {

    @GET("user")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("user")
    suspend fun updateUserDetails(@Body userUpdateRequest: UserUpdateRequest): Response<UserResponse>

    @GET("profiles/{username}")
    suspend fun getProfileByUsername(@Path("username") username: String): Response<ProfileResponse>

    @POST("profiles/{username}/follow")
    suspend fun followUser(@Path("username") username: String): Response<ProfileResponse>

    @DELETE("profiles/{username}/follow")
    suspend fun unfollowUser(@Path("username") username: String): Response<ProfileResponse>

    @GET("articles/feed")
    suspend fun getFeedArticles(
        @Query("limit") limit: Int
    ): Response<MultipleArticleResponse>

    @POST("articles")
    suspend fun createArticle(
        @Body createArticleRequest: CreateArticleRequest
    ): Response<SingleArticleResponse>

    @PUT("articles/{slug}")
    suspend fun updateArticle(
        @Path("slug") slug: String,
        @Body createArticleRequest: CreateArticleRequest
    ): Response<SingleArticleResponse>

    @DELETE("articles/{slug}")
    suspend fun deleteArticle(
        @Path("slug") slug: String,
    ): Response<Void>



    @POST("articles/{slug}/comments")
    suspend fun createComment(
        @Path("slug") slug: String,
        @Body createComment: CreateComment
    ): Response<Comment>


    @POST("articles/{slug}/favorite")
    suspend fun likeUnlikeArticle(
        @Path("slug") slug: String,
    ): Response<Article>
}