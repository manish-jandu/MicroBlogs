package com.scaler.microblogs.data

import com.scaler.libconduit.apis.ConduitAuthApi
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor( @Named("authApi") private val api: ConduitAuthApi) {



}