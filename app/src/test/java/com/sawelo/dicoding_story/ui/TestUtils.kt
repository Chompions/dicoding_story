package com.sawelo.dicoding_story.ui

import kotlin.random.Random

object TestUtils {
    fun randomString(prefix: String = ""): String {
        val charPool = ('a'..'z') + ('A'..'Z')
        return (1..20).map {
            Random.nextInt(0, charPool.size).let {
                charPool[it]
            }
        }.joinToString("", prefix)
    }
}