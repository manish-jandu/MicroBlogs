package com.scaler.microblogs.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.scaler.libconduit.apis.ConduitApi
import com.scaler.libconduit.models.Article
import retrofit2.HttpException
import java.io.IOException

private const val FEED_STARTING_INDEX = 1

class FeedPagingSource(
    val api: ConduitApi
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: FEED_STARTING_INDEX

        return try {
            val response = api.getArticles(params.loadSize)
            val articles = response.articles
            LoadResult.Page(
                data = articles!!,
                prevKey = if (position == FEED_STARTING_INDEX) null else position - 1,
                nextKey = if (articles.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

}
