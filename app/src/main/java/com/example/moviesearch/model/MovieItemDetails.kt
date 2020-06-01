package com.example.moviesearch.model

data class MovieItemDetails (
    val posterPath: String,
    val backdropPath: String,
    val budget: String,
    val genres: String,
    val imdbID: String,
    val language: String,
    val title: String,
    val overview: String,
    val release: String,
    val revenue: String,
    val length: String,
    val rating: String,
    val trailer: String,
    val cast: String,
    var loading: Boolean
)