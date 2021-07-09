package com.scaler.libconduit.apis

import com.scaler.libconduit.requests.CreateArticleRequest
import com.scaler.libconduit.requests.UserUpdateRequest
import com.scaler.libconduit.responses.MultipleArticleResponse
import com.scaler.libconduit.responses.ProfileResponse
import com.scaler.libconduit.responses.SingleArticleResponse
import com.scaler.libconduit.responses.UserResponse
import retrofit2.http.*

interface ConduitAuthApi {

    @GET("user")
    suspend fun getCurrentUser(): UserResponse

    @PUT("user")
    suspend fun updateUserDetails(@Body userUpdateRequest: UserUpdateRequest): UserResponse

    @GET("profiles/{username}")
    suspend fun getProfileByUsername(@Path("username") username: String): ProfileResponse

    @POST("profiles/{username}/follow")
    suspend fun followUser(@Path("username") username: String): ProfileResponse

    @DELETE("profiles/{username}/follow")
    suspend fun unfollowUser(@Path("username") username: String): ProfileResponse

    @GET("articles/feed")
    suspend fun getFeedArticles(
        @Query("limit") limit: Int
    ): MultipleArticleResponse

    @POST("articles")
    suspend fun createArticle(
        @Body createArticleRequest: CreateArticleRequest
    ): SingleArticleResponse

    @PUT("articles/{slug}")
    suspend fun updateArticle(
        @Path("slug") slug: String,
        @Body createArticleRequest: CreateArticleRequest
    ): SingleArticleResponse

    @DELETE("articles/{slug}")
    suspend fun deleteArticle(
        @Path("slug") slug: String,
    ): Void

}