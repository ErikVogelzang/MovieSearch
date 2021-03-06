package com.example.moviesearch.common
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.moviesearch.R
import com.example.moviesearch.api.MovieApi
import com.example.moviesearch.api.MovieDetails
import com.example.moviesearch.database.MovieRepository
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieViewModel
import com.example.moviesearch.ui.MovieSearchAdapter
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//This class contains functions and constants that are used by multiple other classes.

class Common {
    companion object {
        const val STRING_EMPTY = ""
        private const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500/"
        private const val DEFAULT_GRID_SPAN_COUNT = 1
        const val ARRAY_FIRST = 0
        private const val JSON_NULL = "null"
        private const val JSON_BAD_INT = "0"
        private const val JSON_BAD_DOUBLE = "0.0"

        //Returns true if the info from a JSON file is usable.
        fun checkForValidJSonReturn(value: String) : Boolean {
            return when(value){
                STRING_EMPTY -> false
                JSON_BAD_INT -> false
                JSON_NULL -> false
                JSON_BAD_DOUBLE -> false
                else -> true
            }
        }

        //Fetches images using glide and sets them to an ImageView
        fun fetchImageGlide(
            context: Context,
            imageView: ImageView,
            progressBar: ProgressBar,
            path: String
        ) {
            Glide.with(context)
                .load(BASE_IMAGE_URL + path)
                .dontAnimate()
                .let {
                    request ->
                        if (imageView.drawable != null)
                            request.placeholder(imageView.drawable.constantState?.newDrawable()?.mutate())
                        else
                            request
                    request.listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            imageView.visibility = View.VISIBLE
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
                            imageView.visibility = View.VISIBLE
                            return false
                        }

                    })
                }.into(imageView)

        }

        //Functions related to undo deleting and save functionality.
        fun showUndoSnackbar(text: String, vm: MovieViewModel, view: View, resources: Resources)
                : Snackbar {
            val snack = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setActionTextColor(ResourcesCompat.getColor(resources, R.color.blue, null))
                .setAction(R.string.undo_text, View.OnClickListener {
                    onUndo(vm)
                })
            snack.show()
            return snack
        }

        private fun onUndo(vm: MovieViewModel) = vm.restoreLastAction()

        //Functions to initialize the apps RecyclerViews.
        fun setRVLayout(context: Context, rv: RecyclerView, res: Resources) {
            val gridLayoutManager = GridLayoutManager(context, DEFAULT_GRID_SPAN_COUNT,
                RecyclerView.VERTICAL, false)
            rv.layoutManager = gridLayoutManager


            rv.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    gridLayoutManager.spanCount = calculateSpanCount(rv, res)
                    gridLayoutManager.requestLayout()
                }
            })
        }

        private fun calculateSpanCount(rv: RecyclerView, res: Resources): Int{
            val viewWidth = rv.measuredWidth
            val moviePosterWidth = res.getDimension(R.dimen.poster_width)
            val moviePosterMargin = res.getDimension(R.dimen.margin_medium)
            val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                    moviePosterMargin)).toDouble()).toInt()
            return if (spanCount >= DEFAULT_GRID_SPAN_COUNT) spanCount else DEFAULT_GRID_SPAN_COUNT
        }

        fun setRVAdapter(movieList: List<MovieItemSearch>, onMovieClick: (MovieItemSearch) -> Unit)
                : MovieSearchAdapter {
            return MovieSearchAdapter(
                movieList,
                { movieItem -> onMovieClick(movieItem) })
        }
    }
}