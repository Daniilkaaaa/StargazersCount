package com.example.githubstars.model

import java.time.LocalDate

data class StargazersCount(
    val date: LocalDate,
    val count: Int,
)
