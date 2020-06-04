package com.example.moviesearch.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moviesearch.api.MovieApi
import com.example.moviesearch.api.MovieDetails
import com.example.moviesearch.api.MovieList
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieItemSearch
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList



class MovieRepository {

    private val movieList = MutableLiveData<List<MovieItemSearch>>()
    private var movieDetails = MutableLiveData<MovieItemDetails>()
    private var categoryPrimary = MutableLiveData<String>()
    private var categorySecondary = MutableLiveData<String>()
    private var sortBy = MutableLiveData<String>()
    private fun removeQuotes(str: String): String {
        return str.replace(Common.STRING_QUOTE, Common.STRING_EMPTY)
    }

    fun getMovieList(): LiveData<List<MovieItemSearch>> {
        return movieList
    }

    fun getMovieDetails(): LiveData<MovieItemDetails> {
        return movieDetails
    }

    fun getCategoryPrimary(): LiveData<String> {
        return categoryPrimary
    }

    fun setCategoryPrimary(value: String) {
        categoryPrimary.value = value
    }

    fun getCategorySecondary(): LiveData<String> {
        return categorySecondary
    }

    fun setCategorySecondary(value: String) {
        categorySecondary.value = value
    }

    fun getsortBy(): LiveData<String> {
        return sortBy
    }

    fun setSortBy(value: String) {
        sortBy.value = value
    }

    private fun setMoviesLoadState() {
        val moviesLoadState = ArrayList<MovieItemSearch>()
        for (i in Common.MOVIE_START_COUNTER..Common.DEFAULT_PAGE_MOVIES) {
            moviesLoadState.add(MovieItemSearch(i.toString(), i.toString(), true))
        }
        movieList.value = moviesLoadState
    }

    fun setMovieList(year: String, apiKey: String) {
        setMoviesLoadState()

        val moviesFound = ArrayList<MovieItemSearch>()

        val retrofit = Retrofit.Builder()
            .baseUrl(Common.BASE_URL_SEARCH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val movieApi = retrofit.create(MovieApi::class.java)
        val jsonCall = movieApi.getMovies(apiKey, Common.DEFAULT_LANG, Common.DEFAULT_SORT, false, false, Common.DEFAULT_PAGE, year)
        jsonCall.enqueue(object: Callback<MovieList>{
            override fun onFailure(call: Call<MovieList>, t: Throwable) {
            }

            override fun onResponse(call: Call<MovieList>, response: Response<MovieList>) {
                if (!response.isSuccessful) {
                    return
                }
                var results = response.body()?.getResults()
                for (i in 0 until results?.size()!!) {
                    var result = results?.get(i)?.asJsonObject
                    val posterPath = removeQuotes(result?.get(Common.JSON_POSTER).toString())
                    val movieID = removeQuotes(result?.get(Common.JSON_ID).toString())

                    moviesFound.add(MovieItemSearch(posterPath, movieID, false))
                }
                movieList.value = moviesFound
            }
        })
    }

    fun setMovieDetails(id: String, apiKey: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Common.BASE_URL_DETAILS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val movieApi = retrofit.create(MovieApi::class.java)
        val jsonCall = movieApi.getMovieDetails(id, apiKey, Common.EXTRA_APPEND_VAL)
        jsonCall.enqueue(object: Callback<MovieDetails>{
            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Log.i("RESPONSE", t.message)
            }

            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (!response.isSuccessful) {
                    return
                }
                var results = response.body()
                val posterPath = removeQuotes(results!!.getPosterPath())
                val backdropPath = removeQuotes(results.getBackdropPath())
                val budget = removeQuotes(results.getBudget())
                val imdbID = removeQuotes(results.getIMDBID())
                val language = removeQuotes(results.getLanguage())
                val title = removeQuotes(results.getTitle())
                val overview = removeQuotes(results.getOverview())
                val release = removeQuotes(results.getRelease())
                val revenue = removeQuotes(results.getRevenue())
                val length = removeQuotes(results.getRuntime())
                val rating = removeQuotes(results.getRating())
                val trailerPath = findYoutubeTrailerKey(results.getTrailers())
                val genres = getGenresFromArray(results.getGenres())
                val cast = getCastFromArray(results.getCast())
                movieDetails.value = MovieItemDetails(posterPath, backdropPath, budget, genres, imdbID, language, title, overview, release, revenue, length, rating, trailerPath, cast, false)
            }
        })
    }

    private fun findYoutubeTrailerKey(list: JsonArray): String {
        for (i in 0 until list.size()) {
            if (
                removeQuotes(list.get(i).asJsonObject.get("site").asString) == "YouTube"
                && removeQuotes(list.get(i).asJsonObject.get("type").asString) == "Trailer"
            )
                return removeQuotes(list.get(i).asJsonObject.get("key").asString)
        }
        return ""
    }

    private fun getGenresFromArray(list: JsonArray): String {
        var genreList = ""
        for (i in 0 until list.size()) {
            if (i > 0)
                genreList = genreList.plus(", ")
            genreList = genreList.plus(removeQuotes(list.get(i).asJsonObject.get("name").asString))
        }
        return genreList
    }

    private fun getCastFromArray(list: JsonArray): String {
        var castList = ""
        for (i in 0 until list.size()) {
            if (i > 0)
                castList = castList.plus(", ")
            castList = castList.plus(removeQuotes(list.get(i).asJsonObject.get("name").asString))
        }
        return castList
    }
}