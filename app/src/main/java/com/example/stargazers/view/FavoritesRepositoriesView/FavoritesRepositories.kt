package com.example.githubstars.view.FavoritesRepositoriesView

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.githubstars.model.db.DB
import com.example.githubstars.model.tables.Repo
import com.example.stargazers.view.FavoritesRepositoriesView.FavoritesViewModel
import com.example.stargazers.view.FavoritesRepositoriesView.FavoritesViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FavoritesRepositories(db: DB, navController: NavController) {
    val viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModelFactory(db))
    val repositories by viewModel.repositories.observeAsState(initial = emptyList())
    val owners by viewModel.owners.observeAsState(initial = emptyList())
    val stargazers by viewModel.stargazers.observeAsState(initial = emptyList())

    LazyColumn {
        items(repositories) { repository ->
            val ownerRepository = owners.firstOrNull { it.id == repository.id_owner }
            val stargazersRepo = stargazers.filter { it.id_repo == repository.id }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clickable {
                        navController.navigate("chart_screen_from_db/${repository.id.toString()}")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column {
                    Row {
                        if (ownerRepository != null) {
                            DisplayImage(imagePath = ownerRepository.avatar)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                viewModel.deleteRepository(repository)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Удалить"
                            )
                        }
                    }
                    Text(text = repository.name, fontSize = 16.sp, modifier = Modifier.padding(10.dp))
                    Text(text = stargazersRepo.size.toString(), fontSize = 16.sp, modifier = Modifier.padding(10.dp))
                    Text(
                        text = repository.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayImage(imagePath: String) {
    Log.d("DisplayImage", "Image path: $imagePath")
    val bitmap = BitmapFactory.decodeFile(imagePath)
    if (bitmap != null) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.height(100.dp).width(100.dp).padding(10.dp))
    } else {
        Text("Image not found")
    }
}
