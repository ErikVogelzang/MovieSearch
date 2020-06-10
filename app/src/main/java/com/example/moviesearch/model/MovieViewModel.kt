package com.example.moviesearch.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.moviesearch.api.MovieDetails
import com.example.moviesearch.common.Common
import com.example.moviesearch.database.MovieRepository
import com.example.moviesearch.ui.MovieSearchAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    //region Initialization

    private val movieRepository = MovieRepository(application.applicationContext)
    private var movieChangedList = arrayListOf<MovieSaved>()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var movieSavedList = movieRepository.getMoviesLocal()

    //endregion

    //region API Functions

    fun fetchMoviesAPI(genres: String, sortBy: String, apiKey: String, yearGte: String, yearLte: String, page: Int){
        movieRepository.fetchMovieListAPI(genres, sortBy, apiKey, yearGte, yearLte, page)
    }

    fun fetchMovieDetailsAPI(id: String, apiKey: String){
        movieRepository.fetchMovieDetailsAPI(id, apiKey)
    }

    fun getMoviesApi(): LiveData<List<MovieItemSearch>> {
        return movieRepository.getMoviesAPI()
    }

    //endregion

    //region Database functions

    fun saveMovieLocal(movie: MovieSaved, undo: Boolean = false) {
        mainScope.launch {
            if (!undo) {
                movieChangedList.clear()
                movieChangedList.add(movie)
                lastAction = ACTION_SAVE_ONE
            }
            withContext(Dispatchers.IO) {
                movieRepository.saveMovieLocal(movie)
            }
        }
    }

    fun deleteMoviesLocal(undo: Boolean = false) {
        if (!undo) {
            movieChangedList.clear()
            for (movieSaved in movieSavedList.value!!.iterator()) {
                movieChangedList.add(movieSaved)
            }
            lastAction = ACTION_DELETE_ALL
        }
        mainScope.launch {
            withContext(Dispatchers.IO) {
                movieRepository.deleteMoviesLocal()
            }
        }
    }

    fun deleteMovieLocal(movie: MovieSaved, undo: Boolean = false) {
        if (!undo) {
            movieChangedList.clear()
            for (movieSaved in movieSavedList.value!!.iterator()) {
                movieChangedList.add(movieSaved)
            }
            lastAction = ACTION_DELETE_ONE
        }
        mainScope.launch {
            withContext(Dispatchers.IO) {
                movieRepository.deleteMovieLocal(movie)
            }
        }
    }

    fun getMoviesLocal(): LiveData<List<MovieSaved>> = movieSavedList

    fun fetchMovieDetailsLocal(id: String) = movieRepository.fetchMovieDetailsLocal(id)

    fun isMovieLocal(id: String): Boolean = movieRepository.isMovieLocal(id)

    //endregion

    //region Functions For Both Data Sources

    fun getMovieDetails(): LiveData<MovieItemDetails> {
        return movieRepository.getMovieDetails()
    }

    fun getMaxPages(): Int = movieRepository.getMaxPages()

    fun restoreLastAction() {
        when(lastAction) {
            ACTION_SAVE_ONE -> deleteMovieLocal(movieChangedList[Common.ARRAY_FIRST], true)
            ACTION_DELETE_ONE -> {
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        movieRepository.deleteMoviesLocal()
                        for (movie in movieChangedList) {
                            movieRepository.saveMovieLocal(movie)
                        }
                    }
                }
            }
            ACTION_DELETE_ALL -> {
                for (movie in movieChangedList) {
                    saveMovieLocal(movie, true)
                }
            }
        }
    }

    companion object {
        private const val ACTION_NONE = "NONE"
        private const val ACTION_DELETE_ALL = "DEL_ALL"
        private const val ACTION_DELETE_ONE = "DEL_ONE"
        private const val ACTION_SAVE_ONE = "SAVE"
        private var lastAction = ACTION_NONE
    }

    //endregion
}