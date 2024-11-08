package com.example.stargazers.view.FavoritesRepositoriesView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.githubstars.model.db.DB

class FavoritesViewModelFactory(private val db: DB) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}