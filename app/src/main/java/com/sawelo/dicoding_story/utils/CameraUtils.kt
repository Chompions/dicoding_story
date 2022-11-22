package com.sawelo.dicoding_story.utils

import android.content.Context
import java.io.File

interface CameraUtils {
    fun createTempFile(context: Context): File
    fun reduceFileImage(file: File): File
}