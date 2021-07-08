package com.scaler.microblogs.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.microblogs.adapters.FeedPagingSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val api: ConduitApi) {

    fun getFeeds() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FeedPagingSource(api)}
        ).liveData

    suspend fun getTags() = api.getTags()

      fun getFeedByTag(tag:String) =  Pager(
        config = PagingConfig(
            pageSize = 20,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { FeedPagingSource(api,tag)}
    ).liveData

}