package com.scaler.microblogs.adapters.pagingadapters

import androidx.paging.PagingSource
import com.scaler.libconduit.apis.ConduitAuthApi
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.utils.Constants.FEED_STARTING_INDEX
import retrofit2.HttpException
import java.io.IOException

class CurrentUserFeedPagingSource(
    private val authApi: ConduitAuthApi,
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: FEED_STARTING_INDEX

        return try {
            val response = authApi.getFeedArticles(params.loadSize)
            val articles = response.body()!!.articles
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