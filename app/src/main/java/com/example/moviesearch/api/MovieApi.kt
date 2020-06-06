package com.example.moviesearch.api
import com.example.moviesearch.common.Common
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MovieApi {

    @GET(Common.BASE_QUERY_SEARCH)
    fun getMovies(
        @Query(Common.KEY_QUERY) apiKey: String,
        @Query(Common.LANG_QUERY) language: String,
        @Query(Common.SORT_QUERY) sortBy: String,
        @Query(Common.ADULT_QUERY) adult: Boolean,
        @Query(Common.VIDEO_QUERY) video: Boolean,
        @Query(Common.PAGE_QUERY) page: Int,
        @Query(Common.GENRES_QUERY) genres: String,
        @Query(Common.YEAR_GTE_QUERY) yearGte: String,
        @Query(Common.YEAR_LTE_QUERY) yearLte: String
    ): Call<MovieList>

    @GET
    fun getMovieDetails(
        @Url url: String,
        @Query(Common.KEY_QUERY) apiKey: String,
        @Query(Common.APPEND_QUERRY) extra: String
    ): Call<MovieDetails>
}