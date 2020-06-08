package com.example.moviesearch.ui


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebChromeClient
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieSaved
import com.example.moviesearch.model.MovieViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_details.*

/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieDetails: MovieItemDetails
    private lateinit var menu: Menu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }


    private fun initViewModel() {
        movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel::class.java)
        movieViewModel.getMovieDetailsLiveData().observe(this.viewLifecycleOwner, Observer {
            movieDetails = it
            if (!Common.checkForValidJSonReturn(args.posterPath))
                ivPoster.setImageResource(R.drawable.no_image_poster)
            if (!Common.checkForValidJSonReturn(movieDetails.backdropPath))
                ivBackdrop.setImageResource(R.drawable.no_image_backdrop)
            if (Common.checkForValidJSonReturn(args.posterPath)) {
                pbPoster.visibility = View.VISIBLE
                Common.fetchImageGlide(
                    requireContext(), ivPoster, pbPoster,
                    args.posterPath
                )
            }
            else {
                pbPoster.visibility = View.GONE
            }
            if (Common.checkForValidJSonReturn(movieDetails.backdropPath)) {
                pbBackdrop.visibility = View.VISIBLE
                Common.fetchImageGlide(
                    requireContext(), ivBackdrop, pbBackdrop,
                    movieDetails.backdropPath
                )
            }
            else {
                pbBackdrop.visibility = View.GONE
            }
            setDetailsText(tvBudgetText, tvBudget, movieDetails.budget)
            setDetailsText(tvCastText, tvCast, movieDetails.cast)
            setDetailsText(tvGenresText, tvGenres, movieDetails.genres)
            setDetailsText(tvLengthText, tvLength, movieDetails.length)
            setDetailsText(tvPlotText, tvPlot, movieDetails.overview)
            setDetailsText(tvRatingText, tvRating, movieDetails.rating)
            setDetailsText(tvReleaseText, tvRelease, movieDetails.release)
            setDetailsText(tvRevenueText, tvRevenue, movieDetails.revenue)
            setDetailsText(tvTitle, tvTitle, movieDetails.title)
            setDetailsText(tvLanguageText, tvLanguage, movieDetails.language)
            if (movieDetails.trailer != Common.STRING_EMPTY) {
                wvVideo.visibility = View.VISIBLE
                wvVideo.settings.javaScriptEnabled = true
                wvVideo.webChromeClient = WebChromeClient()
                wvVideo.loadData(
                    "<html><body style='margin:0;padding:0;'><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/".plus(
                        movieDetails.trailer
                    ).plus("\" frameborder=\"0\" allowfullscreen></iframe>"), "text/html", "utf-8"
                )
            }
            else {
                wvVideo.visibility = View.GONE
                wvVideo.clearCache(false)
            }
        })
        movieViewModel.loadMovieWithID(args.movieID.toString()).observe(this.viewLifecycleOwner, Observer {
            if (it != null) {
                menu.setGroupVisible(R.id.save, false)
                menu.setGroupVisible(R.id.delete, true)
            }
            else {
                menu.setGroupVisible(R.id.delete, false)
                menu.setGroupVisible(R.id.save, true)
            }
        })
        movieViewModel.getMovieDetails(args.movieID.toString(), getString(R.string.movie_db_api_key))
    }

    private fun setDetailsText(tvContent: TextView, tvHeader: TextView, newText: String) {
        if (Common.checkForValidJSonReturn(newText)) {
            tvHeader.visibility = View.VISIBLE
            tvContent.visibility = View.VISIBLE
            tvContent.text = newText
        }
        else {
            tvContent.visibility = View.GONE
            tvHeader.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        tvBudgetText.visibility = View.GONE
        tvBudget.visibility = View.GONE
        ivBackdrop.visibility = View.GONE
        ivPoster.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvLanguage.visibility = View.GONE
        tvLanguageText.visibility = View.GONE
        tvRelease.visibility = View.GONE
        tvReleaseText.visibility = View.GONE
        tvRevenue.visibility = View.GONE
        tvRevenueText.visibility = View.GONE
        tvLength.visibility = View.GONE
        tvLengthText.visibility = View.GONE
        tvRating.visibility = View.GONE
        tvRatingText.visibility = View.GONE
        tvGenres.visibility = View.GONE
        tvGenresText.visibility = View.GONE
        tvCast.visibility = View.GONE
        tvCastText.visibility = View.GONE
        tvPlot.visibility = View.GONE
        tvPlotText.visibility = View.GONE
        movieViewModel.clearMovieDetails()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_details, menu)
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_movie -> {
                movieViewModel.saveMovie(saveMovie())
                Common.showUndoSnackbar(getString(R.string.saved_movie_text), this::onSaveUndo, requireView(), resources)
                true
            }
            R.id.action_delete_movie -> {
                movieViewModel.deleteSavedMovie(saveMovie())
                Common.showUndoSnackbar(getString(R.string.deleted_movie_text), this::onDeleteUndo, requireView(), resources)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveMovie(): MovieSaved {
        return MovieSaved(
            args.movieID.toString(),
            args.posterPath,
            movieDetails.backdropPath,
            movieDetails.budget,
            movieDetails.genres,
            movieDetails.imdbID,
            movieDetails.language,
            movieDetails.title,
            movieDetails.overview,
            movieDetails.release,
            movieDetails.revenue,
            movieDetails.length,
            movieDetails.rating,
            movieDetails.trailer,
            movieDetails.cast
        )
    }



    private fun onSaveUndo() {
        var changedMovies = movieViewModel.getChangedMovies()
        if (changedMovies.size > 0)
            movieViewModel.deleteSavedMovie(changedMovies[0])
    }

    private fun onDeleteUndo() {
        var changedMovies = movieViewModel.getChangedMovies()
        if (changedMovies.size > 0)
            movieViewModel.saveMovie(changedMovies[0])
    }
}
