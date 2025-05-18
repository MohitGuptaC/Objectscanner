package com.example.objectscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.objectscanner.ui.RecipeScreen
import com.example.objectscanner.ui.RecipeViewModel
import com.example.objectscanner.ui.theme.ObjectScannerTheme

class RecipeRecommendationActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ObjectScannerTheme {
                RecipeScreen(
                    state = viewModel.state,
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
        
        handleIntent()
    }    
    
    private fun handleIntent() {
        try {
            val detectedIngredients = intent.getStringArrayListExtra("detectedIngredients")?.map { it.trim().lowercase() }
                ?: emptyList()

            Log.d(TAG, "Normalized Detected Ingredients: $detectedIngredients")
            
            viewModel.loadRecipes(detectedIngredients, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent: ${e.message}")
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