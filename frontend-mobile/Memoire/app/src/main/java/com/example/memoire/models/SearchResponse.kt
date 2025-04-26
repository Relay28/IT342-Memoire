package com.example.memoire.models

data class SearchResponse(
    val results: List<ProfileDTO>,
    val page: Int,
    val size: Int
)
