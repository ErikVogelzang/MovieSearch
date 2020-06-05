package com.example.moviesearch.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.moviesearch.database.MovieRepository

class MovieViewModel  : ViewModel() {
    private val movieRepository = MovieRepository()

    fun getMoviesSearch(genres: String, sortBy: String, apiKey: String){
        movieRepository.setMovieList(genres, sortBy, apiKey)
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
}