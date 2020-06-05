package com.example.moviesearch.model

data class MovieItemSearch(
    val posterPath: String,
    val movieID: String,
    var loading: Boolean
) {
    var hasBeenFetched = false
}