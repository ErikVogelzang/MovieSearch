package com.example.moviesearch.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemSearch
import kotlinx.android.synthetic.main.item_movie.view.*

class MovieSearchAdapter(val movieList: List<MovieItemSearch>, private val onClick: (MovieItemSearch) -> Unit) :
    RecyclerView.Adapter<MovieSearchAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false)
        )
    }

    override fun getItemCount(): Int = movieList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { onClick(movieList[adapterPosition]) }
        }

        fun bind(movieItem: MovieItemSearch) {
            loadRVMovieBehaviour(movieItem)
        }

        private fun loadRVMovieBehaviour(movieItem: MovieItemSearch) {
            if (!movieItem.loading) {
                if (Common.checkForValidJSonReturn(movieItem.posterPath) && !movieItem.hasBeenFetched) {
                    Common.fetchImageGlide(
                        context, itemView.ivMovie, itemView.pbLoading,
                        movieItem.posterPath
                    )
                    movieItem.hasBeenFetched = true
                    itemView.tvFallbackTitle.visibility = View.GONE
                }
                else {
                    itemView.pbLoading.visibility = View.GONE
                    itemView.tvFallbackTitle.text = movieItem.title
                }
            }
            else {
                itemView.pbLoading.visibility = View.VISIBLE
            }
        }
    }
}