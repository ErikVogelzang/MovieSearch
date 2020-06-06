package com.example.moviesearch.model

data class MovieItemSearch(
    val posterPath: String,
    val movieID: String,
    val title: String,
    var loading: Boolean
) {
    var hasBeenFetched = false
}