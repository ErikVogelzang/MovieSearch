package com.example.moviesearch.api

import com.google.gson.JsonArray

class MovieList(
    private var results: JsonArray,
    private var total_pages: String
) {
    fun getResultsAPI(): JsonArray = results
    fun getAmountOfPagesAPI(): String = total_pages
}