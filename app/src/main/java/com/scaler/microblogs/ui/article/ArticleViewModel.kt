package com.scaler.microblogs.ui.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.data.AuthRepository
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: Repository
) : ViewModel() {
    private val _article = MutableLiveData<Article>()
    val article:LiveData<Article> = _article


    private val articleEventChannel = Channel<ArticleEvent>()
    val articleEvent = articleEventChannel.receiveAsFlow()


    fun deleteArticle(slug: String) = viewModelScope.launch {
        authRepo.deleteArticle(slug)
    }

    fun getArticleData(slug: String) =viewModelScope.launch{
        val result = repo.getArticleBySlug(slug)
        if (result.isSuccessful ){
            _article.postValue(result.body()!!.article!!)
        }else{
            articleEventChannel.send(ArticleEvent.Error)
        }
    }

    sealed class ArticleEvent {
        object Error : ArticleEvent()
    }
}