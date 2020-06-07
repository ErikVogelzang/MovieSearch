package com.example.moviesearch.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.moviesearch.model.MovieSaved

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieSaved)

    @Query("SELECT * FROM movieTable")
    fun getAllMovies(): LiveData<List<MovieSaved>>

    @Query("DELETE FROM movieTable")
    suspend fun deleteAllMovies()

    @Delete
    suspend fun deleteMovie(movie: MovieSaved)

    @Query("SELECT * FROM movieTable WHERE movieID = :id")
    fun loadMovieWithID(id: String): LiveData<MovieSaved>
}