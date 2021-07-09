package com.scaler.libconduit.apis

import com.scaler.libconduit.requests.CreateArticleRequest
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupRequest
import com.scaler.libconduit.requests.UserUpdateRequest
import com.scaler.libconduit.responses.*
import retrofit2.http.*

interface ConduitApi {

    @POST("users/login")
    suspend fun loginUser(@Body userLoginRequest: UserLoginRequest ): UserResponse

    @POST("users")
    suspend fun registerUser(@Body userSignupRequest: UserSignupRequest): UserResponse

    @GET("articles")
    suspend fun getArticles(
        @Query("limit") limit:Int
    ): MultipleArticleResponse

    @GET("articles/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String
    ): SingleArticleResponse

    @GET("tags")
    suspend fun getTags(): TagsResponse

    @GET("articles")
    suspend fun getFeedArticlesByTag(
        @Query("limit") limit:Int,
        @Query("tag") tag:String,
    ): MultipleArticleResponse
}