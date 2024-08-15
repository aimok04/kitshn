package de.kitshn.android.ui.view.home.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.model.TandoorFood
import de.kitshn.android.api.tandoor.model.TandoorKeyword
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import de.kitshn.android.ui.state.foreverRememberNotSavable
import kotlinx.coroutines.delay

@Composable
fun rememberHomeSearchState(
    key: String
): MutableState<HomeSearchState> {
    return foreverRememberNotSavable(
        key = key,
        initialValue = HomeSearchState()
    )
}

data class HomeSearchStateDefaultValues(
    val autoFocusSearchField: Boolean = true,
    val query: String = "",
    val new: Boolean = false,
    val random: Boolean = false,
    val keywords: List<TandoorKeyword> = listOf(),
    val foods: List<TandoorFood> = listOf(),
    val minimumRating: Int? = null,
    val sortOrder: TandoorRecipeQueryParametersSortOrder? = null
)

class HomeSearchState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var searchRequestState = TandoorRequestState()
    var extendedSearchRequestState = TandoorRequestState()

    var query by mutableStateOf("")

    val searchResultIds = mutableStateListOf<Int>()
    var currentPage by mutableIntStateOf(1)
    var nextPageExists by mutableStateOf(false)

    var defaultValuesApplied: Boolean = true
    var defaultValues = HomeSearchStateDefaultValues()

    var appliedAutoFocusSearchField: Boolean = false

    fun open() {
        this.appliedAutoFocusSearchField = false

        this.defaultValues = HomeSearchStateDefaultValues()
        this.shown.value = true
    }

    fun open(values: HomeSearchStateDefaultValues) {
        this.appliedAutoFocusSearchField = false

        this.defaultValuesApplied = false
        this.defaultValues = values
        this.shown.value = true
    }

    suspend fun openWithKeyword(client: TandoorClient, keywordOverview: TandoorKeywordOverview) {
        return openWithKeywordId(client, keywordOverview.id)
    }

    suspend fun openWithKeywordId(client: TandoorClient, keywordId: Int) {
        TandoorRequestState().wrapRequest {
            val keyword = client.container.keyword.getOrDefault(keywordId, null)
                ?: client.keyword.retrieve(keywordId)

            open(
                HomeSearchStateDefaultValues(
                    autoFocusSearchField = false,
                    keywords = listOf(keyword)
                )
            )
        }
    }

    fun dismiss() {
        this.shown.value = false
    }

    suspend fun reopen(open: suspend () -> Unit) {
        if(this.shown.value) {
            dismiss()
            delay(250)
        }

        open()
    }

}