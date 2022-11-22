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
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.activity.CameraActivity
import com.sawelo.dicoding_story.databinding.FragmentAddStoryBinding
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.CameraUtilsImpl
import com.sawelo.dicoding_story.utils.SharedPrefsData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        binding.edAddDescription.doAfterTextChanged { viewModel.setTempStoryDescText(it.toString()) }

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
            lifecycleScope.launch {
                binding.pbAddProgressBar.visibility = View.VISIBLE
                if (viewModel.getDescTextRequestBody() != null && viewModel.getImageFileMultipartBody() != null) {
                    viewModel.postStory()
                    parentFragmentManager.popBackStack()
                } else {
                    Snackbar
                        .make(binding.btnAddUpload, R.string.error_add_story, Snackbar.LENGTH_SHORT)
                        .show()
                }

                binding.pbAddProgressBar.visibility = View.GONE
            }
        }
    }

    private val launcherCameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                @Suppress("DEPRECATION") val file = if (Build.VERSION.SDK_INT >= 33) {
                    result.data?.getSerializableExtra(
                        CameraActivity.PHOTO_EXTRA, File::class.java
                    ) as File
                } else {
                    result.data?.getSerializableExtra(
                        CameraActivity.PHOTO_EXTRA
                    ) as File
                }

                val smallerFile = CameraUtilsImpl.createTempFile(requireContext())
                val inputStream = file.inputStream()
                val outputStream = FileOutputStream(smallerFile)
                val buffer = ByteArray(1024)

                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                outputStream.close()
                inputStream.close()

                viewModel.setTempStoryImageFile(smallerFile)
            }
        }
    }

    private val launcherIntentGalleryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch(Dispatchers.IO) {
                val selectedImg: Uri = result.data?.data as Uri
                val file = CameraUtilsImpl.createTempFile(requireContext())

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
}