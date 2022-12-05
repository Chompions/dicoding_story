package com.sawelo.dicoding_story.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sawelo.dicoding_story.data.remote.StoryRepository
import com.sawelo.dicoding_story.ui.TestUtils.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @Mock
    private lateinit var mockStoryRepository: StoryRepository
    private lateinit var authViewModel: AuthViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        authViewModel = AuthViewModel(mockStoryRepository)
    }

    @Test
    fun `set and get name`() {
        val expectedName = randomString("name")
        authViewModel.setName(expectedName)
        val actualName = authViewModel.name.value
        Assert.assertEquals(expectedName, actualName)
    }

    @Test
    fun `set and get email`() {
        val expectedEmail = randomString("email")
        authViewModel.setEmail(expectedEmail)
        val actualEmail = authViewModel.email.value
        Assert.assertEquals(expectedEmail, actualEmail)
    }

    @Test
    fun `set and get password`() {
        val expectedPassword = randomString("password")
        authViewModel.setPassword(expectedPassword)
        val actualPassword = authViewModel.password.value
        Assert.assertEquals(expectedPassword, actualPassword)
    }

    @Test
    fun register() = runTest {
        val expectedName = randomString("name").also {
            authViewModel.setName(it)
        }
        val expectedEmail = randomString("email").also {
            authViewModel.setEmail(it)
        }
        val expectedPassword = randomString("password").also {
            authViewModel.setPassword(it)
        }

        authViewModel.register()
        Mockito.verify(mockStoryRepository)
            .register(expectedName, expectedEmail, expectedPassword)
    }

    @Test
    fun login() = runTest {
        val expectedEmail = randomString("email").also {
            authViewModel.setEmail(it)
        }
        val expectedPassword = randomString("password").also {
            authViewModel.setPassword(it)
        }

        authViewModel.login()
        Mockito.verify(mockStoryRepository)
            .login(expectedEmail, expectedPassword)
    }
}