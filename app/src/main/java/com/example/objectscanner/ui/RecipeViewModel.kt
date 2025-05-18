package com.example.objectscanner.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectscanner.R
import com.example.objectscanner.RecipeRecommendationActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

data class RecipeScreenState(
    val headerText: String = "",
    val recipes: List<RecipeRecommendationActivity.RecipeWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipeViewModel : ViewModel() {
    var state by mutableStateOf(RecipeScreenState())
        private set
    
    companion object {
        private var cachedRecipes: List<RecipeRecommendationActivity.Recipe>? = null
    }
    
    fun loadRecipes(detectedIngredients: List<String>, context: Context) {
        if (detectedIngredients.isEmpty()) {
            state = state.copy(
                headerText = context.getString(R.string.no_ingredients_detected),
                recipes = emptyList()
            )
            return
        }
        
        state = state.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val detectedIngredientsSet = detectedIngredients.toHashSet()
                val recipes = parseRecipesFromJson(context)
                
                if (recipes.isEmpty()) {
                    state = state.copy(
                        headerText = context.getString(R.string.no_recipes_found),
                        recipes = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }
                
                val matchedRecipes = recipes.mapNotNull { recipe ->
                    val normalizedRecipeIngredients = recipe.ingredients.map { it.trim().lowercase() }
                    val matchedIngredients = normalizedRecipeIngredients.filter { it in detectedIngredientsSet }
                    if (matchedIngredients.isEmpty()) return@mapNotNull null

                    val missingIngredients = normalizedRecipeIngredients.filterNot { it in detectedIngredientsSet }
                    RecipeRecommendationActivity.RecipeWithDetails(recipe.name, recipe.link, matchedIngredients, missingIngredients)
                }
                
                if (matchedRecipes.isNotEmpty()) {
                    // Sort recipes by number of matched ingredients (descending)
                    val sortedRecipes = withContext(Dispatchers.Default) {
                        matchedRecipes.sortedByDescending { it.matchedIngredients.size }
                    }
                    
                    state = state.copy(
                        headerText = context.getString(R.string.recipe_recommendations),
                        recipes = sortedRecipes,
                        isLoading = false
                    )
                } else {
                    state = state.copy(
                        headerText = context.getString(R.string.no_recipes_found),
                        recipes = emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    headerText = context.getString(R.string.error_fetching_recipes),
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    private suspend fun parseRecipesFromJson(context: Context): List<RecipeRecommendationActivity.Recipe> = withContext(Dispatchers.IO) {
        cachedRecipes?.let { return@withContext it }

        return@withContext try {
            val inputStream = context.assets.open("dataset.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val recipes = gson.fromJson(json, Array<RecipeRecommendationActivity.Recipe>::class.java).toList()

            cachedRecipes = recipes
            recipes
        } catch (_: IOException) {
            emptyList()
        }
    }
}
