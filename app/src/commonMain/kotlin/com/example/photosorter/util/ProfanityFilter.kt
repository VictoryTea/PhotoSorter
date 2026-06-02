package com.example.photosorter.util

object ProfanityFilter {
    private val badWords = setOf(
        "fuck", "shit", "bitch", "cunt", "ass", "asshole", "dick", "cock", "pussy",
        "bastard", "slut", "whore", "fag", "nigger", "nigga", "crap", "piss"
        // Add more as needed, keeping it simple for now
    )

    fun isClean(username: String): Boolean {
        val lower = username.lowercase()
        return badWords.none { lower.contains(it) }
    }
}
