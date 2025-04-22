package com.example.objectscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.IOException

class RecipeRecommendationActivity : AppCompatActivity() {

    private lateinit var recommendationsTextView: TextView
    private lateinit var webView: WebView
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
        webView = findViewById(R.id.webView)
        goBackBtn = findViewById(R.id.goBackBtn)
    }

    private fun handleIntent() {
        try {
            val detectedIngredients = intent.getStringArrayListExtra("detectedIngredients")?.map { it.trim().lowercase() }
                ?: emptyList()

            Log.d(TAG, "Normalized Detected Ingredients: $detectedIngredients")

            if (detectedIngredients.isEmpty()) {
                recommendationsTextView.text = "No ingredients detected."
            } else {
                recommendRecipes(detectedIngredients)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent: ${e.message}")
            recommendationsTextView.text = "Error processing ingredients."
        }
    }

    private fun setupBackButton() {
        goBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun recommendRecipes(detectedIngredients: List<String>) {
        try {
            val recipes = parseRecipesFromJson()
            if (recipes.isEmpty()) {
                recommendationsTextView.text = "No recipes available."
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
                recommendationsTextView.text = "No recipes found for the detected ingredients."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recommending recipes: ${e.message}")
            recommendationsTextView.text = "Error finding recipes."
        }
    }

    private fun displayRecipes(matchedRecipes: List<RecipeWithDetails>) {
        try {
            val recommendationsHtml = matchedRecipes
                .sortedByDescending { it.matchedIngredients.size }
                .joinToString("<br><br>") { recipe ->
                    val zeptoLinks = recipe.missingIngredients.joinToString("<br>") { ingredient ->
                        "<a href='https://www.zeptonow.com/search?query=$ingredient'>Zepto Link for $ingredient</a>"
                    }

                    """
                    <b>${recipe.name}:</b><br>
                    <a href="${recipe.link}">${recipe.link}</a><br>
                    Matched Ingredients: ${recipe.matchedIngredients.joinToString(", ")}<br>
                    Missing Ingredients: ${recipe.missingIngredients.joinToString(", ")}<br>
                    $zeptoLinks
                    """.trimIndent()
                }

            webView.settings.javaScriptEnabled = true
            webView.webViewClient = CustomWebViewClient()
            webView.loadDataWithBaseURL(null, recommendationsHtml, "text/html; charset=UTF-8", "UTF-8", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying recipes: ${e.message}")
            recommendationsTextView.text = "Error displaying recipes."
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
            Toast.makeText(this, "Error reading recipe file", Toast.LENGTH_LONG).show()
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

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    // Always try to open in external app
                    startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening URL: ${e.message}")
                    Toast.makeText(this@RecipeRecommendationActivity, "Error opening link", Toast.LENGTH_SHORT).show()
                }
            }
            return false
        }
    }

    companion object {
        private const val TAG = "RecipeRecommendation"
    }
}