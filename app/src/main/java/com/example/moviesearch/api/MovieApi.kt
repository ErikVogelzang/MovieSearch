package com.example.moviesearch.api
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MovieApi {

    @GET(BASE_QUERY_SEARCH)
    fun getMoviesAPI(
        @Query(KEY_QUERY) apiKey: String,
        @Query(LANG_QUERY) language: String,
        @Query(SORT_QUERY) sortBy: String,
        @Query(ADULT_QUERY) adult: Boolean,
        @Query(VIDEO_QUERY) video: Boolean,
        @Query(PAGE_QUERY) page: Int,
        @Query(GENRES_QUERY) genres: String,
        @Query(YEAR_GTE_QUERY) yearGte: String,
        @Query(YEAR_LTE_QUERY) yearLte: String
    ): Call<MovieList>

    @GET
    fun getMovieDetailsAPI(
        @Url url: String,
        @Query(KEY_QUERY) apiKey: String,
        @Query(APPEND_QUERY) extra: String
    ): Call<MovieDetails>

    companion object {
        private const val BASE_QUERY_SEARCH = "movie?"
        private const val KEY_QUERY = "api_key"
        private const val LANG_QUERY = "language"
        private const val SORT_QUERY = "sort_by"
        private const val ADULT_QUERY = "include_adult"
        private const val VIDEO_QUERY = "include_video"
        private const val PAGE_QUERY = "page"
        private const val YEAR_GTE_QUERY = "primary_release_date.gte"
        private const val YEAR_LTE_QUERY = "primary_release_date.lte"
        private const val GENRES_QUERY = "with_genres"
        private const val APPEND_QUERY = "append_to_response"
    }
}