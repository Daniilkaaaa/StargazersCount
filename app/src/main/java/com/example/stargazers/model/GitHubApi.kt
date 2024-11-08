package com.example.githubstars.model

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 100
    ): Response<SearchRepositoriesResponse>

    @GET("repos/{owner}/{repo}/stargazers")
    fun getDateStargazers(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Accept") accept: String = "application/vnd.github.v3.star+json",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): Call<List<StargazerCount>>
    @GET("repos/{owner}/{repo}")
    suspend fun getCountStargazers(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): RepositoryStarsCount

    @GET("/repos/{owner}/{repo}/stargazers")
    suspend fun getNewStargazers(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int = 1,
        @Header("Accept") accept: String = "application/vnd.github.v3.star+json",
    ): List<StargazerCount>

    @GET("rate_limit")
    @Headers("Accept: application/vnd.github.v3+json")
    suspend fun getRateLimit(): Response<RateLimit>
}


data class SearchRepositoriesResponse(
    val items: List<com.example.githubstars.model.Response>
)

data class StargazerCount(
    val user: User,
    val starred_at: String,
) {
    data class User(
        val login: String,
        val html_url : String,
        val avatar_url: String,
    )
}

data class RepositoryStarsCount(
    val stargazers_count: Int
)


data class RateLimit(
    val rate: Rate,
    val resources: Resources
) {
    data class Rate(
        val limit: Int,
        val remaining: Int,
        val reset: Int
    )

    data class Resources(
        val core: Core
    ) {
        data class Core(
            val limit: Int,
            val remaining: Int,
            val reset: Int
        )
    }
}


