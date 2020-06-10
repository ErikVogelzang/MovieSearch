package com.example.moviesearch.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "movieTable")
data class MovieSaved (
    @PrimaryKey
    val movieID: String,
    val posterPath: String,
    val backdropPath: String,
    val budget: String,
    val genres: String,
    val imdbID: String,
    val language: String,
    val title: String,
    val overview: String,
    val release: String,
    val revenue: String,
    val length: String,
    val rating: String,
    val trailer: String,
    val cast: String
) : Parcelable