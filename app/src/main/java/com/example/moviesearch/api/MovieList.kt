package com.example.moviesearch.api

import com.google.gson.JsonArray

class MovieList(
    private var results: JsonArray
) {
    fun getResults(): JsonArray = results
}