package de.kitshn.ui.dialog.recipe.creationandedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.dialog.select.KeywordCheckedListItem
import de.kitshn.ui.dialog.select.KeywordSearchBar
import de.kitshn.ui.layout.ResponsiveSideBySideLayout

@Composable
fun KeywordsPage(
    client: TandoorClient,
    values: RecipeCreationAndEditDialogValue
) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        BoxWithConstraints(
            Modifier.padding(16.dp)
        ) {
            ResponsiveSideBySideLayout(
                showDivider = true,

                leftMinWidth = 200.dp,
                rightMinWidth = 200.dp,

                maxHeight = 800.dp,

                leftLayout = { enoughSpace ->
                    Box(
                        Modifier.height(
                            if(enoughSpace)
                                this@BoxWithConstraints.maxHeight
                            else
                                (this@BoxWithConstraints.maxHeight - 32.dp) / 2f
                        ),
                    ) {
                        KeywordSearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            client = client,
                            selectedKeywords = values.keywords
                        ) { keyword, keywordId, value ->
                            if(value) {
                                values.keywords.add(0, keyword)
                            } else {
                                values.keywords.removeIf { it.id == keywordId }
                            }
                        }
                    }
                }
            ) {
                Box(
                    Modifier.fillMaxHeight()
                ) {
                    if(values.keywords.size == 0) {
                        FullSizeAlertPane(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.recipe_edit_no_keywords_added),
                            text = stringResource(R.string.recipe_edit_no_keywords_added)
                        )
                    } else {
                        LazyColumn(
                            Modifier.clip(RoundedCornerShape(16.dp))
                        ) {
                            items(values.keywords.size, key = { values.keywords[it].id }) {
                                val keyword = values.keywords[it]

                                KeywordCheckedListItem(
                                    Modifier,
                                    checked = true,
                                    keyword = keyword
                                ) {
                                    values.keywords.remove(keyword)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}