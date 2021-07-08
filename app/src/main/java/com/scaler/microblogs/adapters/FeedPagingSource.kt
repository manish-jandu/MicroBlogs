package com.scaler.microblogs.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.models.Article
import retrofit2.HttpException
import java.io.IOException

private const val FEED_STARTING_INDEX = 1

class FeedPagingSource(
    private val api: ConduitApi,
    private val tag:String?=null
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: FEED_STARTING_INDEX

        return try {
            if(tag.isNullOrEmpty()){
                //get simple global feed
                val response = api.getArticles(params.loadSize)
                val articles = response.articles
                LoadResult.Page(
                    data = articles!!,
                    prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                    nextKey = if (articles.isEmpty()) null else position + 1
                )
            }else{
                //get feed by tags
                val response = api.getFeedArticlesByTag(params.loadSize,tag)
                val articles = response.articles
                LoadResult.Page(
                    data = articles!!,
                    prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                    nextKey = if (articles.isEmpty()) null else position + 1
                )
            }

        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

}
