package com.example.objectscanner.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.objectscanner.R
import com.example.objectscanner.ui.theme.AppColors

@Composable
fun MainScreen(
    state: MainScreenState, // Assuming MainScreenState is defined
    onEvent: (MainScreenEvent) -> Unit // Assuming MainScreenEvent is defined
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 16.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top content area with fixed weight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(top = 8.dp), // Additional top padding for safety
                verticalArrangement = Arrangement.Center
            ) {
                // Image Preview - takes most of the top area
                ImagePreview(
                    bitmap = state.currentBitmap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                        .padding(vertical = 8.dp)
                )
                // Label Text - takes smaller portion of top area
                Text(
                    text = state.labelText.ifEmpty {
                        stringResource(R.string.detected_labels_will_appear_here)
                    },
                    color = AppColors.Blue,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .padding(vertical = 8.dp)
                )
            }
            // Button section with fixed weight and spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Fixed spacing between buttons
            ) {
                // Button Section with fixed sizing
                CaptureButton(
                    enabled = state.currentBitmap == null && !state.isProcessing,
                    onClick = { onEvent(MainScreenEvent.CaptureImage) }
                )

                ResetButton(
                    enabled = state.currentBitmap != null && !state.isProcessing,
                    onClick = { onEvent(MainScreenEvent.ResetDetection) }
                )

                NextButton(
                    enabled = state.currentBitmap != null && !state.isProcessing,
                    onClick = { onEvent(MainScreenEvent.NextIngredient) }
                )

                RecommendButton(
                    enabled = state.detectedIngredients.isNotEmpty() && !state.isProcessing,
                    onClick = { onEvent(MainScreenEvent.RecommendRecipes) }
                )
            } // End of button section
        } // End of main Column
    } // End of Surface
}

@Composable
private fun ImagePreview(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    // Use a consistent content scale
    val imageContentScale = ContentScale.Fit
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            // Display captured image with fixed content scale
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.preview_of_the_captured_image),
                contentScale = imageContentScale,
                modifier = Modifier.fillMaxSize(0.95f) // Slightly smaller to avoid edge issues
            )
        } else {
            // For empty state, use a fixed placeholder icon size
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_image_24),
                    contentDescription = stringResource(R.string.preview_of_the_captured_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize(0.5f) // Use fixed 50% size of the container
                )
            }
        }
    }
}

@Composable
private fun CaptureButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = AppColors.Blue,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.capture_image),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ResetButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = AppColors.Blue,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.reset),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun NextButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = AppColors.Blue,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.next_ingredient),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RecommendButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = AppColors.Blue,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.recommend_recipe),
            fontSize = 14.sp
        )
    }
}