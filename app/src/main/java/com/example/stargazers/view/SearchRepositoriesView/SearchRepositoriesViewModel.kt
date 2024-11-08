package com.example.stargazers.view.SearchRepositoriesView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.http.HttpException
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.githubstars.model.Response
import com.example.githubstars.model.RetrofitInstance
import com.example.githubstars.model.StargazerCount
import com.example.githubstars.model.db.DB
import com.example.githubstars.model.tables.Repo
import com.example.githubstars.model.tables.Stargazer
import com.example.githubstars.model.tables.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SearchRepositoriesViewModel() : ViewModel() {
    val api = RetrofitInstance.api
    var query = mutableStateOf("")
        private set
    var repositories = mutableStateOf(emptyList<Response>())
        private set
    var remainingRequests = mutableStateOf(0)
        private set
    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> get() = _isSaving
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun updateQuery(newQuery: String) {
        query.value = newQuery
    }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun searchRepositories(query: String) {
        viewModelScope.launch {
            try {
                val response = api.searchRepositories("owner:$query")
                if (response.isSuccessful) {
                    repositories.value = response.body()?.items ?: emptyList()
                }
            } catch (e: HttpException) {
                // Handle error
            }
        }
    }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun fetchRemainingRequests() {
        viewModelScope.launch {
            try {
                val rateLimit = api.getRateLimit()
                remainingRequests.value = rateLimit.body()?.rate?.remaining ?: 0
            } catch (e: HttpException) {
                // Handle error
            }
        }
    }

    fun loadStargazers(owner: String, repo: String): List<StargazerCount> {
        val allStargazers = mutableListOf<StargazerCount>()
        var page = 1
        var hasMorePages = true
        while (hasMorePages) {
            val response = RetrofitInstance.api.getDateStargazers(owner, repo, page = page).execute()
            if (response.isSuccessful) {
                val stargazers = response.body() ?: emptyList()
                allStargazers.addAll(stargazers)
                hasMorePages = stargazers.size == 100
                page++
            } else {
                // Обработка ошибок
                println("Ошибка: ${response.errorBody()?.string()}")
                hasMorePages = false
            }
        }
        return allStargazers
    }

    suspend fun downloadImage(context: Context, imageUrl: String): String {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        return try {
            val result = (imageLoader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap
            val fileName = "${System.currentTimeMillis()}.png"
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyImages")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    fun saveRepository(context: Context, response: Response, db: DB) {
        _isSaving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val existingRepo = db.repositoryDao().getByUrl(response.html_url)
            if (existingRepo != null) {
                _isSaving.postValue(false)
                return@launch
            }
            val avatarPath = downloadImage(context, response.owner.avatar_url) ?: ""
            val ownerRepository = User(
                name = response.owner.login,
                url = response.owner.html_url,
                avatar = avatarPath
            )
            val ownerId = db.userDao().insert(ownerRepository)

            val repository = Repo(
                id_owner = ownerId.toInt(),
                url = response.html_url,
                name = response.name,
                description = response.description ?: ""
            )
            val repositoryId = db.repositoryDao().insert(repository)
            val stargazers = loadStargazers(ownerRepository.name, repository.name)
            stargazers.forEach { stargazer ->
                val userStargazer = User(
                    name = stargazer.user.login,
                    url = stargazer.user.html_url,
                    avatar = stargazer.user.avatar_url
                )
                val userId = db.userDao().insert(userStargazer)
                val newStargazer = Stargazer(
                    id_repo = repositoryId.toInt(),
                    id_user = userId,
                    starred_at = stargazer.starred_at
                )
                db.stargazerDao().insert(newStargazer)
            }

            _isSaving.postValue(false)
        }
    }
}