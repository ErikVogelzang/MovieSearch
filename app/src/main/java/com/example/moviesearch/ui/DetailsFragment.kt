package com.example.moviesearch.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.moviesearch.R
import com.example.moviesearch.model.MovieItemDetails
import com.example.moviesearch.model.MovieViewModel
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
            tvBackdrop.text = movieDetails.backdropPath
            tvBudget.text = movieDetails.budget
            tvCast.text = movieDetails.cast
            tvGenres.text = movieDetails.genres
            tvID.text = args.movieID.toString()
            tvIMDBID.text = movieDetails.imdbID
            tvLength.text = movieDetails.length
            tvOverview.text = movieDetails.overview
            tvPoster.text = movieDetails.posterPath
            tvRating.text = movieDetails.rating
            tvRelease.text = movieDetails.release
            tvRevenue.text = movieDetails.revenue
            tvTitle.text = movieDetails.title
            tvTrailer.text = movieDetails.trailer
            tvLanguage.text = movieDetails.language
        })
        movieViewModel.getMovieDetails(args.movieID.toString(), getString(R.string.movie_db_api_key))
    }
}
