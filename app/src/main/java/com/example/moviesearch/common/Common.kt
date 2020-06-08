package com.example.moviesearch.common
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.media.Image
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.moviesearch.R
import com.google.android.material.snackbar.Snackbar
import java.util.*

class Common {
    companion object {
        const val PLACEHOLDER_TEXT = "placeholder"
        var lastYear = PLACEHOLDER_TEXT
        var catSecSelected = 0
        var catPriSelected = 0
        var sortBySelected = 0
        var searchFragmentDestroyed = false
        var maxPages = 1
        var savedMoviesNum = 0
        const val DEFAULT_SPAN_COUNT = 1
        const val BASE_URL_SEARCH = "https://api.themoviedb.org/3/discover/"
        const val BASE_QUERY_SEARCH = "movie?"
        const val BASE_URL_DETAILS = "https://api.themoviedb.org/3/movie/"
        const val KEY_QUERY = "api_key"
        const val LANG_QUERY = "language"
        const val SORT_QUERY = "sort_by"
        const val ADULT_QUERY = "include_adult"
        const val VIDEO_QUERY = "include_video"
        const val PAGE_QUERY = "page"
        const val YEAR_GTE_QUERY = "primary_release_date.gte"
        const val YEAR_LTE_QUERY = "primary_release_date.lte"
        const val GENRES_QUERY = "with_genres"
        const val APPEND_QUERRY = "append_to_response"
        const val STRING_EMPTY = ""
        const val STRING_DOT = "."
        const val STRING_GT = ">"
        const val STRING_GTE = ">="
        const val STRING_LTE = "<="
        const val STRING_EQUAL = "="
        const val STRING_LT = "<"
        const val STRING_GTLT = "> <"
        const val STRING_GTELTE = ">= <="
        const val STRING_QUOTE = "\""
        const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500/"
        const val DEFAULT_LANG = "en-US"
        const val DEFAULT_SORT = "popularity.desc"
        const val EXTRA_APPEND_VAL = "videos,credits"
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_MOVIES = 20
        const val MOVIE_START_COUNTER = 1
        const val JSON_POSTER = "poster_path"
        const val JSON_ID = "id"
        const val JSON_TITLE = "title"
        const val YEAR_END = "-12-31"
        const val YEAR_START = "-01-01"



        fun checkForValidJSonReturn(value: String) : Boolean {
            return when(value){
                STRING_EMPTY -> false
                "0" -> false
                "null" -> false
                "0.0" -> false
                else -> true
            }
        }

        fun fetchImageGlide(
            context: Context,
            imageView: ImageView,
            progressBar: ProgressBar,
            path: String
        ) {
            Glide.with(context)
                .load(BASE_IMAGE_URL + path)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                })
                .into(imageView)
        }

        fun showUndoSnackbar(text: String, onUndo: () -> Unit, view: View, resources: Resources) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setActionTextColor(ResourcesCompat.getColor(resources, R.color.blue, null))
                .setAction(R.string.undo_text, View.OnClickListener {
                    onUndo()
                }).show()
        }
    }
}