package com.example.moviesearch.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
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
import android.content.Intent
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast


class MovieDetailsFragment : Fragment() {

    //region Initialization

    private val args: MovieDetailsFragmentArgs by navArgs()
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieDetails: MovieItemDetails
    private lateinit var menu: Menu
    private lateinit var snack: Snackbar
    private var wvNoError = true
    private var viewLoaded = false

    private fun initViewModel() {
        movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel::class.java)

        movieViewModel.getMoviesLocal().observe(this.viewLifecycleOwner, Observer {
            saved = movieViewModel.isMovieLocal(args.movieID.toString())
            updateMenu()
            if (!viewLoaded) {
                if (saved)
                    movieViewModel.fetchMovieDetailsLocal(args.movieID.toString())
                else
                    movieViewModel.fetchMovieDetailsAPI(args.movieID.toString(),
                        getString(R.string.movie_db_api_key))
            }
        })

        movieViewModel.getMovieDetails().observe(this.viewLifecycleOwner, Observer {
            if (!viewLoaded) {
                movieDetails = it
                initImageViews()
                initTextViews()
                initWebView()
                if (it.title != Common.STRING_EMPTY)
                    viewLoaded = true
            }
        })
    }

    private fun initViews() {
        setHasOptionsMenu(true)
    }

    private fun initWebView() {
        if (movieDetails.trailer != Common.STRING_EMPTY) {
            ivNoVideo.visibility = View.VISIBLE
            pbVideo.visibility = View.VISIBLE
            wvVideo.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    wvVideo.visibility = View.INVISIBLE
                    pbVideo.visibility = View.INVISIBLE
                    wvVideo.clearCache(false)
                    wvNoError = false
                    super.onReceivedError(view, request, error)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loadYoutube()
                }
            }
            wvVideo.loadUrl(BASE_IMDB_URL)
        }
        else {
            wvVideo.visibility = View.INVISIBLE
            wvVideo.clearCache(false)
        }
    }

    private fun initTextViews() {
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
    }

    private fun initImageViews() {
        setImageView(ivPoster, pbPoster, args.posterPath)
        setImageView(ivBackdrop, pbBackdrop, movieDetails.backdropPath)
    }

    private fun setImageView(iv: ImageView, pb: ProgressBar, path: String) {
        if (Common.checkForValidJSonReturn(path)) {
            pb.visibility = View.VISIBLE
            Common.fetchImageGlide(
                requireContext(), iv, pb,
                path
            )
        }
        else {
            iv.setImageResource(R.drawable.no_image_poster)
            pb.visibility = View.GONE
        }
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

    private fun loadYoutube() {
        wvVideo.webViewClient = null
        wvVideo.webChromeClient = WebChromeClient()
        wvVideo.settings.javaScriptEnabled = true
        wvVideo.loadData(YOUTUBE_BODY.plus(YOUTUBE_IFRAME_START).plus(YOUTUBE_EMBED)
            .plus(movieDetails.trailer).plus(YOUTUBE_IFRAME_END),
            YOUTUBE_MIME_TYPE, YOUTUBE_ENCODING)
        ivNoVideo.visibility = View.INVISIBLE
        pbVideo.visibility = View.INVISIBLE
        wvVideo.visibility = View.VISIBLE
    }

    //endregion

    //region Android Lifecycle Functions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViews()
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    override fun onDestroyView() {
        if (this::snack.isInitialized)
            snack.dismiss()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        updateMenu()
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_movie -> {
                movieViewModel.saveMovieLocal(saveMovie())
                snack = Common.showUndoSnackbar(getString(R.string.saved_movie_text), movieViewModel, requireView(), resources)
                true
            }
            R.id.action_delete_movie -> {
                movieViewModel.deleteMovieLocal(saveMovie())
                snack = Common.showUndoSnackbar(getString(R.string.deleted_movie_text), movieViewModel, requireView(), resources)
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            R.id.action_share_movie -> {
                shareMovie()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //endregion

    //region Menu Functions

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

    private fun shareMovie() {
        if (movieDetails.imdbID != Common.STRING_EMPTY) {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = WHATSAPP_TYPE
            whatsappIntent.setPackage(WHATSAPP_PACKAGE)
            whatsappIntent.putExtra(
                Intent.EXTRA_TEXT,
                BASE_IMDB_URL.plus(movieDetails.imdbID)
            )
            try {
                requireActivity().startActivity(whatsappIntent)
            } catch (ex: android.content.ActivityNotFoundException) {
                showToast(getString(R.string.whatsapp_error))
            }
        }
        else {
            showToast(getString(R.string.imdb_no_id))
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            requireContext(),
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateMenu() {
        if (this::menu.isInitialized) {
            if (saved) {
                menu.setGroupVisible(R.id.save, false)
                menu.setGroupVisible(R.id.delete, true)
            } else {
                menu.setGroupVisible(R.id.delete, false)
                menu.setGroupVisible(R.id.save, true)
            }
        }
    }

    //endregion

    companion object {
        private var saved = false
        private const val BASE_IMDB_URL = "https://www.imdb.com/title/"
        private const val WHATSAPP_TYPE = "text/plain"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val YOUTUBE_BODY = "<html><body style='margin:0;padding:0;'>"
        private const val YOUTUBE_IFRAME_START = "<iframe width=\"100%\" height=\"100%\" src=\""
        private const val YOUTUBE_EMBED = "https://www.youtube.com/embed/"
        private const val YOUTUBE_IFRAME_END = "\" frameborder=\"0\" allowfullscreen></iframe>"
        private const val YOUTUBE_MIME_TYPE = "text/html"
        private const val YOUTUBE_ENCODING = "utf-8"
    }
}
