package com.example.memoire.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

object ImageUtils {
    fun setBase64ImageToView(base64Image: String, imageView: ImageView) {
        try {
            val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(decodedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback will be handled by caller
        }
    }
}