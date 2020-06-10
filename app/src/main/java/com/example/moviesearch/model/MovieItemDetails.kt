package com.example.moviesearch.model

import com.example.moviesearch.common.Common


data class MovieItemDetails (
    val backdropPath: String = Common.STRING_EMPTY,
    val budget: String = Common.STRING_EMPTY,
    val genres: String = Common.STRING_EMPTY,
    val imdbID: String = Common.STRING_EMPTY,
    val language: String = Common.STRING_EMPTY,
    val title: String = Common.STRING_EMPTY,
    val overview: String = Common.STRING_EMPTY,
    val release: String = Common.STRING_EMPTY,
    val revenue: String = Common.STRING_EMPTY,
    val length: String = Common.STRING_EMPTY,
    val rating: String = Common.STRING_EMPTY,
    val trailer: String = Common.STRING_EMPTY,
    val cast: String = Common.STRING_EMPTY,
    var loading: Boolean = true
)