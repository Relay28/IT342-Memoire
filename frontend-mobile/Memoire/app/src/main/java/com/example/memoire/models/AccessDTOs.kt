package com.example.memoire.models

// Data classes for capsule access
data class CapsuleAccessDTO(
    val id: Long,
    val capsuleId: Long,
    val userId: Long,
    val uploadedById: Long,
    val role: String
)
data class UpdateRoleRequest(
    val newRole: String
)