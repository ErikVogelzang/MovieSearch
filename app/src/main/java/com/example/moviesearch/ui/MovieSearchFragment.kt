package com.example.moviesearch.ui


import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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


class MovieSearchFragment : Fragment() {

    //region Initialization

    private  var lastTextState = SearchTextState()
    private  var currentTextState = SearchTextState()
    private lateinit var movieViewModel: MovieViewModel
    private val movieList = arrayListOf<MovieItemSearch>()
    private lateinit var movieAdapter: MovieSearchAdapter

    private val categories = arrayOf (
        PRI_CAT_START_TEXT,
        CAT_NAME_ACT,
        CAT_NAME_ADV,
        CAT_NAME_ANI,
        CAT_NAME_COM,
        CAT_NAME_CRI,
        CAT_NAME_DOC,
        CAT_NAME_DRAMA,
        CAT_NAME_FAM,
        CAT_NAME_FAN,
        CAT_NAME_HIS,
        CAT_NAME_HOR,
        CAT_NAME_MUS,
        CAT_NAME_MYS,
        CAT_NAME_ROM,
        CAT_NAME_SCIFI,
        CAT_NAME_TV,
        CAT_NAME_THR,
        CAT_NAME_WAR,
        CAT_NAME_WES
    )

    private val sortOptions = arrayOf (
        SORT_MAIN_START_TEXT,
        SORT_MAIN_POP,
        SORT_MAIN_REL,
        SORT_MAIN_REV,
        SORT_MAIN_TI,
        SORT_MAIN_SCO,
        SORT_MAIN_VC
    )

    private val sortDirections = arrayOf (
        SORT_DIR_DESC,
        SORT_DIR_ASC
    )

    private val sortByYearOptions = arrayOf (
        STRING_GTE,
        STRING_GT,
        STRING_LTE,
        STRING_LT,
        STRING_EQUAL,
        STRING_GTLT,
        STRING_GTELTE
    )

    private fun initViewModel() {
        movieViewModel = ViewModelProvider(requireActivity()).get(MovieViewModel::class.java)
        movieViewModel.getMoviesApi().observe(this.viewLifecycleOwner, Observer {
            if (it != null) {
                rvMoviesSearch.visibility = View.VISIBLE
                movieList.clear()
                movieList.addAll(it)
                if (checkForMovieRestoreChange()) {
                    movieAdapter.notifyItemRangeChanged(Common.ARRAY_FIRST, movieList.size)
                    maxPages = movieViewModel.getMaxPages()
                    if (movieList.isEmpty()) {
                        btnPrev.visibility = View.GONE
                        etPage.visibility = View.GONE
                        btnNext.visibility = View.GONE
                    } else {
                        btnPrev.visibility = View.VISIBLE
                        etPage.visibility = View.VISIBLE
                        btnNext.visibility = View.VISIBLE
                    }
                }
                else
                    movieAdapter.notifyDataSetChanged()

            }
            else
                rvMoviesSearch.visibility = View.INVISIBLE
        })
    }

    private fun checkForMovieRestoreChange() :Boolean {
        var hasChanged = false
        if (movieList.size != destroyedMovieList.size || destroyedMovieList.size == ZERO)
            hasChanged = true
        else {
            for (pos in Common.ARRAY_FIRST..increaseOrDecreaseNumber(destroyedMovieList.size,false)) {
                if (movieList[pos].movieID != destroyedMovieList[pos].movieID)
                    hasChanged = true
            }
        }
        return hasChanged
    }

    private fun initViews() {
        Common.setRVLayout(requireContext(), rvMoviesSearch, resources)
        movieAdapter = Common.setRVAdapter(movieList, { movieItem -> onMovieClick(movieItem) })
        rvMoviesSearch.adapter = movieAdapter
        etYear1.setOnEditorActionListener(setEditTextActionListener(this::setCorrectYearDifference))
        etYear2.setOnEditorActionListener(setEditTextActionListener(this::setCorrectYearDifference))
        etPage.setOnEditorActionListener(setEditTextActionListener(this::onPageTextDone))
        btnPrev.setOnClickListener(setBtnOnClick(this::onPrevBtnClick))
        btnNext.setOnClickListener(setBtnOnClick(this::onNextBtnClick))
        ddCatPrimary.adapter = setSpinnerAdapter(categories.toList())
        ddSortByMain.adapter = setSpinnerAdapter(sortOptions.toList())
        ddSortByDirection.adapter = setSpinnerAdapter(sortDirections.toList())
        ddSortByYear.adapter = setSpinnerAdapter(sortByYearOptions.toList())
        ddCatPrimary.onItemSelectedListener = initSpinner("PRI", this::onPrimaryCatItemSelect)
        ddCatSecondary.onItemSelectedListener = initSpinner("SEC")
        ddSortByMain.onItemSelectedListener = initSpinner("MAIN")
        ddSortByDirection.onItemSelectedListener = initSpinner("DIR")
        ddSortByYear.onItemSelectedListener = initSpinner("YEAR", this::onSortByYearItemSelect)
        if (restoreFromDestroyed) {
            ddCatPrimary.setSelection(catPriSelected)
            ddSortByMain.setSelection(sortByMainSelected)
            ddSortByYear.setSelection(sortByYearSelected)
            ddSortByDirection.setSelection(sortByDirSelected)
            etPage.setText(page)
            etYear1.setText(yearOne)
            etYear2.setText(yearTwo)
            updateTextState()
        }
    }

    private fun initSpinner(debug: String, onSelect: ((pos: Int) -> Unit)? = null): AdapterView.OnItemSelectedListener {
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
                Log.i("sgdgdsgds", debug)
                categoriesChanged()
            }
        }
    }

    private fun setSecondaryCategories(primarySelection: String, restored: Boolean) {
        var pos = Common.ARRAY_FIRST
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
                    secCategories.add(SEC_CAT_START_TEXT)
                    passedFirst = true
                }
            }
        }
        if (ddCatSecondary.selectedItem != null) {
            for (sel in Common.ARRAY_FIRST until (increaseOrDecreaseNumber(secCategories.size, false))) {
                if (secCategories[sel] == ddCatSecondary.selectedItem.toString())
                    pos = sel
            }
        }
        if (restoreFromDestroyed) {
            pos = catSecSelected
        }
        ddCatSecondary.adapter = setSpinnerAdapter(secCategories)
        ddCatSecondary.setSelection(pos)
    }

    private fun onMovieClick(movieItem: MovieItemSearch) {
        if (movieItem.loading)
            return
        findNavController().navigate(MovieSearchFragmentDirections.actionMovieSearchFragmentToDetailsFragment(movieItem.movieID.toInt(), movieItem.posterPath))
    }

    private fun setSpinnerAdapter(list: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
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

    private fun setBtnOnClick(onClick: (() -> Unit)? = null): View.OnClickListener {
        return View.OnClickListener {
            if (onClick != null)
                onClick()
            categoriesChanged(false)
        }
    }

    //endregion

    //region Android Lifecycle Functions

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        setSecondaryCategories(ddCatPrimary.selectedItem.toString(), true)
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroyView() {
        catPriSelected = ddCatPrimary.selectedItemPosition
        catSecSelected = ddCatSecondary.selectedItemPosition
        sortByMainSelected = ddSortByMain.selectedItemPosition
        sortByYearSelected = ddSortByYear.selectedItemPosition
        sortByDirSelected = ddSortByDirection.selectedItemPosition
        page = etPage.text.toString()
        yearOne = etYear1.text.toString()
        yearTwo = etYear2.text.toString()
        restoreFromDestroyed = true
        destroyedMovieList = movieList
        super.onDestroyView()
    }

    //endregion

    //region API Functions

    private fun fetchMovies() {
        if (ddCatPrimary.selectedItemPosition == Common.ARRAY_FIRST || ddSortByMain.selectedItemPosition == Common.ARRAY_FIRST)
            return
        movieViewModel.fetchMoviesAPI(getGenreQuery(), getSortByQuery(), getString(R.string.movie_db_api_key), getYearGteQuery(), getYearLteQuery(), etPage.text.toString().toInt())
    }

    private fun getGenreID(genre: String) : String {
        return when (genre) {
            CAT_NAME_ACT -> CAT_ID_ACT
            CAT_NAME_ADV -> CAT_ID_ADV
            CAT_NAME_ANI -> CAT_ID_ANI
            CAT_NAME_COM -> CAT_ID_COM
            CAT_NAME_CRI -> CAT_ID_CRI
            CAT_NAME_DOC -> CAT_ID_DOC
            CAT_NAME_DRAMA -> CAT_ID_DRAMA
            CAT_NAME_FAM -> CAT_ID_FAM
            CAT_NAME_FAN -> CAT_ID_FAN
            CAT_NAME_HIS -> CAT_ID_HIS
            CAT_NAME_HOR -> CAT_ID_HOR
            CAT_NAME_MUS -> CAT_ID_MUS
            CAT_NAME_MYS -> CAT_ID_MYS
            CAT_NAME_ROM -> CAT_ID_ROM
            CAT_NAME_SCIFI -> CAT_ID_SCIFI
            CAT_NAME_TV -> CAT_ID_TV
            CAT_NAME_THR -> CAT_ID_THR
            CAT_NAME_WAR -> CAT_ID_WAR
            CAT_NAME_WES -> CAT_ID_WES
            else -> Common.STRING_EMPTY
        }
    }

    private fun getGenreQuery() : String {
        val primary = getGenreID(ddCatPrimary.selectedItem.toString())
        val secondary = getGenreID(ddCatSecondary.selectedItem.toString())
        if (primary == Common.STRING_EMPTY)
            return Common.STRING_EMPTY
        if (secondary == Common.STRING_EMPTY)
            return primary
        else
            return primary.plus(QUERY_GENRE_SPACING).plus(secondary)
    }

    private fun getSortByQuery() : String {
        var str = ddSortByMain.selectedItem.toString()
        when (str) {
            SORT_MAIN_REL -> str = SORT_MAIN_REL_QUERY
            SORT_MAIN_TI -> str = SORT_MAIN_TI_QUERY
            SORT_MAIN_SCO -> str = SORT_MAIN_SCO_QUERY
            SORT_MAIN_VC -> str = SORT_MAIN_VC_QUERY
            else -> str = str.toLowerCase()
        }
        when (ddSortByDirection.selectedItem.toString()) {
            SORT_DIR_ASC -> str = str.plus(SORT_DIR_ASC_QUERY)
            SORT_DIR_DESC -> str = str.plus(SORT_DIR_DESC_QUERY)
        }
        return str
    }

    private fun getYearGteQuery(): String {
        val filter = ddSortByYear.selectedItem.toString()
        val year = etYear1.text.toString()
        if (filter != STRING_LTE && filter != STRING_LT) {
            if (filter == STRING_GT || filter == STRING_GTLT)
                return getYearFormatted(increaseOrDecreaseStringNumber(year, true), true)
            return getYearFormatted(year, true)
        }
        return Common.STRING_EMPTY
    }

    private fun getYearLteQuery(): String {
        val filter = ddSortByYear.selectedItem.toString()
        val year1 = etYear1.text.toString()

        if (filter == STRING_LTE || filter == STRING_EQUAL)
            return getYearFormatted(year1, false)

        val year2 = etYear2.text.toString()
        return when(filter) {
            STRING_GTLT -> getYearFormatted(increaseOrDecreaseStringNumber(year2, false), false)
            STRING_LT -> getYearFormatted(increaseOrDecreaseStringNumber(year1, false), false)
            STRING_GTELTE -> getYearFormatted(year2, false)
            else -> Common.STRING_EMPTY
        }
    }

    private fun getYearFormatted(year: String, isHigherThan: Boolean) : String {
        var newYear = year
        if (year.length != ACCEPTED_YEAR_LENGTH)
            newYear = LOWEST_ACCEPTED_YEAR
        if (year == Common.STRING_EMPTY)
            return Common.STRING_EMPTY
        if (isHigherThan)
            return newYear + YEAR_START
        else
            return newYear + YEAR_END
    }

    //endregion

    //region UI Functions

    private fun onPrimaryCatItemSelect(pos: Int) {
        if (pos != Common.ARRAY_FIRST)
            setSecondaryCategories(ddCatPrimary.getItemAtPosition(pos).toString(), false)
        else
            ddCatSecondary.adapter = setSpinnerAdapter(listOf())
    }

    private fun onSortByYearItemSelect(pos: Int) {
        val item = ddSortByYear.selectedItem.toString()
        if (item == STRING_GTELTE || item == STRING_GTLT) {
            etYear2.visibility = View.VISIBLE
            setCorrectYearDifference()
        }
        else
            etYear2.visibility = View.GONE
    }

    private fun increaseOrDecreaseStringNumber(number: String, increase: Boolean, amount: Int = ONE) : String {
        val newNumber = number.toIntOrNull()
        if (newNumber == null)
            return Common.STRING_EMPTY
        return increaseOrDecreaseNumber(newNumber, increase, amount).toString()
    }

    private fun increaseOrDecreaseNumber(number: Int, increase: Boolean, amount: Int = ONE): Int {
        return when(increase) {
            true -> number + amount
            false -> number - amount
        }
    }

    private fun onPageTextDone() {
        var pageNumber = etPage.text.toString().toIntOrNull()
        if (pageNumber == null || pageNumber == ZERO) {
            pageNumber = ONE
        }
        else if (pageNumber > maxPages) {
            pageNumber = maxPages
        }
        etPage.setText(pageNumber.toString())
        restoreFromDestroyed = false
        updateTextState()
    }

    private fun onPrevBtnClick() {
        if (etPage.text.toString().toInt() == ONE)
            return
        etPage.setText(increaseOrDecreaseStringNumber(etPage.text.toString(), false))
        updateTextState()
        restoreFromDestroyed = false
    }

    private fun onNextBtnClick() {
        if (etPage.text.toString().toInt() == maxPages)
            return
        etPage.setText(increaseOrDecreaseStringNumber(etPage.text.toString(), true))
        updateTextState()
        restoreFromDestroyed = false
    }

    private fun categoriesChanged(newCategories: Boolean = true) {
        if (newCategories && !restoreFromDestroyed)
            etPage.setText(ONE.toString())
        if (isTextStateChanged() || newCategories) {
            fetchMovies()
            updateTextState()
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, KEYBOARD_FLAGS)
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
        val filter = ddSortByYear.selectedItem.toString()
        if (year1 != null && year2 != null) {
            if (filter == STRING_GTELTE && year1 > year2)
                etYear2.setText(increaseOrDecreaseStringNumber(etYear1.text.toString(), true))
            else if (filter == STRING_GTLT && (year2-year1) < GT_LT_YEAR_DIFF)
                etYear2.setText(increaseOrDecreaseStringNumber(etYear1.text.toString(), true, GT_LT_YEAR_DIFF))
        }
        updateTextState()
    }

    //endregion

    companion object {
        private var destroyedMovieList = arrayListOf<MovieItemSearch>()
        private var catSecSelected = 0
        private var catPriSelected = 0
        private var sortByMainSelected = 0
        private var sortByDirSelected = 0
        private var sortByYearSelected = 0
        private var yearOne = Common.STRING_EMPTY
        private var yearTwo = Common.STRING_EMPTY
        private var page = Common.STRING_EMPTY
        private var restoreFromDestroyed = false
        private var maxPages = 0
        private const val ACCEPTED_YEAR_LENGTH = 4
        private const val LOWEST_ACCEPTED_YEAR = "1895"
        private const val STRING_GT = ">"
        private const val STRING_GTE = ">="
        private const val STRING_LTE = "<="
        private const val STRING_EQUAL = "="
        private const val STRING_LT = "<"
        private const val STRING_GTLT = "> <"
        private const val STRING_GTELTE = ">= <="
        private const val YEAR_END = "-12-31"
        private const val YEAR_START = "-01-01"
        private const val PRI_CAT_START_TEXT = "Select primary genre"
        private const val SEC_CAT_START_TEXT = "Select secondary genre"
        private const val SORT_MAIN_START_TEXT = "Select how to sort"
        private const val ONE = 1
        private const val ZERO = 0
        private const val CAT_NAME_ACT = "Action"
        private const val CAT_NAME_ADV = "Adventure"
        private const val CAT_NAME_ANI = "Animation"
        private const val CAT_NAME_COM = "Comedy"
        private const val CAT_NAME_CRI = "Crime"
        private const val CAT_NAME_DOC = "Documentary"
        private const val CAT_NAME_DRAMA = "Drama"
        private const val CAT_NAME_FAM = "Family"
        private const val CAT_NAME_FAN = "Fantasy"
        private const val CAT_NAME_HIS = "History"
        private const val CAT_NAME_HOR = "Horror"
        private const val CAT_NAME_MUS = "Music"
        private const val CAT_NAME_MYS = "Mystery"
        private const val CAT_NAME_ROM = "Romance"
        private const val CAT_NAME_SCIFI = "Science Fiction"
        private const val CAT_NAME_TV = "TV Movie"
        private const val CAT_NAME_THR = "Thriller"
        private const val CAT_NAME_WAR = "War"
        private const val CAT_NAME_WES = "Western"
        private const val CAT_ID_ACT = "28"
        private const val CAT_ID_ADV = "12"
        private const val CAT_ID_ANI = "16"
        private const val CAT_ID_COM = "35"
        private const val CAT_ID_CRI = "80"
        private const val CAT_ID_DOC = "99"
        private const val CAT_ID_DRAMA = "18"
        private const val CAT_ID_FAM = "10751"
        private const val CAT_ID_FAN = "14"
        private const val CAT_ID_HIS = "36"
        private const val CAT_ID_HOR = "27"
        private const val CAT_ID_MUS = "10402"
        private const val CAT_ID_MYS = "9648"
        private const val CAT_ID_ROM = "10749"
        private const val CAT_ID_SCIFI = "878"
        private const val CAT_ID_TV = "10770"
        private const val CAT_ID_THR = "53"
        private const val CAT_ID_WAR = "10752"
        private const val CAT_ID_WES = "37"
        private const val QUERY_GENRE_SPACING = ", "
        private const val SORT_MAIN_POP = "Popularity"
        private const val SORT_MAIN_REL = "Release"
        private const val SORT_MAIN_REV = "Revenue"
        private const val SORT_MAIN_TI = "Title"
        private const val SORT_MAIN_SCO = "Score"
        private const val SORT_MAIN_VC = "Vote Count"
        private const val SORT_MAIN_REL_QUERY = "primary_release_date"
        private const val SORT_MAIN_TI_QUERY = "original_title"
        private const val SORT_MAIN_SCO_QUERY = "vote_average"
        private const val SORT_MAIN_VC_QUERY = "vote_count"
        private const val SORT_DIR_DESC = "Descending"
        private const val SORT_DIR_ASC = "Ascending"
        private const val SORT_DIR_ASC_QUERY = ".asc"
        private const val SORT_DIR_DESC_QUERY = ".desc"
        private const val KEYBOARD_FLAGS = 0
        private const val GT_LT_YEAR_DIFF = 2
    }
}
