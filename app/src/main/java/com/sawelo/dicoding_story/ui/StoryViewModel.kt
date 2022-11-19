package com.sawelo.dicoding_story.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sawelo.dicoding_story.remote.StoryListResponse
import com.sawelo.dicoding_story.remote.StoryRepository
import com.sawelo.dicoding_story.utils.CameraUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    private var _storiesPagingData: Flow<PagingData<StoryListResponse>>? = null
    private val _currentStory = MutableLiveData<StoryListResponse>()
    val currentStory: LiveData<StoryListResponse> = _currentStory

    private val _tempStoryImageFile = MutableLiveData<File>()
    val tempStoryImageFile: LiveData<File> = _tempStoryImageFile
    private val _tempStoryDescText = MutableLiveData<String>()
    val tempStoryDescText: LiveData<String> = _tempStoryDescText

    fun setCurrentStory(story: StoryListResponse) {
        _currentStory.value = story
    }

    fun setTempStoryImageFile(file: File) {
        _tempStoryImageFile.postValue(file)
    }

    fun setTempStoryDescText(text: String) {
        _tempStoryDescText.value = text
    }

    fun getStories(): Flow<PagingData<StoryListResponse>> {
        if (_storiesPagingData == null) {
            _storiesPagingData = storyRepository.getStoriesFlow().cachedIn(viewModelScope)
        }
        return _storiesPagingData as Flow<PagingData<StoryListResponse>>
    }

    fun getDescTextRequestBody(): RequestBody? {
        return tempStoryDescText.value?.toRequestBody("text/plain".toMediaType())
    }

    fun getImageFileMultipartBody(): MultipartBody.Part? {
        val file = tempStoryImageFile.value
        return if (file != null) {
            val compressedFile = CameraUtils.reduceFileImage(file)
            val request = compressedFile.asRequestBody("image/file".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", compressedFile.name, request)
        } else null
    }

    suspend fun postStory() {
        val imageFileMultipartBody = getImageFileMultipartBody()
        val descTextRequestBody = getDescTextRequestBody()
        if (imageFileMultipartBody != null && descTextRequestBody != null) {
            storyRepository.postStory(imageFileMultipartBody, descTextRequestBody)
        }
    }
}