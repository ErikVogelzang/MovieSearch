package com.example.moviesearch.ui


import android.app.Activity
import android.content.res.Configuration
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.iterator
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieViewModel
import kotlinx.android.synthetic.main.fragment_movie_search.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class MovieSearchFragment : Fragment() {

    private lateinit var movieListViewModel: MovieViewModel
    private val movieList = arrayListOf<MovieItemSearch>()
    private lateinit var movieAdapter: MovieAdapter



    private val categories = arrayOf (
        "Select primary genre",
        "Action",
        "Adventure",
        "Animation",
        "Comedy",
        "Crime",
        "Documentary",
        "Drama",
        "Family",
        "Fantasy",
        "History",
        "Horror",
        "Music",
        "Mystery",
        "Romance",
        "Science Fiction",
        "TV Movie",
        "Thriller",
        "War",
        "Western"
    )

    private val sortOptions = arrayOf (
        "Select how to sort",
        "Popularity Asc",
        "Popularity Desc",
        "Release Asc",
        "Release Desc",
        "Revenue Asc",
        "Revenue Desc",
        "Title Asc",
        "Title Desc",
        "Score Asc",
        "Score Desc",
        "Vote Count Asc",
        "Vote Count Desc"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViewModel() {
        movieListViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel::class.java)
        movieListViewModel.getMovieListLiveData().observe(this.viewLifecycleOwner, Observer {
            assignAdapter()
            movieList.clear()
            movieList.addAll(it)
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()

    }

    private fun initViews() {
        val gridLayoutManager = GridLayoutManager(requireActivity(), Common.DEFAULT_SPAN_COUNT,
            RecyclerView.VERTICAL, false)
        rvMovies.layoutManager = gridLayoutManager


        rvMovies.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rvMovies.viewTreeObserver.removeOnGlobalLayoutListener(this)
                gridLayoutManager.spanCount = calculateSpanCount()
                gridLayoutManager.requestLayout()
            }
        })
        assignAdapter()

        val catAdapterPri = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        ddCatPrimary.adapter = catAdapterPri
        val sortAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)
        ddSortBy.adapter = sortAdapter
        ddCatPrimary.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0)
                    setSecondaryCategories(parent?.getItemAtPosition(position).toString(), false)
                else
                    ddCatSecondary.adapter = ArrayAdapter<String>(requireActivity().application.applicationContext, android.R.layout.simple_spinner_dropdown_item, ArrayList<String>())
                onCategoriesChanged()
            }

        }
        ddCatSecondary.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Common.catSecSelected = ddCatSecondary.selectedItemPosition
                onCategoriesChanged()
            }

        }
        ddSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                ddCatSecondary.visibility = View.GONE
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onCategoriesChanged()
            }
        }
        if (Common.searchFragmentDestroyed) {
            ddCatPrimary.setSelection(Common.catPriSelected)
            ddSortBy.setSelection(Common.sortBySelected)
            Common.searchFragmentDestroyed = false
        }
    }

    private fun setSecondaryCategories(primarySelection: String, restored: Boolean) {
        var pos = 0
        if (ddCatSecondary.selectedItem != null) {
            if (ddCatSecondary.selectedItem.toString() != primarySelection) {
                pos = ddCatSecondary.selectedItemPosition
            }
        }

        val secCategories = ArrayList<String>()
        var passedFirst = false
        for (sel in categories) {
            if (primarySelection != sel) {
                if (passedFirst)
                    secCategories.add(sel)
                else {
                    secCategories.add("Select secondary genre")
                    passedFirst = true
                }
            }
        }
        if (ddCatSecondary.selectedItem != null) {
            for (sel in 0 until (secCategories.size-1)) {
                if (secCategories[sel] == ddCatSecondary.selectedItem.toString())
                    pos = sel
            }
        }
        if (restored)
            pos = Common.catSecSelected
        val catAdapterSec = ArrayAdapter<String>(requireActivity().applicationContext, android.R.layout.simple_spinner_dropdown_item, secCategories)
        ddCatSecondary.adapter = catAdapterSec
        ddCatSecondary.setSelection(pos)
    }


    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        val action = MovieSearchFragmentDirections.actionMovieSearchFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath)
        findNavController().navigate(action)
    }

    fun assignAdapter() {
        movieAdapter = MovieAdapter(
            movieList,
            { movieItem -> onMovieClick(movieItem) })
        rvMovies.adapter = movieAdapter
    }

    private fun onCategoriesChanged() {
        if (ddCatPrimary.selectedItemPosition == 0 || ddSortBy.selectedItemPosition == 0)
            return
        movieListViewModel.getMoviesSearch(getGenreQuery(), getsortByQuery(), getString(R.string.movie_db_api_key))
    }

    private fun calculateSpanCount(): Int{
        val viewWidth = rvMovies.measuredWidth
        val moviePosterWidth = resources.getDimension(R.dimen.poster_width)
        val moviePosterMargin = resources.getDimension(R.dimen.margin_medium)
        val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                moviePosterMargin)).toDouble()).toInt()
        return if (spanCount >= Common.DEFAULT_SPAN_COUNT) spanCount else Common.DEFAULT_SPAN_COUNT
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        setSecondaryCategories(ddCatPrimary.selectedItem.toString(), true)
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroyView() {
        Common.catPriSelected = ddCatPrimary.selectedItemPosition
        Common.catSecSelected = ddCatSecondary.selectedItemPosition
        Common.sortBySelected = ddSortBy.selectedItemPosition
        Common.searchFragmentDestroyed = true
        super.onDestroyView()
    }

    private fun getGenreID(genre: String) : String {
        return when (genre) {
            "Action" -> "28"
            "Adventure" -> "12"
            "Animation" -> "16"
            "Comedy" -> "35"
            "Crime" -> "80"
            "Documentary" -> "99"
            "Drama" -> "18"
            "Family" -> "10751"
            "Fantasy" -> "14"
            "History" -> "36"
            "Horror" -> "27"
            "Music" -> "10402"
            "Mystery" -> "9648"
            "Romance" -> "10749"
            "Science Fiction" -> "878"
            "TV Movie" -> "10770"
            "Thriller" -> "53"
            "War" -> "37"
            "Western" -> "10752"
            else -> Common.STRING_EMPTY
        }
    }

    private fun getGenreQuery() : String {
        val primary = getGenreID(ddCatPrimary.selectedItem.toString())
        val secondary = getGenreID(ddCatSecondary.selectedItem.toString())
        if (primary == Common.STRING_EMPTY)
            return Common.STRING_EMPTY
        if (secondary == Common.STRING_EMPTY)
            return primary
        else
            return primary.plus(", ").plus(secondary)
    }

    private fun getsortByQuery() : String {
        return when (ddSortBy.selectedItem.toString()) {
            "Popularity Asc" -> "popularity.asc"
            "Popularity Desc" -> "popularity.desc"
            "Release Asc" -> "release_date.asc"
            "Release Desc" -> "release_date.desc"
            "Revenue Asc" -> "revenue.asc"
            "Revenue Desc" -> "revenue.desc"
            "Title Asc" -> "original_title.asc"
            "Title Desc" -> "original_title.desc"
            "Score Asc" -> "vote_average.asc"
            "Score Desc" -> "vote_average.desc"
            "Vote Count Asc" -> "vote_count.asc"
            "Vote Count Desc" -> "vote_count.desc"
            else -> Common.STRING_EMPTY
        }
    }
}
