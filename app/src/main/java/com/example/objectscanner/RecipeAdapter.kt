package com.example.objectscanner

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RecipeAdapter(
    private val recipes: List<RecipeRecommendationActivity.RecipeWithDetails>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        private val recipeLinkTextView: TextView = itemView.findViewById(R.id.recipeLinkTextView)
        private val matchedIngredientsTextView: TextView = itemView.findViewById(R.id.matchedIngredientsTextView)
        private val missingIngredientsTextView: TextView = itemView.findViewById(R.id.missingIngredientsTextView)
        private val ingredientButtonsContainer: LinearLayout = itemView.findViewById(R.id.ingredientButtonsContainer)

        fun bind(recipe: RecipeRecommendationActivity.RecipeWithDetails) {
            val context = itemView.context

            // Set recipe name
            recipeNameTextView.text = recipe.name

            // Set recipe link with click listener to open in external browser
            recipeLinkTextView.text = recipe.link
            recipeLinkTextView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, recipe.link.toUri())
                context.startActivity(intent)
            }

            // Set matched and missing ingredients
            matchedIngredientsTextView.text = recipe.matchedIngredients.joinToString(", ")
            missingIngredientsTextView.text = recipe.missingIngredients.joinToString(", ")

            // Clear previous buttons and add new ones for missing ingredients
            ingredientButtonsContainer.removeAllViews()

            recipe.missingIngredients.forEach { ingredient ->
                val button = MaterialButton(context).apply {
                    text = context.getString(R.string.buy_ingredient, ingredient)
                    contentDescription = context.getString(R.string.buy_ingredient_accessibility, ingredient)

                    // Force apply background and text colors for visibility in both themes
                    setBackgroundColor(context.getColor(R.color.blue))
                    setTextColor(context.getColor(R.color.white))

                    // Open Zepto link
                    setOnClickListener {
                        val zeptoUrl = "https://www.zeptonow.com/search?query=$ingredient"
                        val intent = Intent(Intent.ACTION_VIEW, zeptoUrl.toUri())
                        context.startActivity(intent)
                    }

                    // Appearance tweaks
                    textSize = 12f
                    setPadding(12, 6, 12, 6)

                    minWidth = 0
                    minHeight = (36 * context.resources.displayMetrics.density).toInt()
                    cornerRadius = (20 * context.resources.displayMetrics.density).toInt()

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 12, 0)
                    }
                    this.layoutParams = layoutParams
                }

                ingredientButtonsContainer.addView(button)
            }
        }
    }
}
