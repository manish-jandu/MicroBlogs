package com.scaler.microblogs

import com.scaler.libconduit.ConduitClient
import com.scaler.libconduit.requests.UserLoginData
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupData
import com.scaler.libconduit.requests.UserSignupRequest
import com.scaler.microblogs.di.AuthModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val api = ConduitClient.publicApi
    private val authApi = AuthModule.authApi
    var token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTgyMjA2LCJ1c2VybmFtZSI6InRlc3Q5OTk2IiwiZXhwIjoxNjMxMDYzNjEwfQ.kViDrFAHrqJMLbCHE0DWeS2p4Nv_sBETLb04MGy6HbI"


    val email: String = "test9996@test.com"
    val password: String = "test9996"
    val userName: String = "test9996"


    @Test
    fun getTags() = runBlocking {
        val response = api.getTags()
        assertNotNull(response)
    }

    @Test
    fun getArticles() = runBlocking {
        val response = api.getArticles(30)
        assertNotNull(response)
    }

    @Test
    fun signup() = runBlocking {
        val response =
            api.registerUser(UserSignupRequest(UserSignupData(email, password, userName)))
        print(response)
        print(response.code())
        response.raw()
        response.message()
        assertNotNull(response.isSuccessful)
    }

    @Test
    fun login() = runBlocking {
        val response = api.loginUser(UserLoginRequest(UserLoginData(email, password)))
        assertNotNull(response)
    }

    @Test
    fun getFeedArticles() = runBlocking {
        val response = authApi.getCurrentUser()
        assertNotNull(response.body())
    }

}