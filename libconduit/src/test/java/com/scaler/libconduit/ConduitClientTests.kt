package com.scaler.libconduit

import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.apis.ConduitAuthApi
import com.scaler.libconduit.requests.UserLoginData
import com.scaler.libconduit.requests.UserLoginRequest
import com.scaler.libconduit.requests.UserSignupData
import com.scaler.libconduit.requests.UserSignupRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class ConduitClientTests {

    private val api: ConduitApi = ConduitClient.publicApi
    private val authApi: ConduitAuthApi = ConduitClient.authApi
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MTgyMTg4LCJ1c2VybmFtZSI6InRlc3Q5OTk3IiwiZXhwIjoxNjMxMDEwODM0fQ.93GW0LnrJ5ADIk31UPHQrWJr4NATUGHFx-w76J4fjlo"

    val email:String = "test9997@test.com"
    val password:String = "test9997@test.com"
    val userName :String = "test9997"

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
        val response = api.registerUser(UserSignupRequest(UserSignupData(email,password,userName)))
        print(response)
        assertNotNull(response)
    }

    @Test
    fun login() = runBlocking {
        val response = api.loginUser(UserLoginRequest(UserLoginData(email,password)))
        ConduitClient.authToken = response.user!!.token
        assertNotNull(response)
    }

    @Test
    fun getFeedArticles() = runBlocking {
        ConduitClient.authToken = token
        val response = authApi.getCurrentUser()
        assertNotNull(response)
    }

}