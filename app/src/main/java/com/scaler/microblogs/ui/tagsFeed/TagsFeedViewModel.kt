package com.scaler.microblogs.ui.tagsFeed

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
class TagsFeedViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    // TODO: Implement the ViewModel
     lateinit var articleByTag: LiveData<PagingData<Article>>


    fun getArticlesByTag(tag: String) {
        articleByTag = repo.getFeedByTag(tag).cachedIn(viewModelScope)
    }


}