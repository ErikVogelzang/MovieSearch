package com.example.moviesearch.api

import com.google.gson.JsonArray

class MovieList(
    private var results: JsonArray,
    private var total_pages: String
) {
    fun getResults(): JsonArray = results
    fun getAmountOfPages(): String = total_pages
}