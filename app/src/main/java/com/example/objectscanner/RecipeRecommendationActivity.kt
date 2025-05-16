package com.example.objectscanner

import android.content.Intent
import androidx.core.net.toUri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    }

    private fun displayRecipes(matchedRecipes: List<RecipeWithDetails>) {
        try {
            // Get colors from resources - these will adapt to dark or light mode automatically
            val backgroundColor = ContextCompat.getColor(this, R.color.backgroundcolor)
            val textColor = ContextCompat.getColor(this, R.color.black)
            val cardBackgroundColor = ContextCompat.getColor(this, R.color.backgroundcolor)
            val linkColor = ContextCompat.getColor(this, R.color.blue)
            val buttonColor = ContextCompat.getColor(this, R.color.blue)
            val buttonTextColor = ContextCompat.getColor(this, R.color.white)

            // Convert colors to hex strings for CSS
            val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColor)
            val textColorHex = String.format("#%06X", 0xFFFFFF and textColor)
            val cardBackgroundColorHex = String.format("#%06X", 0xFFFFFF and cardBackgroundColor)
            val linkColorHex = String.format("#%06X", 0xFFFFFF and linkColor)
            val buttonColorHex = String.format("#%06X", 0xFFFFFF and buttonColor)
            val buttonTextColorHex = String.format("#%06X", 0xFFFFFF and buttonTextColor)

            val htmlHeader = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            background-color: $backgroundColorHex;
                            color: $textColorHex;
                            font-family: sans-serif;
                            padding: 12px;
                            margin: 0;
                        }
                        .recipe-card {
                            background-color: $cardBackgroundColorHex;
                            border-radius: 8px;
                            padding: 16px;
                            margin-bottom: 16px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .recipe-title {
                            font-size: 18px;
                            font-weight: bold;
                            margin-bottom: 8px;
                        }                        .recipe-link {
                            color: $linkColorHex;
                            text-decoration: none;
                            margin-bottom: 8px;
                            display: block;
                            word-break: break-all;
                        }
                        .ingredient-section {
                            margin: 8px 0;
                        }
                        .ingredient-label {
                            font-weight: bold;
                            margin-right: 4px;
                        }
                        .button-container {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 8px;
                            margin-top: 12px;
                        }
                        .zepto-button {
                            background-color: $buttonColorHex;
                            color: $buttonTextColorHex;
                            border: none;
                            border-radius: 4px;
                            padding: 8px 12px;
                            text-align: center;
                            text-decoration: none;
                            display: inline-block;
                            font-size: 14px;
                            cursor: pointer;
                        }
                    </style>
                </head>
                <body>
            """.trimIndent()

            val recipeCards = matchedRecipes
                .sortedByDescending { it.matchedIngredients.size }
                .joinToString("") { recipe ->
                    val zeptoButtons = recipe.missingIngredients.joinToString("") { ingredient ->
                        """<a href='https://www.zeptonow.com/search?query=$ingredient' class='zepto-button'>Buy $ingredient</a>"""
                    }

                    """
                    <div class='recipe-card'>
                        <div class='recipe-title'>${recipe.name}</div>
                        <a href="${recipe.link}" class='recipe-link'>${recipe.link}</a>
                        
                        <div class='ingredient-section'>
                            <span class='ingredient-label'>Matched Ingredients:</span>
                            ${recipe.matchedIngredients.joinToString(", ")}
                        </div>
                        
                        <div class='ingredient-section'>
                            <span class='ingredient-label'>Missing Ingredients:</span>
                            ${recipe.missingIngredients.joinToString(", ")}
                        </div>
                        
                        <div class='button-container'>
                            $zeptoButtons
                        </div>
                    </div>
                    """.trimIndent()
                }

            val htmlFooter = """
                </body>
                </html>
            """.trimIndent()

            val fullHtml = htmlHeader + recipeCards + htmlFooter

            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = false
                allowFileAccess = false
                allowContentAccess = false
            }

            webView.webViewClient = CustomWebViewClient()
            webView.loadDataWithBaseURL(null, fullHtml, "text/html; charset=UTF-8", "UTF-8", null)

            recommendationsTextView.text = ""
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

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening URL: ${e.message}")
                Toast.makeText(this@RecipeRecommendationActivity, getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
            }
            return true
        }
    }

    companion object {
        private const val TAG = "RecipeRecommendation"
    }
}