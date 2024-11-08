package com.example.stargazers.view.FavoritesRepositoriesView

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubstars.model.db.DB
import com.example.githubstars.model.tables.Repo
import com.example.githubstars.model.tables.Stargazer
import com.example.githubstars.model.tables.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class FavoritesViewModel(private val db: DB) : ViewModel() {
    val repositories = db.repositoryDao().getAllLiveData()
    val owners = db.userDao().getAll()
    val stargazers = db.stargazerDao().getAll()

    fun deleteRepository(repository: Repo) {
        viewModelScope.launch(Dispatchers.IO) {
            val ownerRepository = db.userDao().getById(repository.id_owner).first()
            if (ownerRepository != null) {
                deleteImage(ownerRepository.avatar)
            }
            repository.id?.let {
                db.stargazerDao().deleteByIdRepository(it)
                db.repositoryDao().deleteById(it)
            }
            db.userDao().deleteById(repository.id_owner)
        }
    }

    private fun deleteImage(imagePath: String) {
        val file = File(imagePath)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("Favorite Repositories", "Image deleted")
            } else {
                Log.d("Favorite Repositories", "Delete error")
            }
        } else {
            Log.d("Favorite Repositories", "Image not found")
        }
    }
}


