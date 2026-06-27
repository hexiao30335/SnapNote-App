package com.snapnote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.snapnote.ui.screens.AddImagesScreen
import com.snapnote.ui.screens.KnowledgeGraphScreen
import com.snapnote.ui.screens.NoteDetailScreen
import com.snapnote.ui.screens.NotesScreen
import com.snapnote.ui.screens.ProfileScreen
import com.snapnote.ui.screens.RelationsScreen
import com.snapnote.ui.screens.ScanScreen

@Composable
fun SnapNoteNavGraph(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scan.route,
        modifier = modifier
    ) {
        composable(Screen.Scan.route) {
            ScanScreen(navController = navController)
        }
        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            NoteDetailScreen(
                noteId = noteId,
                navController = navController
            )
        }
        composable(
            route = Screen.AddImages.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            AddImagesScreen(
                noteId = noteId,
                navController = navController
            )
        }
        composable(
            route = Screen.KnowledgeGraph.route,
            arguments = listOf(navArgument("knowledgePointId") { type = NavType.LongType })
        ) { backStackEntry ->
            val kpId = backStackEntry.arguments?.getLong("knowledgePointId") ?: 0L
            KnowledgeGraphScreen(
                knowledgePointId = kpId,
                navController = navController
            )
        }
        composable(Screen.Relations.route) {
            RelationsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}
