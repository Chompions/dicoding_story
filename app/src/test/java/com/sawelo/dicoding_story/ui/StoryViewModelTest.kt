package com.sawelo.dicoding_story.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import androidx.recyclerview.widget.AdapterListUpdateCallback
import com.sawelo.dicoding_story.remote.StoryListResponse
import com.sawelo.dicoding_story.remote.StoryRepository
import com.sawelo.dicoding_story.utils.CameraUtils
import com.sawelo.dicoding_story.utils.StoryComparator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @Mock
    private lateinit var mockStoryRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel
    private val dispatcher = StandardTestDispatcher()

    private fun randomString(prefix: String = ""): String {
        val charPool = ('a'..'z') + ('A'..'Z')
        return (1..20).map {
            Random.nextInt(0, charPool.size).let {
                charPool[it]
            }
        }.joinToString("", prefix)
    }

    private fun fakeStoryListResponse(prefix: String) = StoryListResponse(
        id = randomString(prefix),
        name = randomString(prefix),
        description = randomString(prefix),
        photoUrl = randomString(prefix),
        createdAt = randomString(prefix),
        lat = Random.nextFloat(),
        lon = Random.nextFloat(),
    )

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        storyViewModel = StoryViewModel(mockStoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setCurrentStory and getCurrentStory`() {
        val fakeResponse = fakeStoryListResponse("story")

        val expectedCurrentStory = MutableLiveData<StoryListResponse>()
        expectedCurrentStory.value = fakeResponse

        storyViewModel.setCurrentStory(fakeResponse)
        val actualCurrentStory = storyViewModel.currentStory

        Assert.assertEquals(expectedCurrentStory.value, actualCurrentStory.value)
    }

    @Test
    fun `setTempStoryDescText and getDescTextRequestBody`() {
        val fakeString = randomString()
        val expectedDescTextRequestBody = fakeString.toRequestBody("text/plain".toMediaType())

        storyViewModel.setTempStoryDescText(fakeString)
        val actualDescTextRequestBody = storyViewModel.getDescTextRequestBody()

        Assert.assertEquals(expectedDescTextRequestBody.contentLength(), actualDescTextRequestBody?.contentLength())
        Assert.assertEquals(expectedDescTextRequestBody.contentType(), actualDescTextRequestBody?.contentType())
    }

    @Test
    fun `setTempStoryImageFile and getImageFileMultipartBody`() {
        val mockCameraUtils = mock(CameraUtils::class.java)
        val mockFile = File.createTempFile(randomString("prefix"), randomString("suffix"))

        val dir = "test_color.png"
        val inputStream = javaClass.classLoader?.getResourceAsStream(dir) as InputStream
        val outputStream = FileOutputStream(mockFile)
        val buffer = ByteArray(512)

        var len: Int
        while (inputStream.read(buffer).also { len = it } > 0) {
            outputStream.write(buffer, 0, len)
        }
        outputStream.close()
        inputStream.close()

        val requestFile = mockFile.asRequestBody("image/file".toMediaType())
        val expectedTempStoryImageFile = MultipartBody.Part.createFormData(
            "photo", mockFile.name, requestFile).body

        `when`(mockCameraUtils.reduceFileImage(mockFile)).thenReturn(mockFile)
        storyViewModel.setTempStoryImageFile(mockFile)
        val actualTempStoryImageFile = storyViewModel
            .getImageFileMultipartBody(mockCameraUtils)?.body

        Assert.assertEquals(expectedTempStoryImageFile.contentLength(), actualTempStoryImageFile?.contentLength())
        Assert.assertEquals(expectedTempStoryImageFile.contentType(), actualTempStoryImageFile?.contentType())
    }

    @Test
    fun getStories() = runTest {

        val mockPagingDataAdapter = mock(PagingDataAdapter::class.java)
        val pagingDataDiffer = AsyncPagingDataDiffer(
            StoryComparator,
            AdapterListUpdateCallback(mockPagingDataAdapter)
        )

        val fakeResponse = List(10) {
            fakeStoryListResponse(it.toString())
        }

        val expectedFlow = flow {
            delay(1000L)
            emit(PagingData.from(fakeResponse))
        }
        expectedFlow.take(1).collect {
            pagingDataDiffer.submitData(it)
        }
        val expectedStories = pagingDataDiffer.snapshot().items

        `when`(mockStoryRepository.getStoriesFlow()).thenReturn(expectedFlow)
        val actualFlow = storyViewModel.getStories()
        actualFlow.take(1).collect {
            pagingDataDiffer.submitData(it)
        }
        val actualStories = pagingDataDiffer.snapshot().items

        Assert.assertEquals(expectedStories, actualStories)
    }

    @Test
    fun postStory() {
    }
}