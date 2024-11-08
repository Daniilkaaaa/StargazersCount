package com.example.stargazers.view.StargazersChartView

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.room.InvalidationTracker
import co.yml.charts.common.extensions.isNotNull
import com.example.githubstars.model.StargazerCount
import com.example.githubstars.model.db.DB
import com.example.githubstars.view.StargazersChartView.StargazersChart
import kotlinx.coroutines.flow.Flow

@Composable
fun StargazerrsChartDB(repositoryId: Int, db: DB) {
    val repositories by db.repositoryDao().getAllLiveData().observeAsState(initial = emptyList())
    val users by db.userDao().getAll().observeAsState(initial = emptyList())
    val stargazers by db.stargazerDao().getAll().observeAsState(initial = emptyList())
    val stargazersRepository = stargazers.filter { it.id_repo == repositoryId }
    var stargazersCount by remember { mutableStateOf<List<StargazerCount>>(emptyList()) }
    if (repositories.isNotEmpty()) {
        val updatedStargazersCount = mutableListOf<StargazerCount>()
        for (stargazer in stargazersRepository) {
            val userStargazer = users.find { it.id == stargazer.id_user.toInt() }
            val user = userStargazer?.let {
                StargazerCount.User(
                    login = userStargazer.name,
                    html_url = it.url,
                    avatar_url = userStargazer.avatar
                )
            }
            val stargazerCount = user?.let { StargazerCount(user = it, starred_at = stargazer.starred_at) }
            if (stargazerCount != null) {
                updatedStargazersCount.add(stargazerCount)
            }
        }
        fun showStargazers(date: String) {

        }

        stargazersCount = updatedStargazersCount
        StargazersChart(stargazersCount, false)
//            showStargazers = { date ->
//                showStargazers(date)
//            })
    }
}