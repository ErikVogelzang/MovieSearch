package com.example.moviesearch.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class MovieDetails (
    private var backdrop_path: String,
    private var budget: String,
    private var genres: JsonArray,
    private var imdb_id: String,
    private var original_language: String,
    private var title: String,
    private var overview: String,
    private var release_date: String,
    private var revenue: String,
    private var runtime: String,
    private var vote_average: String,
    private var videos: JsonObject,
    private var credits: JsonObject
) {
    fun getBackdropPath(): String = backdrop_path
    fun getBudget(): String = budget
    fun getGenres(): JsonArray = genres
    fun getIMDBID(): String = imdb_id
    fun getLanguage(): String = original_language
    fun getTitle(): String = title
    fun getOverview(): String = overview
    fun getRelease(): String = release_date
    fun getRevenue(): String = revenue
    fun getRuntime(): String = runtime
    fun getRating(): String = vote_average
    fun getTrailers(): JsonArray = videos.get(JSON_RESULTS).asJsonArray
    fun getCast(): JsonArray = credits.get(JSON_CAST).asJsonArray

    companion object {
        private const val JSON_RESULTS = "results"
        private const val JSON_CAST = "cast"
    }
}