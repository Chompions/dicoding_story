package com.sawelo.dicoding_story.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

        setIvAddPhoto()
        setBtnAddCamera()
        setBtnAddGallery()
        setEdAddDescription()
        setRbAddLocation()
        setBtnAddUpload()
    }

    private fun setIvAddPhoto() {
        viewModel.tempStoryImageFile.observe(viewLifecycleOwner) {
            val decodedImage = BitmapFactory.decodeFile(it.path)
            binding.ivAddPhoto.setImageBitmap(decodedImage)
        }
    }

    private fun setEdAddDescription() {
        binding.edAddDescription.setText(viewModel.tempStoryDescText)
        binding.edAddDescription.doAfterTextChanged { viewModel.tempStoryDescText = it.toString() }
    }

    private fun setBtnAddCamera() {
        binding.btnAddCamera.setOnClickListener {
            val intent = Intent(context, CameraActivity::class.java)
            launcherCameraActivityResult.launch(intent)
        }
    }

    private fun setBtnAddGallery() {
        binding.btnAddGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a Picture")
            launcherIntentGalleryResult.launch(chooser)
        }
    }

    private fun setRbAddLocation() {
        binding.rbAddLocation.isChecked = false
        binding.rbAddLocation.setOnCheckedChangeListener { _, isChecked ->
            val requiredPermissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
            if (isChecked) launcherLocationPermissionResult.launch(requiredPermissions)
        }
    }

    private fun setBtnAddUpload() {
        binding.btnAddUpload.setOnClickListener {
            lifecycleScope.launch {
                binding.pbAddProgressBar.visibility = View.VISIBLE
                if (viewModel.getImageFileMultipartBody(CameraUtilsImpl) != null &&
                    viewModel.getDescTextRequestBody() != null
                ) {
                    viewModel.postStory(CameraUtilsImpl)
                    parentFragmentManager.setFragmentResult(
                        ADD_STORY_REQUEST_KEY,
                        bundleOf(ADD_STORY_FINISHED to true))
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

    @SuppressLint("MissingPermission")
    private val launcherLocationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val anyPermissionGranted = permissions.entries.any { it.value }
        if (anyPermissionGranted) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation.addOnSuccessListener {
                viewModel.tempStoryLocation = it
            }
        } else {
            binding.rbAddLocation.isChecked = false
            Toast
                .makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {
        const val ADD_STORY_REQUEST_KEY = "ADD_STORY_REQUEST_KEY"
        const val ADD_STORY_FINISHED = "ADD_STORY_FINISHED"
    }
}