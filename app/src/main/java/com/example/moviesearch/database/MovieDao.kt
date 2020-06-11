package com.example.moviesearch.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.moviesearch.model.MovieSaved

@Dao
interface MovieDao {
    //Saved a movie in the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieSaved)

    //Returns all the saved movies from the database.
    @Query("SELECT * FROM movieTable")
    fun getAllMovies(): LiveData<List<MovieSaved>>

    //Deletes all the saved movies from the database.
    @Query("DELETE FROM movieTable")
    suspend fun deleteAllMovies()

    //Deletes a single movie from the database.
    @Delete
    suspend fun deleteMovie(movie: MovieSaved)

    //Returns a movie with the specified movieID from the database if it exists.
    @Query("SELECT * FROM movieTable WHERE movieID = :id")
    fun loadMovieWithID(id: String): LiveData<MovieSaved>
}