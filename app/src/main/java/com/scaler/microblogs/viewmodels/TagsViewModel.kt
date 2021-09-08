package com.scaler.microblogs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.libconduit.responses.TagsResponse
import com.scaler.microblogs.data.Repository
import com.scaler.microblogs.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {
    private val _tags = MutableLiveData<NetworkResult<List<String>>>()
    val tags: LiveData<NetworkResult<List<String>>> = _tags

    fun getTags() = viewModelScope.launch(Dispatchers.IO) {
        _tags.postValue(NetworkResult.Loading())
        try {
            val result = repo.remote.getTags()
            _tags.postValue(handleTagsResponse(result))
        } catch (e: Exception) {
            _tags.postValue(NetworkResult.Error(e.message.toString()))
        }
    }

    private fun handleTagsResponse(response: Response<TagsResponse>): NetworkResult<List<String>>? {
        return when{
            response.message().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.body()!!.tags == null -> {
                NetworkResult.Error("tags not Found")
            }
            response.isSuccessful -> {
                val result = response.body()!!.tags!!
                NetworkResult.Success(result)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

}