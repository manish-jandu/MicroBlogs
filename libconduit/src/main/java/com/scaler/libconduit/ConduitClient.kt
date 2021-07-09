package com.scaler.libconduit

import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.apis.ConduitAuthApi
import okhttp3.Interceptor
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ConduitClient {
    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        var req = chain.request()
        authToken?.let {
            req = req.newBuilder()
                .header("Authorization", "Token $it")
                .build()
        }
        chain.proceed(req)
    }

    val okHttpBuilder= OkHttpClient.Builder()


    val retrofitBuilder = Retrofit.Builder()
        .baseUrl("https://conduit.productionready.io/api/")
        .addConverterFactory(MoshiConverterFactory.create())

    val publicApi = retrofitBuilder
        .client(okHttpBuilder.build())
        .build()
        .create(ConduitApi::class.java)

    val authApi = retrofitBuilder
        .client(okHttpBuilder.addInterceptor(authInterceptor).build())
        .build()
        .create(ConduitAuthApi::class.java)

}