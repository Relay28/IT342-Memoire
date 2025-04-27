package com.example.memoire.models

import java.util.Date

data class CapsuleContentEntity(
    val id: Long?,
    val capsuleId: Long,
    val contentType: String,
    val fileName: String?, // Optional as it's not in your entity
    val fileSize: Long?, // Can be calculated from fileData if needed
    val uploadedAt: String, // Match entity field name
    val uploadedBy: String?, // Username of content uploader
    val contentUrl: String?, // URL to access the content via API
    val canEdit: Boolean = false,
    val isImage: Boolean = false
)
//
//data class CapsuleContentEntity(
//    val id: Long?,
//    val capsuleId: Long,
//    val contentType: String,
//    val uploadedAt: Date?,
//    val uploadedBy: String?,
//    val contentUrl: String,
//    val canEdit: Boolean = false,
//    val isImage: Boolean = false
//)

data class RenderableContent(
    val id: Long,
    val contentType: String,
    val uploadedAt: String,
    val uploadedBy: String,
    val contentUrl: String,
    val canEdit: Boolean,
    val isImage: Boolean
)
data class ErrorResponse(
    val status: Int,
    val message: String
)