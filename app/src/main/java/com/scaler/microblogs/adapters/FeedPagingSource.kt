package com.scaler.microblogs.adapters

import androidx.paging.PagingSource
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.apis.ConduitAuthApi
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.utils.FeedType
import retrofit2.HttpException
import java.io.IOException

private const val FEED_STARTING_INDEX = 1

class FeedPagingSource(
    private val api: ConduitApi,
    private val authApi: ConduitAuthApi,
    private val tag: String? = null,
    private val userName: String? = null,
    private val feedType: FeedType
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: FEED_STARTING_INDEX

        return try {
            when (feedType) {
                FeedType.GLOBAL_FEED -> {
                    //get simple global feed
                    val response = api.getArticles(params.loadSize)
                    val articles = response.body()!!.articles
                    LoadResult.Page(
                        data = articles!!,
                        prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                        nextKey = if (articles.isEmpty()) null else position + 1
                    )
                }
                FeedType.CURRENT_USER_FEED -> {
                    val response = authApi.getFeedArticles(params.loadSize)
                    val articles = response.body()!!.articles
                    LoadResult.Page(
                        data = articles!!,
                        prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                        nextKey = if (articles.isEmpty()) null else position + 1
                    )
                }
                FeedType.TAG_FEED -> {
                    val response = api.getFeedArticlesByTag(params.loadSize, tag!!)
                    val articles = response.body()!!.articles
                    LoadResult.Page(
                        data = articles!!,
                        prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                        nextKey = if (articles.isEmpty()) null else position + 1
                    )
                }
                FeedType.PROFILE_FEED ->{
                    val response = api.getFeedArticlesByUserName(params.loadSize, userName!!)
                    val articles = response.body()!!.articles
                    LoadResult.Page(
                        data = articles!!,
                        prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                        nextKey = if (articles.isEmpty()) null else position + 1
                    )
                }
                FeedType.PROFILE_FAVOURITE_FEED->{
                val response = api.getFeedArticlesByUserFavourite(params.loadSize, userName!!)
                val articles = response.body()!!.articles
                LoadResult.Page(
                    data = articles!!,
                    prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                    nextKey = if (articles.isEmpty()) null else position + 1
                )
                }
                else ->{
                    //get simple global feed
                    val response = api.getArticles(params.loadSize)
                    val articles = response.body()!!.articles
                    LoadResult.Page(
                        data = articles!!,
                        prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                        nextKey = if (articles.isEmpty()) null else position + 1
                    )
                }
            }


        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

}
