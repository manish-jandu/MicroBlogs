package com.scaler.microblogs.ui.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaler.microblogs.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {
    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> = _tags

    fun getTags() = viewModelScope.launch {
        val result = repo.getTags()
        result.body()!!.tags?.let {
            _tags.postValue(it)
        }
    }

}