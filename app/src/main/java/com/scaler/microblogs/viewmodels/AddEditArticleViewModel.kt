package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.AuthRepository
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditArticleViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) :
    ViewModel() {
    private val _article = MutableLiveData<Article>()
    val article: LiveData<Article> = _article

    private val addEditArticleChannel = Channel<AddEditArticleEvent>()
    val addEditArticleEvent = addEditArticleChannel.receiveAsFlow()

    private fun getUserToken(): Boolean {
        val token = appPrefStorage.getUserToken()
        return if (token.isNullOrEmpty()) {
            false
        } else {
            AuthModule.authToken = token
            true
        }
    }

    fun createArticle(title: String, description: String, body: String, tags: String) =
        viewModelScope.launch {
            if (title.isEmpty() || description.isEmpty() || body.isEmpty() || tags.isEmpty()) {
                addEditArticleChannel.send(AddEditArticleEvent.DataIsEmpty)
            } else {
                if(getUserToken()){
                    val tagsList = tags.split(",")
                    val result = authRepo.createArticle(title, description, body, tagsList)
                    if (result.isSuccessful) {
                        addEditArticleChannel.send(AddEditArticleEvent.ArticleCreated)

                    } else {
                        addEditArticleChannel.send(AddEditArticleEvent.Error)
                    }
                }else{
                    addEditArticleChannel.send(AddEditArticleEvent.LoggedOut)
                }
            }
        }

    fun getArticleData(slug: String) = viewModelScope.launch {
        val result = repo.getArticleBySlug(slug)
        if (result.isSuccessful) {
            _article.postValue(result.body()!!.article!!)
        }
    }

    sealed class AddEditArticleEvent {
        object DataIsEmpty : AddEditArticleEvent()
        object ArticleCreated : AddEditArticleEvent()
        object Error : AddEditArticleEvent()
        object LoggedOut : AddEditArticleEvent()
    }
}