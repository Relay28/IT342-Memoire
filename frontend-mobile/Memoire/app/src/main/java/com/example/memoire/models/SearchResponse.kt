package com.example.memoire.models

data class SearchResponse(
    val results: List<ProfileDTO2>,
    val page: Int,
    val size: Int
)
