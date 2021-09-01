package com.scaler.microblogs.data

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(
    remoteDataSource: RemoteDataSource,
    authRemoteDataSource: AuthRemoteDataSource
) {
    val remote = remoteDataSource
    val authRemote = authRemoteDataSource
}