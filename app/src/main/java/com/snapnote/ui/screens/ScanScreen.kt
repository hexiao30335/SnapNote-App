package com.snapnote.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.snapnote.data.model.ScanStatus
import com.snapnote.ui.components.ContentTypeChip
import com.snapnote.ui.components.SnapButton
import com.snapnote.ui.components.SnapCard
import com.snapnote.ui.components.SnapInput
import com.snapnote.ui.navigation.Screen
import com.snapnote.ui.theme.Background
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.viewmodel.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ScanViewModel = viewModel { ScanViewModel(context) }
    val selectedImages = viewModel.selectedImages
    val isProcessing = viewModel.isProcessing.value
    val noteTitle = viewModel.noteTitle.value
    val scanResults = viewModel.scanResults
    val currentNoteId = viewModel.currentNoteId.value

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.selectImages(uris)
        }
    }

    LaunchedEffect(currentNoteId) {
        if (currentNoteId != null && scanResults.isNotEmpty() && !isProcessing) {
            navController.navigate(Screen.NoteDetail.createRoute(currentNoteId)) {
                popUpTo(Screen.Scan.route) { inclusive = false }
            }
            viewModel.clearSelection()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描导入") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "选择图片",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "从相册选择截图或纸质资料照片，AI 将自动识别并提取知识点",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SnapInput(
                    value = noteTitle,
                    onValueChange = viewModel::setNoteTitle,
                    label = "笔记标题（可选）"
                )
            }

            item {
                ImagePickerArea(
                    selectedCount = selectedImages.size,
                    onPickClick = { imagePicker.launch("image/*") }
                )
            }

            if (selectedImages.isNotEmpty()) {
                item {
                    Text(
                        text = "已选择 ${selectedImages.size} 张图片",
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary
                    )
                }
            }

            if (isProcessing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("AI 正在解析图片...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (scanResults.isNotEmpty()) {
                item {
                    Text(
                        text = "解析结果",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(scanResults) { result ->
                    ScanResultCard(result = result)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedImages.isNotEmpty() && !isProcessing) {
                    SnapButton(
                        text = if (noteTitle.isNotBlank()) "创建笔记并解析" else "创建新笔记并解析",
                        onClick = { viewModel.createNoteAndScan(context) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ImagePickerArea(
    selectedCount: Int,
    onPickClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        SnapCard(
            onClick = onPickClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "选择图片",
                    modifier = Modifier.size(48.dp),
                    tint = Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedCount == 0) "点击选择图片" else "已选 $selectedCount 张，点击重新选择",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScanResultCard(result: com.snapnote.data.model.ScanResult) {
    SnapCard {
        Column {
            Text(
                text = result.fileName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (result.status == ScanStatus.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "完成",
                    tint = com.snapnote.ui.theme.Success,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.extractedKnowledgePoints.forEach { point ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = point.title,
                            style = MaterialTheme.typography.titleSmall
                        )
                        ContentTypeChip(contentType = point.contentType)
                        Text(
                            text = point.content.take(100) + if (point.content.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
