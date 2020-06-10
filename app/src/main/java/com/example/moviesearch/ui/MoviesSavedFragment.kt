package com.example.moviesearch.ui


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.common.Common.Companion.showUndoSnackbar
import com.example.moviesearch.common.Common.Companion.setRVLayout
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieSaved
import com.example.moviesearch.model.MovieViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_movies_saved.*

class MoviesSavedFragment : Fragment() {

    //region Initialization

    private lateinit var movieViewModel: MovieViewModel
    private val moviesSaved = arrayListOf<MovieItemSearch>()
    private val moviesSavedAsData = arrayListOf<MovieSaved>()
    private lateinit var movieAdapter: MovieSearchAdapter
    private lateinit var menu: Menu
    private lateinit var snack: Snackbar

    private fun initViewModel() {
        movieViewModel = ViewModelProvider(requireActivity()).get(MovieViewModel::class.java)
        movieViewModel.getMoviesLocal().observe(this.viewLifecycleOwner, Observer {
            moviesSaved.clear()
            moviesSavedAsData.clear()
            moviesSavedAsData.addAll(it)
            moviesSaved.clear()
            moviesSavedAsData.addAll(it)
            saved = false
            for (movie in it) {
                moviesSaved.add(MovieItemSearch(movie.posterPath, movie.movieID, movie.title, false))
                saved = true
            }
            if (this::menu.isInitialized)
                updateDeleteIcon(menu)
            movieAdapter.notifyDataSetChanged()
        })
    }

    private fun initViews() {
        setHasOptionsMenu(true)
        setRVLayout(requireContext(), rvMoviesSaved, resources)
        ItemTouchHelper(setItemTouchHelperCallback()).attachToRecyclerView(rvMoviesSaved)
        movieAdapter = Common.setRVAdapter(moviesSaved, { movieItem -> onMovieClick(movieItem) })
        rvMoviesSaved.adapter = movieAdapter
    }

    private fun setItemTouchHelperCallback() : ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                movieViewModel.deleteMovieLocal(moviesSavedAsData[viewHolder.adapterPosition])
                snack = showUndoSnackbar(getString(R.string.deleted_movie_text), this@MoviesSavedFragment::onDeleteUndo, requireView(), resources)
            }
        }
    }

    //endregion

    //region Android Lifecycle Functions

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_saved, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all_movies -> {
                movieViewModel.deleteMoviesLocal()
                snack = showUndoSnackbar(getString(R.string.deleted_all_movies_text), this::onDeleteUndo, requireView(), resources)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    //endregion

    //region UI Click Functions

    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        findNavController().navigate(MoviesSavedFragmentDirections.actionMoviesSavedFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath))
    }

    private fun onDeleteUndo() {
        Common.onUndo(movieViewModel)
    }

    private fun updateDeleteIcon(passedMenu: Menu) {
        passedMenu.setGroupVisible(R.id.deleteAll, saved)
    }

    //endregion

    companion object {
        var saved = false
    }
}
