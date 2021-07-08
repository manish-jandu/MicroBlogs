package com.scaler.microblogs.ui.feed

import androidx.lifecycle.ViewModel
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val repo:Repository) : ViewModel() {
    // TODO: Implement the ViewModel


    val articles = repo.getFeeds()


}