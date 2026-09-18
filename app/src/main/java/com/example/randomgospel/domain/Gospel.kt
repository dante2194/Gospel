package com.example.randomgospel.domain

data class Gospel(
    val name: String,
    val shortCode: String,
    val totalChapters: Int
)

// Chapter counts: Matthew 28, Mark 16, Luke 24, John 21
val gospelList = listOf(
    Gospel("Matthew", "mat", 28),
    Gospel("Mark", "mrk", 16),
    Gospel("Luke", "luk", 24),
    Gospel("John", "jhn", 21)
)
