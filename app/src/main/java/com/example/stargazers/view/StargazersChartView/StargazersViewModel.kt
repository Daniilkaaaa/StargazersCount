package com.example.stargazers.view.StargazersChartView

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.yml.charts.common.model.Point
import com.example.githubstars.model.StargazerCount
import com.example.stargazers.model.ChartType
import com.example.stargazers.model.StargazerCountByPointOfTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException

class StargazersViewModel : ViewModel() {
    var selectedView by mutableStateOf(ChartType.YEARLY)
    var showStargazersList by mutableStateOf(false)
    var selectedPoint by mutableStateOf(0)
    // Function to handle stargazer counting logic
    fun countStargazerPoints(stargazers: List<StargazerCount>, selectedView: ChartType): List<StargazerCountByPointOfTime> {
        return when (selectedView) {
            ChartType.YEARLY -> countStargazerPointOfYear(stargazers)
            ChartType.MONTHLY -> countStargazerPointOfMonth(stargazers)
            ChartType.DAILY -> countStargazerPointOfDay(stargazers)
        }
    }
    fun countStargazerPointOfYear(stargazers: List<StargazerCount>): List<StargazerCountByPointOfTime> {
        val yearMap = mutableMapOf<Int, Int>()

        for (stargazer in stargazers) {
            try {
                val instant = Instant.parse(stargazer.starred_at)
                val year = instant.atZone(ZoneId.of("UTC")).year
                yearMap[year] = yearMap.getOrDefault(year, 0) + 1
            } catch (e: DateTimeParseException) {
                println("Error parsing date: ${e.message}")
            }
        }

        return yearMap.map { (year, count) -> StargazerCountByPointOfTime(year.toString(), count) }
    }
    fun countStargazerPointOfMonth(stargazers: List<StargazerCount>): List<StargazerCountByPointOfTime> {
        val monthMap = mutableMapOf<String, Int>()

        for (stargazer in stargazers) {
            try {
                val instant = Instant.parse(stargazer.starred_at)
                val monthYear = instant.atZone(ZoneId.of("UTC")).let {
                    "${it.month}-${it.year}" // Форматируем как "Месяц-Год"
                }

                monthMap[monthYear] = monthMap.getOrDefault(monthYear, 0) + 1
            } catch (e: DateTimeParseException) {
                println("Error parsing date: ${e.message}")
            }
        }

        return monthMap.map { (monthYear, count) -> StargazerCountByPointOfTime(monthYear, count) }
    }
    fun countStargazerPointOfDay(stargazers: List<StargazerCount>): List<StargazerCountByPointOfTime> {
        val dayMap = mutableMapOf<String, Int>()

        for (stargazer in stargazers) {
            try {
                val instant = Instant.parse(stargazer.starred_at)
                val date = instant.atZone(ZoneId.of("UTC")).toLocalDate() // Получаем LocalDate
                val dateString = date.toString() // Форматируем как "YYYY-MM-DD"

                dayMap[dateString] = dayMap.getOrDefault(dateString, 0) + 1
            } catch (e: DateTimeParseException) {
                println("Error parsing date: ${e.message}")
            }
        }

        return dayMap.map { (dateString, count) -> StargazerCountByPointOfTime(dateString, count) }
    }
    fun parseDate(starred_at: String, selectedView: ChartType): String {
        val instant = Instant.parse(starred_at)
        var dateString = ""
        if (selectedView == ChartType.DAILY) {
            val date = instant.atZone(ZoneId.of("UTC")).toLocalDate() // Получаем LocalDate
            dateString = date.toString()
        }
        if (selectedView == ChartType.MONTHLY) {
            dateString = instant.atZone(ZoneId.of("UTC")).let {
                "${it.month}-${it.year}" // Форматируем как "Месяц-Год"
            }
        }
        if (selectedView == ChartType.YEARLY) {
            dateString = instant.atZone(ZoneId.of("UTC")).year.toString()
        }
        return dateString
    }
    fun getPointList(stargazersList: List<StargazerCountByPointOfTime>, delta: Int): List<Point> {
        val pointslist = ArrayList<Point>()
        for (i in 0..stargazersList.size - 1) {
            pointslist.add(
                Point(
                    i.toFloat(),
                    (stargazersList[i].count).toFloat()
                )
            )
        }
        if (stargazersList.isEmpty()) {
            return emptyList()
        }
        else {
            return pointslist
        }
    }
    fun calculateDelta(maxYValue: Int): Int {
        if (maxYValue < 10) {
            return 1
        } else if (maxYValue < 100) {
            return 10
        } else if (maxYValue < 1000) {
            return 100
        } else if (maxYValue < 10000) {
            return 1000
        }
        else if (maxYValue < 100000) {
            return 10000
        }
        else if (maxYValue < 1000000) {
            return 100000
        }
        else {
            return 1
        }
    }
    fun onPointClick(pointX: Int) {
        selectedPoint = pointX
    }
    fun toggleShowStargazersList() {
        showStargazersList = !showStargazersList
    }
}