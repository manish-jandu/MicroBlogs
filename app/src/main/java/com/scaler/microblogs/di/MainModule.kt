package com.scaler.microblogs.di


import com.scaler.libconduit.apis.ConduitApi
import com.scaler.microblogs.data.AuthRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    @Named("publicOkHttpClient")
    fun providesPublicOkHttpClient() = OkHttpClient.Builder()
        .build()

    @Provides
    @Singleton
    @Named("publicRetrofit")
    fun providesPublicRetrofit(
        @Named("publicOkHttpClient")
        okHttpClient: OkHttpClient
    ) = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://conduit.productionready.io/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun providesApi(
        @Named("publicRetrofit")
        retrofit: Retrofit
    ) =
        retrofit.create(ConduitApi::class.java)

    @Provides
    fun providesAuthRemoteDataSource() = AuthRemoteDataSource()


 }