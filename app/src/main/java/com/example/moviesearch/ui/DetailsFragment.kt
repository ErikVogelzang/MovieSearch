package com.example.moviesearch.ui


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_details.*

/**
 * A simple [Fragment] subclass.
 */
class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieDetails: MovieItemDetails

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
}
