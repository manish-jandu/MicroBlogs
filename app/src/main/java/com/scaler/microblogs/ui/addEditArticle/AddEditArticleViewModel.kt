package com.scaler.microblogs.ui.addEditArticle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.microblogs.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditArticleViewModel @Inject constructor(private val authRepo: AuthRepository) :
    ViewModel() {

    private val addEditArticleChannel = Channel<AddEditArticleEvent>()
    val addEditArticleEvent = addEditArticleChannel.receiveAsFlow()

    fun createArticle(title: String, description: String, body: String, tags: String) =
        viewModelScope.launch {
            if (title.isEmpty() || description.isEmpty() || body.isEmpty() || tags.isEmpty()) {
                addEditArticleChannel.send(AddEditArticleEvent.DataIsEmpty)
            } else {
                val tagsList = tags.split(",")
                val result = authRepo.createArticle(title, description, body, tagsList)
                if (result.isSuccessful) {
                    addEditArticleChannel.send(AddEditArticleEvent.ArticleCreated)

                } else {
                    addEditArticleChannel.send(AddEditArticleEvent.Error)
                }
            }
        }

    sealed class AddEditArticleEvent {
        object DataIsEmpty : AddEditArticleEvent()
        object ArticleCreated : AddEditArticleEvent()
        object Error : AddEditArticleEvent()
    }
}