package com.example.stargazers.view.StargazersChartView

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.githubstars.model.RetrofitInstance.api
import com.example.githubstars.model.StargazerCount
import com.example.githubstars.view.StargazersChartView.StargazersChart
import kotlinx.coroutines.delay
import retrofit2.awaitResponse

@Composable
fun StargazersChartInternet(owner: String, repo: String) {
    var stargazers by remember { mutableStateOf<List<StargazerCount>>(emptyList()) }
    LaunchedEffect(owner, repo) {
        if (owner != null && repo != null) {
            stargazers = fetchStargazers(owner, repo)
        }
    }

    StargazersChart(stargazers, true)
}

suspend fun fetchStargazers(owner: String, repo: String): List<StargazerCount> {
    var currentPage = 1
    val stargazersList = mutableListOf<StargazerCount>()

    do {
        val response = api.getDateStargazers(owner, repo, perPage = 100, page = currentPage).awaitResponse()
        if (response.isSuccessful) {
            stargazersList.addAll(response.body() ?: emptyList())
            currentPage++
            delay(2000) // 1 second delay
        } else {
            Log.e("StargazersChart", "Failed to get stargazers. Code: ${response.code()}")
            if (response.code() == 429 || response.code() == 403) {
                val waitTime = response.headers()["Retry-After"]?.toLongOrNull() ?: 60
                delay(waitTime * 1000)
                continue
            } else {
                break
            }
        }
    } while (response.body()?.isNotEmpty() == true)
    Log.d("dvdf", stargazersList.size.toString())
    return stargazersList
}