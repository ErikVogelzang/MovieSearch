package com.example.moviesearch.ui


import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
                ddCatSecondary.visibility = View.GONE
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0)
                    setSecondaryCategories(parent?.getItemAtPosition(position).toString(), view!!)
                else
                    ddCatSecondary.adapter = ArrayAdapter<String>(view!!.context, android.R.layout.simple_spinner_dropdown_item, ArrayList<String>())
                onCategoriesChanged()
            }

        }
        ddCatSecondary.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    }

    private fun setSecondaryCategories(primarySelection: String, view: View) {
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
        val catAdapterSec = ArrayAdapter<String>(view.context, android.R.layout.simple_spinner_dropdown_item, secCategories)
        ddCatSecondary.adapter = catAdapterSec
    }


    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        val action = MovieSearchFragmentDirections.actionMovieSearchFragmentToDetailsFragment(movieItem.movieID.toInt())
        findNavController().navigate(action)
    }

    fun assignAdapter() {
        movieAdapter = MovieAdapter(
            movieList,
            { movieItem -> onMovieClick(movieItem) })
        rvMovies.adapter = movieAdapter
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        movieListViewModel.getMoviesSearch(
            "2018",
            getString(R.string.movie_db_api_key)
        )
    }

    private fun onCategoriesChanged() {
        if ("2018" == Common.lastYear || ddCatPrimary.selectedItemPosition == 0 || ddSortBy.selectedItemPosition == 0)
            return
        movieListViewModel.getMoviesSearch("2018", getString(R.string.movie_db_api_key))
        Common.lastYear = "2018"
    }

    private fun calculateSpanCount(): Int{
        val viewWidth = rvMovies.measuredWidth
        val moviePosterWidth = resources.getDimension(R.dimen.poster_width)
        val moviePosterMargin = resources.getDimension(R.dimen.margin_medium)
        val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                moviePosterMargin)).toDouble()).toInt()
        return if (spanCount >= Common.DEFAULT_SPAN_COUNT) spanCount else Common.DEFAULT_SPAN_COUNT
    }
}
