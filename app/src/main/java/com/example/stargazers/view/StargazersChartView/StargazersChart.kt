package com.example.githubstars.view.StargazersChartView

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine

import com.example.githubstars.model.StargazerCount
import com.example.stargazers.model.ChartType
import com.example.stargazers.view.StargazersChartView.StargazersList
import com.example.stargazers.view.StargazersChartView.StargazersViewModel


@Composable
fun StargazersChart(stargazersList: List<StargazerCount>, isFromGitHubApi: Boolean) {
    val viewModel: StargazersViewModel = viewModel()
    var stargazerCounts = viewModel.countStargazerPoints(stargazersList, viewModel.selectedView)
    if (stargazersList.isEmpty()) {
        Text(text = "Подождите...", fontSize = 20.sp, modifier = Modifier.padding(20.dp))
    }
    else if(viewModel.showStargazersList) {
        stargazerCounts = when (viewModel.selectedView) {
            ChartType.YEARLY -> viewModel.countStargazerPointOfYear(stargazersList)
            ChartType.MONTHLY -> viewModel.countStargazerPointOfMonth(stargazersList)
            ChartType.DAILY -> viewModel.countStargazerPointOfDay(stargazersList)
        }
        val date = stargazerCounts[viewModel.selectedPoint].pointOfTime
        val stargazers = stargazersList.filter { viewModel.parseDate(it.starred_at, viewModel.selectedView) == date }
        Log.d("starred_at", stargazersList[viewModel.selectedPoint].starred_at)
        Log.d("date", date)
        StargazersList(stargazers, isFromGitHubApi)
    }
    else {
        stargazerCounts = when (viewModel.selectedView) {
            ChartType.YEARLY -> viewModel.countStargazerPointOfYear(stargazersList)
            ChartType.MONTHLY -> viewModel.countStargazerPointOfMonth(stargazersList)
            ChartType.DAILY -> viewModel.countStargazerPointOfDay(stargazersList)
        }
        val maxCount = stargazerCounts.maxByOrNull { it.count }?.count
        val minCount = stargazerCounts.minByOrNull { it.count }?.count
        val pointsData = viewModel.getPointList(stargazerCounts, viewModel.calculateDelta(stargazerCounts.maxOf { it.count }))
        var stepsY = 0
        if (maxCount != null) {
            if (maxCount > 10) {
                stepsY = 10
            }
            else {
                stepsY = maxCount - 1
            }
        }
        val stepsX = stargazerCounts.size - 1
        val xAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .steps(stepsX)
            .labelData { i -> stargazerCounts[i].pointOfTime }
            .labelAndAxisLinePadding(20.dp)
            .build()
        val yAxisData = AxisData.Builder()
            .axisStepSize(100.dp)
            .steps(stepsY)
            .labelData { i ->
                if (maxCount!! > 10) {
                    val yScale = (maxCount?.minus(minCount!!))?.div(10.00000)
                    ((i * yScale!!) + minCount!!).toInt().toString()
                }
                else {
                    (i + minCount!!).toString()
                }
            }
            .labelAndAxisLinePadding(20.dp)
            .build()
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.selectedView = ChartType.YEARLY },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Yearly")
                }
                Button(
                    onClick = { viewModel.selectedView = ChartType.MONTHLY },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Monthly")
                }
                Button(
                    onClick = { viewModel.selectedView = ChartType.DAILY },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Daily")
                }
            }

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                lineChartData = LineChartData(
                    linePlotData = LinePlotData(
                        lines = listOf(
                            Line(
                                dataPoints = pointsData,
                                LineStyle(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    lineType = LineType.SmoothCurve(isDotted = false)
                                ),
                                IntersectionPoint(
                                    color = MaterialTheme.colorScheme.tertiary
                                ),
                                SelectionHighlightPoint(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                ShadowUnderLine(
                                    alpha = 0.5f,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.inversePrimary,
                                            Color.Transparent
                                        )
                                    )
                                ),
                                SelectionHighlightPopUp()
                            )
                        ),
                    ),
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    gridLines = GridLines(color = MaterialTheme.colorScheme.outline),
                    backgroundColor = Color.White
                ),
                OnCLickPoint = { pointX ->
                    viewModel.onPointClick(pointX)
                }
            )
            Button(
                onClick = {
                    //showStargazers(stargazerCounts[selectedPoint].pointOfTime)
                    viewModel.showStargazersList = !viewModel.showStargazersList
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Показать старгейзеров")
            }


        }
    }
}










