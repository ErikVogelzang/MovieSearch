package com.example.moviesearch


import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.fragment_movie_search.*

/**
 * A simple [Fragment] subclass.
 */
class MovieSearchFragment : Fragment() {

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
        initViews(view)
    }

    private fun initViews(view: View) {
        val catAdapterPri = ArrayAdapter<String>(view.context, android.R.layout.simple_spinner_dropdown_item, categories)
        ddCatPrimary.adapter = catAdapterPri
        val sortAdapter = ArrayAdapter<String>(view.context, android.R.layout.simple_spinner_dropdown_item, sortOptions)
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
}
