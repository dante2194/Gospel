package com.example.randomgospel.util

import com.example.randomgospel.domain.gospelList

object BibleUrlBuilder {
    private const val BASE_URL = "https://bible.com/bible"

    /**
     * Random Gospel chapter URL.
     * Format: https://bible.com/bible/{versionId}/{book}.{chapter}.{translationCode}
     *   NIV -> https://bible.com/bible/111/mat.4.NIV
     *   AVD -> https://bible.com/bible/13/mat.4.AVD
     */
    fun buildRandomGospelUrl(translationCode: String, versionId: String): String {
        val gospel  = gospelList.random()
        val chapter = (1..gospel.totalChapters).random()
        return "$BASE_URL/$versionId/${gospel.shortCode}.$chapter.$translationCode"
    }
}
