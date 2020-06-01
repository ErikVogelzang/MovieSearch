package com.example.moviesearch.common
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class Common {
    companion object {
        const val PLACEHOLDER_TEXT = "placeholder"
        var lastYear = PLACEHOLDER_TEXT
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
        const val YEAR_QUERY = "year"
        const val APPEND_QUERRY = "append_to_response"
        const val STRING_EMPTY = ""
        const val STRING_DOT = "."
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
    }
}