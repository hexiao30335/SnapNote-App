package com.snapnote.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.snapnote.data.model.KnowledgePoint
import com.snapnote.ui.components.ContentTypeChip
import com.snapnote.ui.components.KnowledgeAvatar
import com.snapnote.ui.components.SnapCard
import com.snapnote.ui.navigation.Screen
import com.snapnote.ui.theme.Background
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.viewmodel.NotesViewModel
import com.snapnote.ui.viewmodel.RelationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val notesViewModel: NotesViewModel = viewModel { NotesViewModel(context) }
    val relationsViewModel: RelationsViewModel = viewModel { RelationsViewModel(context) }
    val note = notesViewModel.notes.collectAsState().value.find { it.id == noteId }
    val knowledgePoints by relationsViewModel.knowledgePoints.collectAsState()

    LaunchedEffect(noteId) {
        relationsViewModel.loadKnowledgePoints(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note?.title ?: "笔记详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddImages.createRoute(noteId)) },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加图片")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (knowledgePoints.isEmpty()) {
                item {
                    EmptyKnowledgePointsView()
                }
            } else {
                item {
                    Text(
                        text = "知识点列表 (${knowledgePoints.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(knowledgePoints) { point ->
                    KnowledgePointCard(
                        point = point,
                        onClick = {
                            navController.navigate(Screen.KnowledgeGraph.createRoute(point.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KnowledgePointCard(
    point: KnowledgePoint,
    onClick: () -> Unit
) {
    SnapCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            KnowledgeAvatar(contentType = point.contentType)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                ContentTypeChip(contentType = point.contentType)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = point.content.take(120) + if (point.content.length > 120) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (point.relations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${point.relations.size} 个关联",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyKnowledgePointsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "暂无知识点",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮添加图片，AI 将自动提取知识点",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
