package com.example.moviesearch.model

import com.example.moviesearch.common.Common

data class MovieItemSearch(
    val posterPath: String = Common.STRING_EMPTY,
    val movieID: String = Common.STRING_EMPTY,
    val title: String = Common.STRING_EMPTY,
    var loading: Boolean = true
) {
    var hasBeenFetched = false
}