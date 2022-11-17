package com.sawelo.dicoding_story.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.dicoding_story.remote.StoryListResponse
import com.sawelo.dicoding_story.utils.CameraUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryViewModel : ViewModel() {
    private val _listStory = MutableLiveData<List<StoryListResponse>>()
    val listStory: LiveData<List<StoryListResponse>> = _listStory
    private val _currentStory = MutableLiveData<StoryListResponse>()
    val currentStory: LiveData<StoryListResponse> = _currentStory

    private val _tempStoryImageFile = MutableLiveData<File>()
    val tempStoryImageFile: LiveData<File> = _tempStoryImageFile
    private val _tempStoryDescText = MutableLiveData<String>()
    val tempStoryDescText: LiveData<String> = _tempStoryDescText

    fun setListStory(list: List<StoryListResponse>) {
        _listStory.value = list
    }

    fun setCurrentStory(story: StoryListResponse) {
        _currentStory.value = story
    }

    fun setTempStoryImageFile(file: File) {
        _tempStoryImageFile.postValue(file)
    }

    fun setTempStoryDescText(text: String) {
        _tempStoryDescText.value = text
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
}