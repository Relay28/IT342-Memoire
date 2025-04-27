package com.example.memoire.repository

import android.content.Context
import android.net.Uri
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleContentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CapsuleContentRepository {
    private val capsuleContentService = RetrofitClient.capsuleContentInstance

//    suspend fun getRenderableContents(capsuleId: Long): List<CapsuleContentEntity> {
//        return withContext(Dispatchers.IO) {
//            val response = capsuleContentService.getContentsByCapsule(capsuleId)
//            if (response.isSuccessful) {
//                response.body() ?: emptyList()
//            } else {
//                throw HttpException(response)
//            }
//        }
//    }

    suspend fun deleteContent(contentId: Long) {
        withContext(Dispatchers.IO) {
            val response = capsuleContentService.deleteContent(contentId)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

    suspend fun uploadContent(capsuleId: Long, uri: Uri, contentType: String, context: Context): CapsuleContentEntity {
        return withContext(Dispatchers.IO) {
            // Get file from Uri
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open input stream for URI")

            val file = File(context.cacheDir, "temp_upload_file")
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // Create RequestBody and MultipartBody.Part
            val requestFile = file.asRequestBody(contentType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Make API call
            val response = capsuleContentService.uploadContent(capsuleId, filePart)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw HttpException(response)
            }
        }
    }
}