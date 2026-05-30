package com.pulseo

data class Song(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val duration: Long = 0,
    val filePath: String = "",
    val dateImported: Long = 0
)