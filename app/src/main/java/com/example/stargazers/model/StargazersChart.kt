package com.example.stargazers.model

data class StargazerCount(val starred_at: String)

data class StargazerCountByPointOfTime(val pointOfTime: String, val count: Int)

