package com.example.githubstars.view.SearchRepositoriesView
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.example.githubstars.model.db.DB
import com.example.stargazers.view.SearchRepositoriesView.SearchRepositoriesViewModel
import java.time.Instant
import java.time.ZoneId


@Composable
fun RepositoriesList(
    db: DB,
    viewModel: SearchRepositoriesViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val isSaving by viewModel.isSaving.observeAsState(false)
    val responses = viewModel.repositories.value

    LazyColumn {
        items(responses) { response ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .clickable {
                        navController.navigate("chart_screen_from_internet/${response.owner.login}/${response.name}")
                    }
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                ),
            ) {
                Column {
                    Row {
                        GlideImage(response.owner.avatar_url, modifier = Modifier.padding(10.dp, 10.dp))
                        Text(text = response.name, fontSize = 24.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                    if (!response.description.isNullOrEmpty()) {
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = response.description,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Text(text = "Starred at: ${Instant.parse(response.created_at).atZone(ZoneId.of("UTC")).toLocalDate()}", fontSize = 16.sp, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
                    Text(text = response.stargazers_count.toString() + " stars", fontSize = 16.sp, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
                    Text(text = "В избранное", fontSize = 25.sp, modifier = Modifier.padding(start = 10.dp, top = 5.dp).clickable {
                        viewModel.saveRepository(context, response, db)
                    })
                }
            }
        }
    }

    if (isSaving) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text(text = "Сохранение") },
            text = { Text(text = "Идет сохранение репозитория...") },
            confirmButton = {
                Button(onClick = {  }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
    fun GlideImage(url: String, modifier: Modifier = Modifier) {
        AndroidView(
            factory = { context: Context ->
                ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(100, 100) // Задайте нужные размеры
                    Glide.with(context)
                        .load(url)
                        .into(this)
                }
            },
            modifier = modifier
        )
    }