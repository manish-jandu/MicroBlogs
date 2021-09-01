package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Article
import com.scaler.libconduit.responses.MultipleCommentResponse
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
class ArticleViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    private val _article = MutableLiveData<Article>()
    val article: LiveData<Article> = _article

    private val _comments = MutableLiveData<MultipleCommentResponse>()
    val comments: LiveData<MultipleCommentResponse> = _comments

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val articleEventChannel = Channel<ArticleEvent>()
    val articleEvent = articleEventChannel.receiveAsFlow()


    private fun getUserToken(): Boolean {
        val token = appPrefStorage.getUserToken()
        return if (token.isNullOrEmpty()) {
            false
        } else {
            AuthModule.authToken = token
            true
        }
    }

    fun checkIfLoggedIn() = viewModelScope.launch {
        if (getUserToken()) {
            val result = authRepo.getCurrentUser()
            if (result.isSuccessful) {
                _isLoggedIn.postValue(true)
            } else {
                _isLoggedIn.postValue(false)
            }
        } else {
            _isLoggedIn.postValue(false)
        }
    }

    fun deleteArticle(slug: String) = viewModelScope.launch {
        authRepo.deleteArticle(slug)
    }

    fun getArticleDataByRepo(slug: String) = viewModelScope.launch {
        val result = repo.getArticleBySlug(slug)
        if (result.isSuccessful) {
            _article.postValue(result.body()!!.article!!)
        } else {
            articleEventChannel.send(ArticleEvent.Error)
        }
    }

    fun getArticleDataByAuthRepo(slug: String) = viewModelScope.launch {
        val result = authRepo.getArticleBySlug(slug)
        if (result.isSuccessful) {
            _article.postValue(result.body()!!.article!!)
        } else {
            articleEventChannel.send(ArticleEvent.Error)
        }
    }

    fun getComments(slug: String) = viewModelScope.launch {
        val result = repo.getCommentsBySlug(slug)
        if (result.isSuccessful) {
            _comments.postValue(result.body())
        } else {
            articleEventChannel.send(ArticleEvent.Error)
        }
    }

    fun createComment(slug: String, body: String) = viewModelScope.launch {
        val result = authRepo.createComment(slug, body)
        if (result.isSuccessful) {
            getComments(slug)
        }
    }

    fun likeArticle(slug: String) = viewModelScope.launch {
        authRepo.likeArticle(slug)
        getArticleAfterLikeDislike(slug)
    }

    private fun getArticleAfterLikeDislike(slug: String) {
        if (getUserToken()) {
            getArticleDataByAuthRepo(slug)
        } else {
            getArticleDataByRepo(slug)
        }
    }

    fun unlikeArticle(slug: String) = viewModelScope.launch {
        authRepo.unlikeArticle(slug)
        getArticleAfterLikeDislike(slug)
    }

    sealed class ArticleEvent {
        object Error : ArticleEvent()

    }
}