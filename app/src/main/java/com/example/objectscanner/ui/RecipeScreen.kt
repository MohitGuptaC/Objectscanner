package com.example.objectscanner.ui

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.objectscanner.R
import com.example.objectscanner.RecipeRecommendationActivity
import com.example.objectscanner.ui.theme.AppColors
import com.example.objectscanner.ui.theme.ObjectScannerTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun RecipeScreen(
    state: RecipeScreenState,
    onBackPressed: () -> Unit,
    previewMode: Boolean = false
) {
    // Get the navigation and status bars insets to properly position UI elements
    val navigationBarsInsets = WindowInsets.navigationBars

    // Use the density to get values in DP
    val density = LocalDensity.current
    val navigationBarsPadding = with(density) {
        navigationBarsInsets.asPaddingValues().calculateBottomPadding()
    }

    Scaffold(
        // Apply system bars padding at the Scaffold level
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Add padding that considers the navigation bar height
                    .padding(bottom = navigationBarsPadding)
            ) {
                Button(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue)
                ) {
                    Text(
                        text = stringResource(R.string.go_back),
                        fontSize = 16.sp,
                        color = AppColors.White
                    )
                }
            }
        }
    ) { paddingValues ->
        // The paddingValues from Scaffold now includes system bars padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
        ) {
            Text(
                text = state.headerText.ifEmpty { stringResource(R.string.recipe_recommendations) },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            when {
                state.isLoading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }
                }
                state.recipes.isEmpty() -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = state.error ?: stringResource(R.string.no_recipes_found),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.recipes) { recipe ->
                            RecipeCard(recipe = recipe, previewMode = previewMode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeRecommendationActivity.RecipeWithDetails, previewMode: Boolean = false) {
    val context = if (!previewMode) LocalContext.current else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = recipe.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (recipe.link.isNotBlank()) {
                if (previewMode) {
                    Text(
                        text = recipe.link,
                        color = AppColors.Blue,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    )
                } else {
                    Text(
                        text = recipe.link,
                        color = AppColors.Blue,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, recipe.link.toUri())
                                context?.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    context?.getString(R.string.error_opening_link) ?: "Error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.matched_ingredients_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = " " + if (recipe.matchedIngredients.isNotEmpty()) {
                        recipe.matchedIngredients.joinToString(", ")
                    } else {
                        stringResource(R.string.no_matched_ingredients)
                    },
                    fontSize = 14.sp
                )
            }

            if (recipe.missingIngredients.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(R.string.missing_ingredients_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        recipe.missingIngredients.forEach { ingredient ->
                            BuyIngredientButton(ingredient = ingredient, previewMode = previewMode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuyIngredientButton(ingredient: String, previewMode: Boolean = false) {
    val context = if (!previewMode) LocalContext.current else null
    Button(
        onClick = {
            if (!previewMode) {
                try {
                    val encodedIngredient = URLEncoder.encode(ingredient, StandardCharsets.UTF_8.toString())
                    val zeptoUrl = "https://www.zeptonow.com/search?query=$encodedIngredient"
                    val intent = Intent(Intent.ACTION_VIEW, zeptoUrl.toUri())
                    context?.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        context?.getString(R.string.error_opening_shopping_link) ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue)
    ) {
        Text(
            text = stringResource(R.string.buy_ingredient, ingredient),
            fontSize = 12.sp,
            color = AppColors.White
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:parent=pixel_8,navigation=buttons",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun RecipeScreenPreviewWithButtons() {
    ObjectScannerTheme {
        RecipeScreen(
            state = createPreviewRecipeState(),
            onBackPressed = {},
            previewMode = true
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:parent=pixel_8,navigation=gesture",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun RecipeScreenPreviewWithGestures() {
    ObjectScannerTheme {
        RecipeScreen(
            state = createPreviewRecipeState(),
            onBackPressed = {},
            previewMode = true
        )
    }
}

// Helper function to create preview state and avoid code duplication
private fun createPreviewRecipeState() = RecipeScreenState(
    headerText = "Recipe Recommendations",
    isLoading = false,
    recipes = listOf(
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "Potato Chips",
            link = "https://www.youtube.com/results?search_query=potato+chips",
            matchedIngredients = listOf("potato, tomato"),
            missingIngredients = listOf("chilli powder")
        ),
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "Veg Pulao",
            link = "https://www.youtube.com/results?search_query=veg+pulao",
            matchedIngredients = listOf("potato"),
            missingIngredients = listOf("tomato", "carrot", "bean")
        ),
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "French Fries",
            link = "https://www.youtube.com/results?search_query=french+fries",
            matchedIngredients = listOf("potato"),
            missingIngredients = listOf("oil", "salt")
        ),
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "Aloo Tikki",
            link = "https://www.youtube.com/results?search_query=aloo+tikki",
            matchedIngredients = listOf("potato"),
            missingIngredients = listOf("spices", "bread crumbs")
        ),
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "Potato Curry",
            link = "https://www.youtube.com/results?search_query=potato+curry",
            matchedIngredients = listOf("potato"),
            missingIngredients = listOf("onion", "tomato", "spices")
        ),
        RecipeRecommendationActivity.RecipeWithDetails(
            name = "Mashed Potatoes",
            link = "https://www.youtube.com/results?search_query=mashed+potatoes",
            matchedIngredients = listOf("potato"),
            missingIngredients = listOf("butter", "milk", "salt")
        )
    ),
    error = null
)