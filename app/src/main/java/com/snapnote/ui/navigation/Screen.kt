package com.snapnote.ui.navigation

sealed class Screen(val route: String) {
    data object Scan : Screen("scan")
    data object Notes : Screen("notes")
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }
    data object AddImages : Screen("add_images/{noteId}") {
        fun createRoute(noteId: Long) = "add_images/$noteId"
    }
    data object KnowledgeGraph : Screen("knowledge_graph/{knowledgePointId}") {
        fun createRoute(knowledgePointId: Long) = "knowledge_graph/$knowledgePointId"
    }
    data object Relations : Screen("relations")
    data object Profile : Screen("profile")
}
