package com.example.moviesearch.ui


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieSaved
import com.example.moviesearch.model.MovieViewModel
import kotlinx.android.synthetic.main.fragment_movie_search.*
import kotlinx.android.synthetic.main.fragment_movies_saved.*

/**
 * A simple [Fragment] subclass.
 */
class MoviesSavedFragment : Fragment() {

    private lateinit var moviesSavedViewModel: MovieViewModel
    private val moviesSaved = arrayListOf<MovieItemSearch>()
    private lateinit var movieAdapter: MovieSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movies_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        moviesSavedViewModel = ViewModelProvider(requireActivity()).get(MovieViewModel::class.java)
        moviesSavedViewModel.getAllSavedMovies().observe(this.viewLifecycleOwner, Observer {
            moviesSaved.clear()
            for (movie in it) {
                moviesSaved.add(MovieItemSearch(movie.posterPath, movie.movieID, movie.title, false))
            }
            movieAdapter = MovieSearchAdapter(
                moviesSaved,
                { movieItem -> onMovieClick(movieItem) })
            rvMoviesSaved.adapter = movieAdapter
        })
    }

    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        val action = MoviesSavedFragmentDirections.actionMoviesSavedFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath)
        findNavController().navigate(action)
    }

    private fun initViews() {
        val gridLayoutManager = GridLayoutManager(requireActivity(), Common.DEFAULT_SPAN_COUNT,
            RecyclerView.VERTICAL, false)
        rvMoviesSaved.layoutManager = gridLayoutManager


        rvMoviesSaved.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rvMoviesSaved.viewTreeObserver.removeOnGlobalLayoutListener(this)
                gridLayoutManager.spanCount = calculateSpanCount()
                gridLayoutManager.requestLayout()
            }
        })
    }

    private fun calculateSpanCount(): Int{
        val viewWidth = rvMoviesSaved.measuredWidth
        val moviePosterWidth = resources.getDimension(R.dimen.poster_width)
        val moviePosterMargin = resources.getDimension(R.dimen.margin_medium)
        val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                moviePosterMargin)).toDouble()).toInt()
        return if (spanCount >= Common.DEFAULT_SPAN_COUNT) spanCount else Common.DEFAULT_SPAN_COUNT
    }
}
