package com.scaler.libconduit.apis

import com.scaler.libconduit.requests.CreateArticleRequest
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupRequest
import com.scaler.libconduit.requests.UserUpdateRequest
import com.scaler.libconduit.responses.*
import retrofit2.Response
import retrofit2.http.*

interface ConduitApi {

    @POST("users/login")
    suspend fun loginUser(@Body userLoginRequest: UserLoginRequest ):Response<UserResponse>

    @POST("users")
    suspend fun registerUser(@Body userSignupRequest: UserSignupRequest): Response<UserResponse>

    @GET("articles")
    suspend fun getArticles(
        @Query("limit") limit:Int
    ): Response<MultipleArticleResponse>

    @GET("articles/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String
    ): Response<SingleArticleResponse>

    @GET("tags")
    suspend fun getTags(): Response<TagsResponse>

    @GET("articles")
    suspend fun getFeedArticlesByTag(
        @Query("limit") limit:Int,
        @Query("tag") tag:String,
    ): Response<MultipleArticleResponse>
}