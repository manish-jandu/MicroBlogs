package com.scaler.microblogs.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedFavUserArticlesViewModel @Inject constructor(
    private val repo: Repository,) :ViewModel() {

    fun getUserFavouriteArticle(userName: String) =
        repo.remote.getFeedByUserFavourite(userName).cachedIn(viewModelScope)

    fun getUserArticles(userName: String) =
        repo.remote.getFeedByUserName(userName).cachedIn(viewModelScope)
}