package com.example.stargazers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.githubstars.model.RetrofitInstance
import com.example.githubstars.model.RetrofitInstance.api
import com.example.githubstars.model.StargazerCount
import com.example.githubstars.model.db.DB
import com.example.githubstars.model.tables.Repo
import com.example.githubstars.model.tables.Stargazer
import com.example.githubstars.model.tables.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

class StargazersReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            updateStargazers(context)
        } catch (ex: Exception) {
            Log.d("Receive Ex", "onReceive: ${ex.printStackTrace()}")
        }
    }
    private fun showNotification(context: Context, title: String, message: String, idRepo:Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "stargazers_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Stargazers Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("idRepo", idRepo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            idRepo,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notificationManager.notify(idRepo, notificationBuilder.build())

    }
    fun updateStargazers(context: Context) {
        val db = DB.getDatabase(context)
        val repositoriesFlow: Flow<List<Repo>> = db.repositoryDao().getAllFlow()

        CoroutineScope(Dispatchers.IO).launch {
            repositoriesFlow.collect { repositories ->
                for (repository in repositories) {
                    launch {
                        Log.d("dfvgb", repository.name)
                        val ownerRepo: User = db.userDao().getById(repository.id_owner).first()
                        val stargazerCount = RetrofitInstance.api.getCountStargazers(ownerRepo.name, repository.name).stargazers_count
                        Log.d("fvdfb", "Stargazer count for ${repository.name}: $stargazerCount")
                        val stargazersFlow: Flow<List<Stargazer>>? = repository.id?.let { db.stargazerDao().getByIdRepository(it) }
                        if (stargazersFlow == null) {
                            Log.d("cdfv", "null")
                        } else {
                            stargazersFlow.collect { stargazers ->
                                val countStargazersDB = stargazers.size
                                if (countStargazersDB == stargazerCount) {
                                    Log.d("dfvgb", "No update")
                                } else {
                                    Log.d("csvd", "123")
                                    var response: Response<List<StargazerCount>>
                                    val elementsToTake = stargazerCount - countStargazersDB
                                    var stargazersFromGirHub = mutableListOf<StargazerCount>()
                                    var page = stargazerCount / 100 + 1
                                    val perPage = stargazerCount % 100
                                    if (perPage < elementsToTake) {
                                        response = api.getDateStargazers(
                                            ownerRepo.name,
                                            repository.name,
                                            page = page - 1,
                                            perPage = 100
                                        ).execute()
                                        if (response.isSuccessful) {
                                            val stargazers = response.body() ?: emptyList()
                                            stargazersFromGirHub.addAll(
                                                stargazers.takeLast(
                                                    elementsToTake - perPage
                                                )
                                            )
                                        }
                                    }
                                    response = api.getDateStargazers(
                                        ownerRepo.name,
                                        repository.name,
                                        page = page,
                                        perPage = perPage
                                    ).execute()
                                    if (response.isSuccessful) {
                                        val stargazers = response.body() ?: emptyList()
                                        stargazersFromGirHub.addAll(stargazers)
                                    } else {
                                        // Обработка ошибок
                                        println("Ошибка: ${response.errorBody()?.string()}")
                                    }
                                    var stargazersFromGirHubSorted = stargazersFromGirHub.sortedBy { it.starred_at }
                                    if (elementsToTake > 0) {
                                        stargazersFromGirHubSorted = stargazersFromGirHub.takeLast(elementsToTake)
                                    } else {
                                        stargazersFromGirHubSorted = emptyList()
                                    }
                                    for (stargazer in stargazersFromGirHubSorted) {
                                        Log.d("name user", stargazer.user.login)
                                        val idUser  = db.userDao().insert(
                                            User(
                                                name = stargazer.user.login,
                                                url = stargazer.user.html_url,
                                                avatar = stargazer.user.avatar_url,
                                            )
                                        )
                                        db.stargazerDao().insert(
                                            Stargazer(
                                                id_user = idUser ,
                                                id_repo = repository.id,
                                                starred_at = stargazer.starred_at
                                            )
                                        )
                                    }
                                    showNotification(context, "Stargazers Updated", "Updated stargazers for repository: ${repository.name}", repository.id)

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
