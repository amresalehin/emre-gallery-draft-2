package com.example.model

data class MemoryHighlight(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverUri: String,
    val mediaIds: List<String>
)
