package com.sawelo.dicoding_story.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.dicoding_story.remote.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email
    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    fun setName(name: String) {
        _name.value = name
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    suspend fun register() {
        val nameString = name.value
        val emailString = email.value
        val passwordString = password.value

        if (nameString != null &&
            emailString != null &&
            passwordString != null
        ) {
            storyRepository.register(
                nameString, emailString, passwordString
            )
        }
    }

    suspend fun login() {
        val emailString = email.value
        val passwordString = password.value

        if (emailString != null &&
            passwordString != null
        ) {
            storyRepository.login(
                emailString, passwordString
            )
        }
    }
}