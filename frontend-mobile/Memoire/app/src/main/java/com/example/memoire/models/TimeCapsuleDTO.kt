package com.example.memoire.models

import java.util.Date


data class TimeCapsuleDTO(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val createdAt: Date? = null,
    val openDate: Date? = null,
    val contents: List<CapsuleContentEntity>? = emptyList(),
    val locked: Boolean = false,
    val createdById: Long? = null,
    val createdBy: UserEntity? = null,
    val status: String? = null,

    )

