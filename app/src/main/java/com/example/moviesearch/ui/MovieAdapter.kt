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

class MovieAdapter(val movieList: List<MovieItemSearch>, private val onClick: (MovieItemSearch) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false)
        )
    }

    override fun getItemCount(): Int = movieList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position], position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { onClick(movieList[adapterPosition]) }
        }

        fun bind(movieItem: MovieItemSearch, pos: Int) {
            if (!movieItem.loading) {
                Common.fetchImageGlide(context, itemView.ivMovie, itemView.pbLoading,
                    movieItem.posterPath)
            }
            else {
                itemView.ivMovie.visibility = View.GONE
                itemView.pbLoading.visibility = View.VISIBLE
            }

        }
    }
}