package com.example.moviesearch.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moviesearch.api.MovieApi
import com.example.moviesearch.api.MovieDetails
import com.example.moviesearch.api.MovieList
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieSaved
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList



class MovieRepository(context: Context) {

    //region Initialization

    private val movieList = MutableLiveData<List<MovieItemSearch>>()
    private val movieSavedList: LiveData<List<MovieSaved>>
    private var movieDetails = MutableLiveData<MovieItemDetails>()
    private val movieDao: MovieDao
    private var maxPages = 0

    init {
        val database = MovieRoomDatabase.getDatabase(context)
        movieDao = database!!.movieDao()
        movieSavedList = movieDao.getAllMovies()
    }

    //endregion

    //region API Functions

    fun getMoviesAPI(): LiveData<List<MovieItemSearch>> {
        return movieList
    }

    fun fetchMovieListAPI(genres: String, sortBy: String, apiKey: String, yearGte: String, yearLte: String, page: Int) {
        setMoviesLoadState()
        val moviesFound = ArrayList<MovieItemSearch>()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_SEARCH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val movieApi = retrofit.create(MovieApi::class.java)
        val jsonCall = movieApi.getMoviesAPI(apiKey, DEFAULT_LANG, sortBy, false, false, page, genres, yearGte, yearLte)
        jsonCall.enqueue(object: Callback<MovieList>{
            override fun onFailure(call: Call<MovieList>, t: Throwable) {
                movieList.value = null
            }

            override fun onResponse(call: Call<MovieList>, response: Response<MovieList>) {
                if (!response.isSuccessful) {
                    return
                }
                var responseBody = response.body()
                if (responseBody == null)
                    return
                val maxPages = responseBody.getAmountOfPagesAPI().toIntOrNull()
                if (maxPages != null)
                    this@MovieRepository.maxPages = maxPages
                else
                    this@MovieRepository.maxPages = 0
                var results = responseBody.getResultsAPI()
                for (i in 0 until results.size()) {
                    var result = results.get(i)?.asJsonObject
                    val posterPath = removeQuotes(result?.get(JSON_POSTER).toString())
                    val movieID = removeQuotes(result?.get(JSON_ID).toString())
                    val title = removeQuotes(result?.get(JSON_TITLE).toString())
                    moviesFound.add(MovieItemSearch(posterPath, movieID, title, false))
                }
                movieList.value = moviesFound
            }
        })
    }

    fun fetchMovieDetailsAPI(id: String, apiKey: String) {
        movieDetails.value = MovieItemDetails()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_DETAILS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val movieApi = retrofit.create(MovieApi::class.java)
        val jsonCall = movieApi.getMovieDetailsAPI(id, apiKey, EXTRA_APPEND_VAL)
        jsonCall.enqueue(object: Callback<MovieDetails>{
            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {

            }

            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (!response.isSuccessful) {
                    return
                }
                var results = response.body()
                val backdropPath = removeQuotes(results?.getBackdropPath())
                val budget = removeQuotes(results?.getBudget())
                val imdbID = removeQuotes(results?.getIMDBID())
                val language = removeQuotes(results?.getLanguage())
                val title = removeQuotes(results?.getTitle())
                val overview = removeQuotes(results?.getOverview())
                val release = removeQuotes(results?.getRelease())
                val revenue = removeQuotes(results?.getRevenue())
                val length = removeQuotes(results?.getRuntime())
                val rating = removeQuotes(results?.getRating())
                val trailerPath = findYoutubeTrailerKey(results?.getTrailers())
                val genres = getGenresFromArray(results?.getGenres())
                val cast = getCastFromArray(results?.getCast())
                movieDetails.value = MovieItemDetails(backdropPath, budget, genres, imdbID, language, title, overview, release, revenue, length, rating, trailerPath, cast, false)
            }
        })
    }

    private fun removeQuotes(str: String?): String {
        if (str == null)
            return Common.STRING_EMPTY
        return str.replace(STRING_QUOTE, Common.STRING_EMPTY)
    }

    private fun setMoviesLoadState() {
        val moviesLoadState = ArrayList<MovieItemSearch>()
        for (i in MOVIE_START_COUNTER..DEFAULT_PAGE_MOVIES) {
            moviesLoadState.add(MovieItemSearch())
        }
        movieList.value = moviesLoadState
    }

    private fun findYoutubeTrailerKey(list: JsonArray?): String {
        if (list == null)
            return Common.STRING_EMPTY

        for (i in 0 until list.size()) {
            if (
                removeQuotes(list.get(i).asJsonObject.get("site").asString) == "YouTube"
                && removeQuotes(list.get(i).asJsonObject.get("type").asString) == "Trailer"
            )
                return removeQuotes(list.get(i).asJsonObject.get("key").asString)
        }
        return Common.STRING_EMPTY
    }

    private fun getGenresFromArray(list: JsonArray?): String {
        if (list == null)
            return Common.STRING_EMPTY
        var genreList = Common.STRING_EMPTY
        for (i in 0 until list.size()) {
            if (i > 0)
                genreList = genreList.plus(", ")
            genreList = genreList.plus(removeQuotes(list.get(i).asJsonObject.get("name").asString))
        }
        return genreList
    }

    private fun getCastFromArray(list: JsonArray?): String {
        if (list == null)
            return Common.STRING_EMPTY
        var castList = Common.STRING_EMPTY
        for (i in 0 until list.size()) {
            if (i > 0)
                castList = castList.plus(", ")
            castList = castList.plus(removeQuotes(list.get(i).asJsonObject.get("name").asString))
        }
        return castList
    }

    //endregion

    //region Database Functions

    fun fetchMovieDetailsLocal(id: String) {
        if (movieSavedList.value == null)
            return
        movieDetails.value = MovieItemDetails()
        for (movie in movieSavedList.value!!.iterator()) {
            if (movie.movieID == id) {
                movieDetails.value = MovieItemDetails(
                    movie.backdropPath, movie.budget, movie.genres, movie.imdbID, movie.language,
                    movie.title, movie.overview, movie.release, movie.revenue, movie.length,
                    movie.rating, movie.trailer, movie.cast, true)
            }
        }
    }

    fun isMovieLocal(id: String) : Boolean {
        if (movieSavedList.value == null)
            return false
        var local = false
        for (movie in movieSavedList.value!!.iterator()) {
            if (id == movie.movieID)
                local = true
        }
        return local
    }

    fun getMoviesLocal(): LiveData<List<MovieSaved>> = movieSavedList

    suspend fun saveMovieLocal(movie: MovieSaved) = movieDao.insertMovie(movie)

    suspend fun deleteMoviesLocal() = movieDao.deleteAllMovies()

    suspend fun deleteMovieLocal(movie: MovieSaved) = movieDao.deleteMovie(movie)

    //endregion

    //region Functions For Both Data Sources

    fun getMovieDetails(): LiveData<MovieItemDetails> {
        return movieDetails
    }

    fun getMaxPages(): Int = maxPages

    //endregion

    companion object {
        private const val BASE_URL_SEARCH = "https://api.themoviedb.org/3/discover/"
        private const val BASE_URL_DETAILS = "https://api.themoviedb.org/3/movie/"
        private const val STRING_QUOTE = "\""
        private const val DEFAULT_LANG = "en-US"
        private const val EXTRA_APPEND_VAL = "videos,credits"
        private const val DEFAULT_PAGE_MOVIES = 20
        private const val MOVIE_START_COUNTER = 1
        private const val JSON_POSTER = "poster_path"
        private const val JSON_ID = "id"
        private const val JSON_TITLE = "title"
    }
}