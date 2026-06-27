package com.snapnote

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.snapnote.ui.components.BottomNavBar
import com.snapnote.ui.navigation.SnapNoteNavGraph
import com.snapnote.ui.theme.SnapNoteTheme

@Composable
fun SnapNoteApp() {
    SnapNoteTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavBar(navController = navController)
            }
        ) { paddingValues ->
            SnapNoteNavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
