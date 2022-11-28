package com.sawelo.dicoding_story.ui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sawelo.dicoding_story.data.StoryListResponse
import com.sawelo.dicoding_story.data.remote.StoryRepository
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

    var tempStoryDescText: String = ""
    var tempStoryLocation: Location? = null

    fun setCurrentStory(story: StoryListResponse) {
        _currentStory.value = story
    }

    fun setTempStoryImageFile(file: File) {
        _tempStoryImageFile.postValue(file)
    }

    fun getDescTextRequestBody(): RequestBody? {
        return if (tempStoryDescText.isNotBlank()) {
            tempStoryDescText.toRequestBody("text/plain".toMediaType())
        } else null
    }

    fun getImageFileMultipartBody(cameraUtils: CameraUtils): MultipartBody.Part? {
        val file = _tempStoryImageFile.value
        return if (file != null) {
            val compressedFile = cameraUtils.reduceFileImage(file)
            val request = compressedFile.asRequestBody("image/file".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", compressedFile.name, request)
        } else null
    }

    suspend fun getStoriesList(): List<StoryListResponse> {
        return storyRepository.getStoriesList(1, 10, 1)
    }

    fun getStoriesFlow(): Flow<PagingData<StoryListResponse>> {
        if (_storiesPagingData == null) {
            _storiesPagingData = storyRepository.getStoriesFlow().cachedIn(viewModelScope)
        }
        return _storiesPagingData as Flow<PagingData<StoryListResponse>>
    }

    suspend fun postStory(cameraUtils: CameraUtils) {
        val imageFileMultipartBody = getImageFileMultipartBody(cameraUtils)
        val descTextRequestBody = getDescTextRequestBody()
        if (imageFileMultipartBody != null) {
            if (tempStoryLocation != null) {
                storyRepository.postStory(
                    imageFileMultipartBody, descTextRequestBody, tempStoryLocation)
            } else {
                storyRepository.postStory(
                    imageFileMultipartBody, descTextRequestBody)
            }

        }
    }
}