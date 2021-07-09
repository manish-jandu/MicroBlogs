package com.scaler.microblogs.di

import com.scaler.libconduit.apis.ConduitAuthApi
import com.scaler.microblogs.data.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    var authToken: String? = null

    @Provides
    @Singleton
    @Named("authInterceptor")
    fun providesAuthInterceptor() = Interceptor { chain ->
        var req = chain.request()
        MainModule.authToken?.let {
            req = req.newBuilder()
                .header("Authorization", "Token $it")
                .build()
        }
        chain.proceed(req)
    }

    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun providesAuthOkHttpClient(
        @Named("authInterceptor") interceptor: Interceptor
    ) = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @Provides
    @Singleton
    @Named("authRetrofit")
    fun providesAuthRetrofit(
        @Named("authOkHttpClient") okHttpClient: OkHttpClient
    ) = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://conduit.productionready.io/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun providesAuthApi(
        @Named("authRetrofit")
        retrofit: Retrofit
    ) =
        retrofit.create(ConduitAuthApi::class.java)

    @Provides
    @Singleton
    fun providesAuthRepository(
        api: ConduitAuthApi
    ) = AuthRepository(api)

}