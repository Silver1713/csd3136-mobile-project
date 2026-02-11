package com.csd3156.mobileproject.MovieReviewApp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Sections(title: String, desc : String?, modifier: Modifier = Modifier){
    Column (modifier=modifier){
        Text(title,
            modifier = Modifier,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (desc != null) {
            Text(
                desc,
                modifier = Modifier,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}