package com.example.moviesearch.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.moviesearch.database.MovieRepository

class MovieViewModel  : ViewModel() {
    private val movieRepository = MovieRepository()

    fun getMoviesSearch(year: String, apiKey: String){
        movieRepository.setMovieList(year, apiKey)
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
}