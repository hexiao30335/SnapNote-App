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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.snapnote.ui.components.SnapInput
import com.snapnote.ui.navigation.Screen
import com.snapnote.ui.theme.Background
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.viewmodel.RelationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationsScreen(
    navController: NavController,
    viewModel: RelationsViewModel = viewModel { RelationsViewModel(LocalContext.current) }
) {
    val knowledgePoints by viewModel.knowledgePoints.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load all knowledge points on init
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Search with empty query to get all
        viewModel.searchKnowledgePoints("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("知识关联") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SnapInput(
                value = "",
                onValueChange = { viewModel.searchKnowledgePoints(it) },
                label = "搜索知识点",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading && knowledgePoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (knowledgePoints.isEmpty()) {
                EmptyRelationsView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(knowledgePoints) { point ->
                        KnowledgePointRelationItem(
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
}

@Composable
private fun KnowledgePointRelationItem(
    point: KnowledgePoint,
    onClick: () -> Unit
) {
    SnapCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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
                if (point.relations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
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
private fun EmptyRelationsView() {
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
            text = "先扫描一些图片，知识点关联会自动生成",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}


