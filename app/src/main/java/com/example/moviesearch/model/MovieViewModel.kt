package com.example.moviesearch.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.moviesearch.api.MovieDetails
import com.example.moviesearch.common.Common
import com.example.moviesearch.database.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val movieRepository = MovieRepository(application.applicationContext)
    private var movieDeletedList = arrayListOf<MovieSaved>()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    val movieSavedList = movieRepository.getAllSavedMovies()

    fun getMoviesSearch(genres: String, sortBy: String, apiKey: String, yearGte: String, yearLte: String, page: Int){
        movieRepository.setMovieList(genres, sortBy, apiKey, yearGte, yearLte, page)
    }

    fun getMovieDetails(id: String, apiKey: String){
        movieRepository.setMovieDetails(id, apiKey)
    }

    fun getMovieListLiveData(): LiveData<List<MovieItemSearch>> {
        return movieRepository.getMovieList()
    }

    fun getMovieDetailsLiveData(): LiveData<MovieItemDetails> {
        return movieRepository.getMovieDetails()
    }

    fun clearMovieDetails() {
        movieRepository.clearMovieDetails()
    }

    fun saveMovie(movie: MovieSaved) {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                movieRepository.saveMovie(movie)
            }
        }
    }

    fun deleteAllSavedMovies() {
        movieDeletedList.clear()
        for (movie in movieSavedList.value!!.iterator()) {
            movieDeletedList.add(movie)
        }
        mainScope.launch {
            withContext(Dispatchers.IO) {
                movieRepository.deleteAllSavedMovies()
            }
        }
    }

    fun deleteSavedMovie(movie: MovieSaved) {
        movieDeletedList.clear()
        movieDeletedList.add(movie)
        mainScope.launch {
            withContext(Dispatchers.IO) {
                movieRepository.deleteSavedMovie(movie)
            }
        }
    }


    fun getAllSavedMovies(): LiveData<List<MovieSaved>> = movieSavedList

    fun getLastDeletedMovies(): List<MovieSaved> = movieDeletedList

    fun loadMovieWithID(id: String): LiveData<MovieSaved> = movieRepository.loadMovieWithID(id)
}