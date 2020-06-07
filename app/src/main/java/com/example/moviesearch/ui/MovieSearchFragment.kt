package com.example.moviesearch.ui


import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.example.moviesearch.model.MovieItemSearch
import com.example.moviesearch.model.MovieViewModel
import kotlinx.android.synthetic.main.fragment_movie_search.*
import kotlin.collections.ArrayList
import android.content.Context.INPUT_METHOD_SERVICE
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.example.moviesearch.model.SearchTextState


/**
 * A simple [Fragment] subclass.
 */
class MovieSearchFragment : Fragment() {
    private  var lastTextState = SearchTextState()
    private  var currentTextState = SearchTextState()
    private lateinit var movieListViewModel: MovieViewModel
    private val movieList = arrayListOf<MovieItemSearch>()
    private lateinit var movieSearchAdapter: MovieSearchAdapter



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
        "Popularity",
        "Release",
        "Revenue",
        "Title",
        "Score",
        "Vote Count"
    )

    private val sortDirections = arrayOf (
        "Descending",
        "Ascending"
    )

    private val sortByYearOptions = arrayOf (
        Common.STRING_GTE,
        Common.STRING_GT,
        Common.STRING_LTE,
        Common.STRING_LT,
        Common.STRING_EQUAL,
        Common.STRING_GTLT,
        Common.STRING_GTELTE
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
        movieListViewModel = ViewModelProvider(requireActivity()).get(MovieViewModel::class.java)
        movieListViewModel.getMovieListLiveData().observe(this.viewLifecycleOwner, Observer {
            movieList.clear()
            movieList.addAll(it)
            assignMovieAdapter()
            if (Common.maxPages == 0) {
                btnPrev.visibility = View.GONE
                etPage.visibility = View.GONE
                btnNext.visibility = View.GONE
            }
            else {
                btnPrev.visibility = View.VISIBLE
                etPage.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    private fun initViews() {
        val gridLayoutManager = GridLayoutManager(requireActivity(), Common.DEFAULT_SPAN_COUNT,
            RecyclerView.VERTICAL, false)
        rvMoviesSearch.layoutManager = gridLayoutManager


        rvMoviesSearch.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rvMoviesSearch.viewTreeObserver.removeOnGlobalLayoutListener(this)
                gridLayoutManager.spanCount = calculateSpanCount()
                gridLayoutManager.requestLayout()
            }
        })
        assignMovieAdapter()
        etYear1.setOnEditorActionListener(setEditTextActionListener(this::setCorrectYearDifference))
        etYear2.setOnEditorActionListener(setEditTextActionListener(this::setCorrectYearDifference))
        etPage.setOnEditorActionListener(setEditTextActionListener(this::onPageTextDone))
        etPage.setText("1")
        btnPrev.setOnClickListener(setBtnOnClick(this::onPrevBtnClick))
        btnNext.setOnClickListener(setBtnOnClick(this::onNextBtnClick))
        ddCatPrimary.adapter = setSpinnerAdapter(categories.toList())
        ddSortByMain.adapter = setSpinnerAdapter(sortOptions.toList())
        ddSortByDirectionSearch.adapter = setSpinnerAdapter(sortDirections.toList())
        ddSortByYear.adapter = setSpinnerAdapter(sortByYearOptions.toList())
        ddCatPrimary.onItemSelectedListener = initSpinner(this::onPrimaryCatItemSelect)
        ddCatSecondary.onItemSelectedListener = initSpinner()
        ddSortByMain.onItemSelectedListener = initSpinner()
        ddSortByDirectionSearch.onItemSelectedListener = initSpinner()
        ddSortByYear.onItemSelectedListener = initSpinner(this::onSortByYearItemSelect)
        if (Common.searchFragmentDestroyed) {
            ddCatPrimary.setSelection(Common.catPriSelected)
            ddSortByMain.setSelection(Common.sortBySelected)
            Common.searchFragmentDestroyed = false
        }
    }

    private fun initSpinner(onSelect: ((pos: Int) -> Unit)? = null): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (onSelect != null)
                    onSelect(position)
                categoriesChanged()
            }
        }
    }

    private fun setSecondaryCategories(primarySelection: String, restored: Boolean) {
        var pos = 0
        if (ddCatSecondary.selectedItem != null) {
            if (ddCatSecondary.selectedItem.toString() != primarySelection) {
                pos = ddCatSecondary.selectedItemPosition
            }
        }

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
        if (ddCatSecondary.selectedItem != null) {
            for (sel in 0 until (secCategories.size-1)) {
                if (secCategories[sel] == ddCatSecondary.selectedItem.toString())
                    pos = sel
            }
        }
        if (restored)
            pos = Common.catSecSelected
        ddCatSecondary.adapter = setSpinnerAdapter(secCategories)
        ddCatSecondary.setSelection(pos)
    }

    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        val action = MovieSearchFragmentDirections.actionMovieSearchFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath)
        findNavController().navigate(action)
    }

    private fun fetchMovies() {
        if (ddCatPrimary.selectedItemPosition == 0 || ddSortByMain.selectedItemPosition == 0)
            return
        movieListViewModel.getMoviesSearch(getGenreQuery(), getsortByQuery(), getString(R.string.movie_db_api_key), getYearGteQuery(), getYearLteQuery(), etPage.text.toString().toInt())
    }

    private fun calculateSpanCount(): Int{
        val viewWidth = rvMoviesSearch.measuredWidth
        val moviePosterWidth = resources.getDimension(R.dimen.poster_width)
        val moviePosterMargin = resources.getDimension(R.dimen.margin_medium)
        val spanCount = Math.floor((viewWidth / (moviePosterWidth +
                moviePosterMargin)).toDouble()).toInt()
        return if (spanCount >= Common.DEFAULT_SPAN_COUNT) spanCount else Common.DEFAULT_SPAN_COUNT
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        setSecondaryCategories(ddCatPrimary.selectedItem.toString(), true)
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroyView() {
        Common.catPriSelected = ddCatPrimary.selectedItemPosition
        Common.catSecSelected = ddCatSecondary.selectedItemPosition
        Common.sortBySelected = ddSortByMain.selectedItemPosition
        Common.searchFragmentDestroyed = true
        super.onDestroyView()
    }

    private fun getGenreID(genre: String) : String {
        return when (genre) {
            "Action" -> "28"
            "Adventure" -> "12"
            "Animation" -> "16"
            "Comedy" -> "35"
            "Crime" -> "80"
            "Documentary" -> "99"
            "Drama" -> "18"
            "Family" -> "10751"
            "Fantasy" -> "14"
            "History" -> "36"
            "Horror" -> "27"
            "Music" -> "10402"
            "Mystery" -> "9648"
            "Romance" -> "10749"
            "Science Fiction" -> "878"
            "TV Movie" -> "10770"
            "Thriller" -> "53"
            "War" -> "37"
            "Western" -> "10752"
            else -> Common.STRING_EMPTY
        }
    }

    private fun setSpinnerAdapter(list: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
    }

    private fun getGenreQuery() : String {
        val primary = getGenreID(ddCatPrimary.selectedItem.toString())
        val secondary = getGenreID(ddCatSecondary.selectedItem.toString())
        if (primary == Common.STRING_EMPTY)
            return Common.STRING_EMPTY
        if (secondary == Common.STRING_EMPTY)
            return primary
        else
            return primary.plus(", ").plus(secondary)
    }

    private fun getsortByQuery() : String {
        var str = ddSortByMain.selectedItem.toString()
        when (str) {
            "Release" -> str = "primary_release_date"
            "Title" -> str = "original_title"
            "Score" -> str = "vote_average"
            "Vote Count" -> str = "vote_count"
            else -> str = str.toLowerCase()
        }
        when (ddSortByDirectionSearch.selectedItem.toString()) {
            "Ascending" -> str = str.plus(".asc")
            "Descending" -> str = str.plus(".desc")
        }
        return str
    }

    private fun onPrimaryCatItemSelect(pos: Int) {
        if (pos != 0)
            setSecondaryCategories(ddCatPrimary.getItemAtPosition(pos).toString(), false)
        else
            ddCatSecondary.adapter = setSpinnerAdapter(listOf())
    }

    private fun onSortByYearItemSelect(pos: Int) {
        val item = ddSortByYear.selectedItem.toString()
        if (item == Common.STRING_GTELTE || item == Common.STRING_GTLT) {
            etYear2.visibility = View.VISIBLE
            setCorrectYearDifference()
        }
        else
            etYear2.visibility = View.GONE
    }

    private fun assignMovieAdapter() {
        movieSearchAdapter = MovieSearchAdapter(
            movieList,
            { movieItem -> onMovieClick(movieItem) })
        rvMoviesSearch.adapter = movieSearchAdapter
    }

    private fun getYearGteQuery(): String {
        val filter = ddSortByYear.selectedItem.toString()
        val year = etYear1.text.toString()
        if (filter != Common.STRING_LTE && filter != Common.STRING_LT) {
            if (filter == Common.STRING_GT || filter == Common.STRING_GTLT)
                return getYearFormatted(increaseOrDecreaseStringNumber(year, true), true)
            return getYearFormatted(year, true)
        }
        return Common.STRING_EMPTY
    }

    private fun getYearLteQuery(): String {
        val filter = ddSortByYear.selectedItem.toString()
        val year1 = etYear1.text.toString()

        if (filter == Common.STRING_LTE || filter == Common.STRING_EQUAL)
            return getYearFormatted(year1, false)

        val year2 = etYear2.text.toString()
        return when(filter) {
            Common.STRING_GTLT -> getYearFormatted(increaseOrDecreaseStringNumber(year2, false), false)
            Common.STRING_LT -> getYearFormatted(increaseOrDecreaseStringNumber(year1, false), false)
            Common.STRING_GTELTE -> getYearFormatted(year2, false)
            else -> Common.STRING_EMPTY
        }
    }

    private fun getYearFormatted(year: String, isHigherThan: Boolean) : String {
        var newYear = year
        if (year.length != 4)
            newYear = "1895"
        if (year == Common.STRING_EMPTY)
            return Common.STRING_EMPTY
        if (isHigherThan)
            return newYear + Common.YEAR_START
        else
            return newYear + Common.YEAR_END
    }

    private fun setEditTextActionListener(onDone: (() -> Unit)): TextView.OnEditorActionListener {
        return object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_PREVIOUS -> {
                        if (v != null)
                            hideKeyboard(v)
                        onDone()
                        categoriesChanged(false)
                        return true
                    }
                }
                return false
            }

        }
    }

    private fun increaseOrDecreaseStringNumber(number: String, increase: Boolean, multiplyBy: Int = 1) : String {
        val newNumber = number.toIntOrNull()
        if (newNumber == null)
            return Common.STRING_EMPTY
        return when(increase) {
            true -> (newNumber+(1*multiplyBy)).toString()
            else -> (newNumber-(1*multiplyBy)).toString()
        }
    }

    private fun onPageTextDone() {
        var pageNumber = etPage.text.toString().toIntOrNull()
        if (pageNumber == null || pageNumber == 0) {
            pageNumber = 1
        }
        else if (pageNumber > Common.maxPages) {
            pageNumber = Common.maxPages
        }
        etPage.setText(pageNumber.toString())
        updateTextState()
    }

    private fun setBtnOnClick(onClick: (() -> Unit)? = null): View.OnClickListener {
        return View.OnClickListener {
            if (onClick != null)
                onClick()
            categoriesChanged(false)
        }
    }

    private fun onPrevBtnClick() {
        if (etPage.text.toString().toInt() == 1)
            return
        etPage.setText(increaseOrDecreaseStringNumber(etPage.text.toString(), false))
        updateTextState()
    }

    private fun onNextBtnClick() {
        if (etPage.text.toString().toInt() == Common.maxPages)
            return
        etPage.setText(increaseOrDecreaseStringNumber(etPage.text.toString(), true))
        updateTextState()
    }

    private fun categoriesChanged(newCategories: Boolean = true) {
        Log.i("TESTINGTESTING", "ISHERE")
        if (newCategories)
            etPage.setText("1")
        if (isTextStateChanged() || newCategories) {
            fetchMovies()
            updateTextState()
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun updateTextState() {
        lastTextState.yearOneText = currentTextState.yearOneText
        lastTextState.yearTwoText = currentTextState.yearTwoText
        lastTextState.pageText = currentTextState.pageText
        currentTextState.yearOneText = etYear1.text.toString()
        currentTextState.yearTwoText = etYear2.text.toString()
        currentTextState.pageText = etPage.text.toString()
    }

    private fun isTextStateChanged(): Boolean {
        return (
                currentTextState.pageText != lastTextState.pageText
                        || currentTextState.yearOneText != lastTextState.yearOneText
                        || currentTextState.yearTwoText != lastTextState.yearTwoText
                )
    }

    private fun setCorrectYearDifference() {
        val year1 = etYear1.text.toString().toIntOrNull()
        val year2 = etYear2.text.toString().toIntOrNull()
        val gtltDifference = 2
        val filter = ddSortByYear.selectedItem.toString()
        if (year1 != null && year2 != null) {
            if (filter == Common.STRING_GTELTE && year1 > year2)
                etYear2.setText(increaseOrDecreaseStringNumber(etYear1.text.toString(), true))
            else if (filter == Common.STRING_GTLT && (year2-year1) < gtltDifference)
                etYear2.setText(increaseOrDecreaseStringNumber(etYear1.text.toString(), true, gtltDifference))
        }
        updateTextState()
    }
}
