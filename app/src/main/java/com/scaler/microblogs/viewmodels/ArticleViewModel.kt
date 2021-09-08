package com.scaler.microblogs.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Article
import com.scaler.libconduit.models.Comment
import com.scaler.libconduit.responses.MultipleCommentResponse
import com.scaler.libconduit.responses.SingleArticleResponse
import com.scaler.microblogs.data.AppPrefStorage
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.di.AuthModule
import com.scaler.microblogs.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "ArticleViewModel"

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) : ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _article = MutableLiveData<NetworkResult<Article>>()
    val article: LiveData<NetworkResult<Article>> = _article

    private val _comments = MutableLiveData<NetworkResult<List<Comment>>>()
    val comments: LiveData<NetworkResult<List<Comment>>> = _comments

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val articleEventChannel = Channel<ArticleEvent>()
    val articleEvent = articleEventChannel.receiveAsFlow()

    fun checkIfLoggedIn() = viewModelScope.launch {
        val token = appPrefStorage.getUserToken()
        if (token.isNullOrEmpty()) {
            _isLoggedIn.postValue(false)
        } else {
            AuthModule.authToken = token
            _isLoggedIn.postValue(true)
        }
    }

    fun deleteArticle(slug: String) = viewModelScope.launch {
        repo.authRemote.deleteArticle(slug)
    }

    fun getArticleDataByRepo(slug: String) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                val result = repo.remote.getArticleBySlug(slug)
                _article.postValue(handleArticleResponse(result))
            } catch (e: Exception) {
                _article.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _article.postValue(NetworkResult.Error("No Internet Connection."))
        }
    }

    fun getArticleDataByAuthRepo(slug: String) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                val result = repo.authRemote.getArticleBySlug(slug)
                _article.postValue(handleArticleResponse(result))
            } catch (e: Exception) {
                _article.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _article.postValue(NetworkResult.Error("No Internet Connection."))
        }
    }

    private fun handleArticleResponse(response: Response<SingleArticleResponse>): NetworkResult<Article> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()!!.article == null -> {
                NetworkResult.Error("Article not Found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.article!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    fun getComments(slug: String) = viewModelScope.launch(Dispatchers.IO) {
        if (hasInternetConnection()) {
            try {
                val result = repo.remote.getCommentsBySlug(slug)
                _comments.postValue(handleCommentsResponse(result))
            } catch (e: Exception) {
                _comments.postValue(NetworkResult.Error(e.message.toString()))
            }
        } else {
            _comments.postValue(NetworkResult.Error("No Internet Connection."))
        }
    }

    private fun handleCommentsResponse(response: Response<MultipleCommentResponse>): NetworkResult<List<Comment>> {
        return when {
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()!!.comments == null -> {
                NetworkResult.Error("comments not Found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.comments!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    fun createComment(slug: String, body: String) = viewModelScope.launch {
        try {
            val result = repo.authRemote.createComment(slug, body)
            when {
                result.message().contains("timeout") -> {
                    articleEventChannel.send(ArticleEvent.Error("Timeout"))
                }
                result.body() == null -> {
                    articleEventChannel.send(ArticleEvent.Error("Something went wrong"))
                }
                result.isSuccessful -> {
                    //success-refresh comments
                    getComments(slug)
                }
                else -> {
                    articleEventChannel.send(ArticleEvent.Error("Something went wrong"))
                }
            }
        } catch (e: Exception) {
            articleEventChannel.send(ArticleEvent.Error(e.message.toString()))
        }
    }

    fun likeArticle(slug: String) = viewModelScope.launch {
        try {
            val result = repo.authRemote.likeArticle(slug)
            if (result.isSuccessful) {
                _article.postValue(NetworkResult.Success(result.body()!!))
            }
        } catch (e: Exception) {
            Log.i(TAG, e.message.toString())
        }
    }

    fun unlikeArticle(slug: String) = viewModelScope.launch {
        try {
            val result = repo.authRemote.unlikeArticle(slug)
            if (result.isSuccessful) {
                _article.postValue(NetworkResult.Success(result.body()!!))
            }
        } catch (e: Exception) {
            Log.i(TAG, e.message.toString())
        }
    }

    private fun hasInternetConnection(): Boolean {
        if (isInternetAvailable != null) {
            return isInternetAvailable as Boolean
        }
        return false
    }

    sealed class ArticleEvent {
        data class Error(val errorMessage: String) : ArticleEvent()
    }
}