package com.sawelo.dicoding_story.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.activity.CameraActivity
import com.sawelo.dicoding_story.databinding.FragmentAddStoryBinding
import com.sawelo.dicoding_story.remote.ApiConfig
import com.sawelo.dicoding_story.remote.StoryResponse
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.CameraUtils
import com.sawelo.dicoding_story.utils.SharedPrefsData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddStoryFragment : Fragment() {
    @Inject
    lateinit var prefsData: SharedPrefsData
    private lateinit var binding: FragmentAddStoryBinding
    private val viewModel: StoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tempStoryImageFile.observe(viewLifecycleOwner) {
            val decodedImage = BitmapFactory.decodeFile(it.path)
            binding.ivAddPhoto.setImageBitmap(decodedImage)
        }

        binding.edAddDescription.setText(viewModel.tempStoryDescText.value)
        binding.edAddDescription.doAfterTextChanged {
            viewModel.setTempStoryDescText(it.toString())
        }

        binding.btnAddCamera.setOnClickListener {
            val intent = Intent(context, CameraActivity::class.java)
            launcherCameraActivityResult.launch(intent)
        }

        binding.btnAddGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a Picture")
            launcherIntentGalleryResult.launch(chooser)
        }

        binding.btnAddUpload.setOnClickListener {
            uploadStory()
        }
    }

    private val launcherCameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION") val file = if (Build.VERSION.SDK_INT >= 33) {
                result.data?.getSerializableExtra(
                    CameraActivity.PHOTO_EXTRA, File::class.java
                ) as File
            } else {
                result.data?.getSerializableExtra(
                    CameraActivity.PHOTO_EXTRA
                ) as File
            }

            viewModel.setTempStoryImageFile(file)
        }
    }

    private val launcherIntentGalleryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                val selectedImg: Uri = result.data?.data as Uri
                val file = CameraUtils.createTempFile(requireContext())

                val inputStream = requireContext().contentResolver
                    .openInputStream(selectedImg) as InputStream
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)

                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                outputStream.close()
                inputStream.close()

                viewModel.setTempStoryImageFile(file)
            }
        }
    }

    private fun uploadStory() {
        val imageFileMultipartBody = viewModel.getImageFileMultipartBody()
        val descTextRequestBody = viewModel.getDescTextRequestBody()

        if (imageFileMultipartBody != null && descTextRequestBody != null) {
            binding.pbAddProgressBar.visibility = View.VISIBLE

            val token = prefsData.getToken()
            val client = ApiConfig.getApiService().postStory(
                "Bearer $token", imageFileMultipartBody, descTextRequestBody
            )

            client.enqueue(object : Callback<StoryResponse> {
                override fun onResponse(
                    call: Call<StoryResponse>,
                    response: Response<StoryResponse>
                ) {
                    binding.pbAddProgressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Snackbar
                                .make(binding.root, body.message, Snackbar.LENGTH_SHORT)
                                .show()
                            parentFragmentManager.popBackStack()
                        }
                    } else {
                        if (response.errorBody() != null) {
                            val errorResponse = ApiConfig.convertErrorBody<StoryResponse>(
                                response.errorBody()!!
                            )
                            Snackbar
                                .make(
                                    binding.root,
                                    "Upload story failed: ${errorResponse.message}",
                                    Snackbar.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                }

                override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                    binding.pbAddProgressBar.visibility = View.GONE
                    Snackbar
                        .make(
                            binding.root,
                            "Upload story failed: ${t.message}",
                            Snackbar.LENGTH_SHORT
                        )
                        .show()
                }

            })
        } else {
            Snackbar
                .make(binding.root, "Image or description must not be empty", Snackbar.LENGTH_SHORT)
                .show()
        }
    }
}