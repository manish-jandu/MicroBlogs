package com.scaler.microblogs.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TagsFeedViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    fun getArticlesByTag(tag: String) = repo.remote.getFeedByTag(tag).cachedIn(viewModelScope)

}