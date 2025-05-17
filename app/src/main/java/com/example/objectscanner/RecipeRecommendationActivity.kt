package com.example.objectscanner

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.IOException

class RecipeRecommendationActivity : AppCompatActivity() {

    private lateinit var recommendationsTextView: TextView
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var goBackBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_recommendation)

        initializeViews()
        handleIntent()
        setupBackButton()
    }

    private fun initializeViews() {
        recommendationsTextView = findViewById(R.id.recommendationsTextView)
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        goBackBtn = findViewById(R.id.goBackBtn)
        
        // Setup RecyclerView
        recipesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun handleIntent() {
        try {
            val detectedIngredients = intent.getStringArrayListExtra("detectedIngredients")?.map { it.trim().lowercase() }
                ?: emptyList()

            Log.d(TAG, "Normalized Detected Ingredients: $detectedIngredients")

            if (detectedIngredients.isEmpty()) {
                recommendationsTextView.text = getString(R.string.no_ingredients_detected)
            } else {
                recommendRecipes(detectedIngredients)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent: ${e.message}")
            recommendationsTextView.text = getString(R.string.error_processing_ingredients)
        }
    }

    private fun setupBackButton() {
        goBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun recommendRecipes(detectedIngredients: List<String>) {
        try {
            val recipes = parseRecipesFromJson()
            if (recipes.isEmpty()) {
                recommendationsTextView.text = getString(R.string.no_recipes_found)
                return
            }

            val matchedRecipes = recipes.map { recipe ->
                val normalizedRecipeIngredients = recipe.ingredients.map { it.trim().lowercase() }
                val matchedIngredients = normalizedRecipeIngredients.filter { it in detectedIngredients }
                val missingIngredients = normalizedRecipeIngredients.filterNot { it in detectedIngredients }
                RecipeWithDetails(recipe.name, recipe.link, matchedIngredients, missingIngredients)
            }.filter { it.matchedIngredients.isNotEmpty() }

            Log.d(TAG, "Matched Recipes: ${matchedRecipes.map { it.name }}")

            if (matchedRecipes.isNotEmpty()) {
                displayRecipes(matchedRecipes)
            } else {
                recommendationsTextView.text = getString(R.string.no_recipes_found)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recommending recipes: ${e.message}")
            recommendationsTextView.text = getString(R.string.error_fetching_recipes)
        }
    }    private fun displayRecipes(matchedRecipes: List<RecipeWithDetails>) {
        try {
            // Sort recipes by number of matched ingredients (descending)
            val sortedRecipes = matchedRecipes.sortedByDescending { it.matchedIngredients.size }
            
            // Set RecyclerView adapter
            val adapter = RecipeAdapter(sortedRecipes)
            recipesRecyclerView.adapter = adapter
            
            // Update header text
            recommendationsTextView.text = getString(R.string.recipe_recommendations)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying recipes: ${e.message}")
            recommendationsTextView.text = getString(R.string.error_parsing_response)
        }
    }

    private fun parseRecipesFromJson(): List<Recipe> {
        return try {
            val inputStream = assets.open("dataset.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            gson.fromJson(json, Array<Recipe>::class.java).toList()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading recipe file: ${e.message}")
            Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    data class Recipe(
        val name: String,
        val ingredients: List<String>,
        val link: String
    )

    data class RecipeWithDetails(
        val name: String,
        val link: String,
        val matchedIngredients: List<String>,
        val missingIngredients: List<String>
    )
    
    companion object {
        private const val TAG = "RecipeRecommendation"
    }
}