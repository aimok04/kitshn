package de.kitshn.ui.component.model.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.toColorInt

@Composable
fun ShoppingListColorPill(
    shoppingList: TandoorShoppingList
) {
    shoppingList.color?.toColorInt()?.let {
        Box(
            Modifier.height(24.dp)
                .width(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Color(it)
                )
        )
    }
}