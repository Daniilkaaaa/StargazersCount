package com.example.stargazers.view.StargazersChartView

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.example.githubstars.model.StargazerCount
import com.example.stargazers.presenter.StargazersChartPresenter
import java.time.Instant
import java.time.ZoneId

@Composable
fun StargazersList(stargazers: List<StargazerCount>, isFromGitHubApi: Boolean) {
    LazyColumn {
        items(stargazers) { stargazer ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row {
                    Column {
                        Row() {
                            if (isFromGitHubApi) {
                                ImageFromGitHubApi(stargazer.user.avatar_url, modifier = Modifier.padding(10.dp, 10.dp))
                            }
//                            else {
//                                ImageFromBD(stargazer.user.avatar_url, modifier = Modifier.padding(10.dp, 10.dp))
//                            }
                            Text(text = stargazer.user.login, fontSize = 16.sp, modifier = Modifier.padding(start = 10.dp,top = 10.dp))
                        }
                        Text(text = "Starred at: ${Instant.parse(stargazer.starred_at).atZone(ZoneId.of("UTC")).toLocalDate()}", fontSize = 16.sp, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ImageFromGitHubApi(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context: Context ->
            ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(100, 100)
                Glide.with(context)
                    .load(url)
                    .into(this)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ImageFromBD(imagePath: String, modifier: Modifier = Modifier) {
    Log.d("DisplayImage", "Image path: $imagePath")
    val bitmap = BitmapFactory.decodeFile(imagePath)
    if (bitmap != null) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = modifier.height(100.dp).width(100.dp))
    } else {
        Text("Image not found")
    }
}