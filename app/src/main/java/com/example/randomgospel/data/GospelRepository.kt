package com.example.randomgospel.data

import com.example.randomgospel.domain.Gospel
import com.example.randomgospel.domain.gospelList

class GospelRepository {
    fun getRandomGospel(): Gospel = gospelList.random()
}
