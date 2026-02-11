package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csd3156.mobileproject.MovieReviewApp.R
import com.csd3156.mobileproject.MovieReviewApp.ui.components.Sections
import kotlinx.serialization.Serializable

@Serializable
data object Main

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Main Screen UI Here
    // Div Start
    Column(modifier = modifier) {
        TitleSection()
        Sections("Reccomended for You",
            null,
            modifier=Modifier.padding(16.dp)
        )


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun movieSearchBar(
    onQueryChange: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (value: String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = modifier,
                query = query,
                onQueryChange = {
                    query = it
                    onQueryChange(query)
                },
                onSearch = { onSearch(query) },
                expanded = false,
                onExpandedChange = { active = it },
                placeholder = { Text("Search") }
            )
        },
        expanded = false,
        onExpandedChange = { active = it }
    ) {
        // Expanded content — intentionally empty
    }
}

@Composable
fun TitleSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Sections("Discover", "What to watch today?", Modifier.padding(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        makeProfileIcon(
            R.drawable.ic_launcher_background, Modifier
                .padding(16.dp)
                .size(64.dp)
        )

    }
    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        movieSearchBar({ query ->

        }) { value ->
            print(value)
        }
    }
}

@Composable
fun makeProfileIcon(drawableId: Int, modifier: Modifier) {
    Icon(
        painter = painterResource(id = drawableId),
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
    )
}