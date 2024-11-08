package com.example.githubstars.model

import androidx.room.Ignore

data class Response(
    val name: String,
    val description: String,
    val html_url: String,
    val created_at: String,
    val stargazers_count: Int,
    val owner: Owner,
    @Ignore val stargazers: List<Stargazer> = emptyList()
) {
    data class Owner(
        val login: String,
        val html_url: String,
        val avatar_url: String
    )
    data class Stargazer(
        val login: String,
        val html_url: String,
        val starred_at: String
    )
}