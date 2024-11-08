package com.example.githubstars.view.SearchRepositoriesView

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.githubstars.model.db.DB
import com.example.stargazers.NetworkUtil
import com.example.stargazers.view.SearchRepositoriesView.SearchRepositoriesViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRepositories(db: DB, navController: NavHostController, context: Context) {
    val networkUtil = remember { NetworkUtil(context) }
    val viewModel: SearchRepositoriesViewModel = viewModel() // Get the ViewModel
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (networkUtil.isNetworkAvailable()) {
            viewModel.fetchRemainingRequests()
        }
    }
    Column {
        Row {
            Text(
                text = "Избранное",
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(20.dp, 10.dp, 0.dp, 0.dp).clickable {
                    navController.navigate("favorites_repositories")
                }
            )
            Spacer(modifier = Modifier.width(60.dp))
            Text(
                modifier = Modifier.padding(20.dp, 10.dp, 0.dp, 0.dp),
                text = "Осталось запросов: ${viewModel.remainingRequests.value}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                query = viewModel.query.value,
                onQueryChange = { text ->
                    viewModel.updateQuery(text)
                },
                onSearch = {
                    if (networkUtil.isNetworkAvailable()) {
                        viewModel.searchRepositories(viewModel.query.value)
                    }
                },
                placeholder = {
                    Text("Поиск")
                },
                active = false,
                onActiveChange = {}
            ) {}
        }
        RepositoriesList(db, viewModel, navController)
    }
}
