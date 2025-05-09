package com.example.memoire.models

import java.util.Date

data class ReportDTO(
    val id: Long,
    val reportedID: Long,
    val itemType: String,
    val reporter: UserDTO, // Assuming UserDTO is already defined
    val status: String,
    val date: Date
)

data class ReportRequest(
    val reporterId: Long,
    val reportedID: Long,
    val itemType: String,
    val status: String
)