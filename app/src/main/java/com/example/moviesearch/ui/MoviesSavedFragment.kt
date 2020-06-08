package com.example.moviesearch.ui


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.common.Common.Companion.showUndoSnackbar
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieSaved
import com.example.moviesearch.model.MovieViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_movie_search.*
import kotlinx.android.synthetic.main.fragment_movies_saved.*

/**
 * A simple [Fragment] subclass.
 */
class MoviesSavedFragment : Fragment() {

    private lateinit var moviesSavedViewModel: MovieViewModel
    private val moviesSaved = arrayListOf<MovieItemSearch>()
    private val moviesSavedAsData = arrayListOf<MovieSaved>()
    private lateinit var movieAdapter: MovieSearchAdapter
    private lateinit var menu: Menu
    private lateinit var snack: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
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
            moviesSavedAsData.clear()
            Common.savedMoviesNum = 0
            moviesSavedAsData.addAll(it)
            for (movie in it) {
                moviesSaved.add(MovieItemSearch(movie.posterPath, movie.movieID, movie.title, false))
                Common.savedMoviesNum = Common.savedMoviesNum + 1
            }
            assignAdapter()
            if (this::menu.isInitialized)
                updateDeleteIcon(menu)
        })
    }

    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        val action = MoviesSavedFragmentDirections.actionMoviesSavedFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath)
        findNavController().navigate(action)
    }

    private fun initViews() {
        //if (this::moviesSavedViewModel.isInitialized)
            //moviesSavedViewModel
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

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                moviesSavedViewModel.deleteSavedMovie(moviesSavedAsData[viewHolder.adapterPosition])
                snack = showUndoSnackbar(getString(R.string.deleted_movie_text), this@MoviesSavedFragment::onDeleteUndo, requireView(), resources)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvMoviesSaved)
    }

    private fun calculateSpanCount(): Int{
        val viewWidth = rvMoviesSaved.measuredWidth
        val moviePosterWidth = resources.getDimension(R.dimen.poster_width)
        val moviePosterMargin = resources.getDimension(R.dimen.margin_medium)
        val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                moviePosterMargin)).toDouble()).toInt()
        return if (spanCount >= Common.DEFAULT_SPAN_COUNT) spanCount else Common.DEFAULT_SPAN_COUNT
    }

    private fun onDeleteUndo() {
        for (movie in moviesSavedViewModel.getChangedMovies()) {
            moviesSavedViewModel.saveMovie(movie, true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_saved, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all_movies -> {
                moviesSavedViewModel.deleteAllSavedMovies()
                snack = Common.showUndoSnackbar(getString(R.string.deleted_all_movies_text), this::onDeleteUndo, requireView(), resources)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun updateDeleteIcon(passedMenu: Menu) {
        if (Common.savedMoviesNum > 0)
            passedMenu.setGroupVisible(R.id.deleteAll, true)
        else
            passedMenu.setGroupVisible(R.id.deleteAll, false)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        updateDeleteIcon(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        if (this::snack.isInitialized)
            snack.dismiss()
        super.onDestroyView()
    }

    private fun assignAdapter() {
        movieAdapter = MovieSearchAdapter(
            moviesSaved,
            { movieItem -> onMovieClick(movieItem) })
        rvMoviesSaved.adapter = movieAdapter
    }
}
