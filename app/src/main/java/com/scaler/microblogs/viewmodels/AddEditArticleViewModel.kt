package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Article
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

@HiltViewModel
class AddEditArticleViewModel @Inject constructor(
    private val repo: Repository,
    private val appPrefStorage: AppPrefStorage
) :
    ViewModel() {
    var isInternetAvailable: Boolean? = null

    private val _article = MutableLiveData<NetworkResult<Article>>()
    val article: LiveData<NetworkResult<Article>> = _article

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
                addEditArticleChannel.send(AddEditArticleEvent.Error("No Field should be Empty."))
            } else {
                if (getUserToken()) {
                    val tagsList = tags.split(",")
                    createArticleLoggedIn(title, description, body, tagsList)
                } else {
                    addEditArticleChannel.send(AddEditArticleEvent.LoggedOut)
                }
            }
        }

    private fun createArticleLoggedIn(
        title: String,
        description: String,
        body: String,
        tagsList: List<String>
    ) = viewModelScope.launch(Dispatchers.IO) {
        addEditArticleChannel.send(AddEditArticleEvent.Loading)
        if (hasInternetConnection()) {
            try {
                val result = repo.authRemote.createArticle(title, description, body, tagsList)
                handleCreateArticleResponse(result)
            } catch (e: Exception) {
                addEditArticleChannel.send(AddEditArticleEvent.Error(e.message.toString()))
            }
        } else {
            addEditArticleChannel.send(AddEditArticleEvent.Error("No Internet Connection."))
        }
    }

    fun getArticleData(slug: String) = viewModelScope.launch(Dispatchers.IO) {
        _article.postValue(NetworkResult.Loading())
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

    private suspend fun handleCreateArticleResponse(response: Response<SingleArticleResponse>) {
        when {
            response.message().contains("timeout") -> {
                addEditArticleChannel.send(AddEditArticleEvent.Error("Timeout"))
            }
            response.body()!!.article == null -> {
                addEditArticleChannel.send(AddEditArticleEvent.Error("Something went wrong,try again!"))
            }
            response.isSuccessful -> {
                addEditArticleChannel.send(AddEditArticleEvent.ArticleCreated)
            }
            else -> {
                addEditArticleChannel.send(AddEditArticleEvent.Error(response.message()))
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        if (isInternetAvailable != null) {
            return isInternetAvailable as Boolean
        }
        return false
    }

    sealed class AddEditArticleEvent {
        object Loading : AddEditArticleEvent()
        object ArticleCreated : AddEditArticleEvent()
        data class Error(val errorMessage: String) : AddEditArticleEvent()
        object LoggedOut : AddEditArticleEvent()
    }
}