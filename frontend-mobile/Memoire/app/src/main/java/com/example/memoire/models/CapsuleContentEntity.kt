package com.example.memoire.models
data class CapsuleContentEntity(
    val id: Long?,
    val capsuleId: Long,
    val contentType: String,
    val fileName: String,
    val fileSize: Long,
    val filePath: String,
    val uploadDate: String,
    val canEdit: Boolean = false,
    val isImage: Boolean = false
)
data class ErrorResponse(
    val status: Int,
    val message: String
)