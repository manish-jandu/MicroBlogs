package com.scaler.microblogs.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
     lateinit var articleByTag: LiveData<PagingData<Article>>

    val articles = repo.getFeeds().cachedIn(viewModelScope)

    fun getArticlesByTag(tag: String) {
        articleByTag = repo.getFeedByTag(tag)
    }


}