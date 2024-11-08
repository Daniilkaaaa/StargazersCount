package com.example.stargazers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.githubstars.model.db.DB
import com.example.githubstars.view.FavoritesRepositoriesView.FavoritesRepositories
import com.example.githubstars.view.SearchRepositoriesView.SearchRepositories
import com.example.stargazers.view.StargazersChartView.StargazerrsChartDB
import com.example.stargazers.view.StargazersChartView.StargazersChartInternet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    private lateinit var db: DB

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("RememberReturnType", "MissingSuperCall", "CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        db = DB.getDatabase(applicationContext)
        super.onCreate(savedInstanceState)
        startStargazersService()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = rememberPermissionState(
                    permission = android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (!permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                }
            }

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "search_repositories") {
                composable("search_repositories") {
                    SearchRepositories(db, navController, applicationContext)
                }

                composable("chart_screen_from_internet/{owner}/{repo}") {
                    val owner = it.arguments?.getString("owner")
                    val repo = it.arguments?.getString("repo")
                    if (owner != null) {
                        if (repo != null) {
                            StargazersChartInternet(owner, repo)
                        }
                    }
                }

                composable("favorites_repositories") {
                    FavoritesRepositories(db, navController)
                }

                composable("chart_screen_from_db/{id_repository}") {
                    val idRepository = it.arguments?.getString("id_repository")?.toInt()
                    if (idRepository != null) {
                        StargazerrsChartDB(idRepository, db)
                    }
                }
            }
            val idRepo = intent.getIntExtra("idRepo",0)
            if (idRepo != 0) {
                navController.navigate("chart_screen_from_db/${idRepo.toString()}")
            }

        }
    }
    private fun startStargazersService() {
        val serviceIntent = Intent(this, StargazersService::class.java)
        startService(serviceIntent)
    }
}








