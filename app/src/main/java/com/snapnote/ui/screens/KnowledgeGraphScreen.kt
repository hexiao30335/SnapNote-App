package com.snapnote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.snapnote.data.model.ContentType
import com.snapnote.data.model.KnowledgePoint
import com.snapnote.data.model.KnowledgePointRelation
import com.snapnote.ui.components.ContentTypeChip
import com.snapnote.ui.components.KnowledgeAvatar
import com.snapnote.ui.components.RelationAvatar
import com.snapnote.ui.components.RelationTypeChip
import com.snapnote.ui.components.SnapButton
import com.snapnote.ui.components.SnapCard
import com.snapnote.ui.components.SnapInput
import com.snapnote.ui.components.SnapInputMultiLine
import com.snapnote.ui.navigation.Screen
import com.snapnote.ui.theme.Background
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.viewmodel.RelationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeGraphScreen(
    knowledgePointId: Long,
    navController: NavController,
    viewModel: RelationsViewModel = viewModel { RelationsViewModel(LocalContext.current) }
) {
    val selectedPoint by viewModel.selectedPoint.collectAsState()
    val relations by viewModel.relations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEditing = viewModel.isEditing
    val deletedPoints by viewModel.deletedPoints.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(knowledgePointId) {
        viewModel.selectKnowledgePoint(knowledgePointId)
    }

    // 如果处于编辑模式，显示编辑UI
    if (isEditing && selectedPoint != null) {
        EditKnowledgePointScreen(
            title = viewModel.editTitle,
            content = viewModel.editContent,
            contentType = viewModel.editContentType,
            onTitleChange = viewModel::updateEditTitle,
            onContentChange = viewModel::updateEditContent,
            onContentTypeChange = viewModel::updateEditContentType,
            onSave = { viewModel.saveEditedPoint(knowledgePointId) },
            onCancel = { viewModel.stopEditing() }
        )
        return
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedPoint != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除知识点") },
            text = { Text("确定要删除「${selectedPoint!!.title}」吗？\n删除后可从回收站恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.softDeletePoint(selectedPoint!!)
                    showDeleteDialog = false
                    navController.navigateUp()
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    // 来源截图预览对话框
    if (showImageDialog && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("来源截图") },
            text = {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "来源截图",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showImageDialog = false }) { Text("关闭") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("知识关联图谱") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (selectedPoint != null) {
                        // 编辑按钮
                        IconButton(onClick = { viewModel.startEditing(selectedPoint!!) }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        // 删除按钮
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                        // 回收站按钮
                        if (deletedPoints.isNotEmpty()) {
                            IconButton(onClick = { showTrashDialog = true }) {
                                Text(
                                    text = "${deletedPoints.size}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ====== 知识点详情 ======
                item {
                    selectedPoint?.let { point ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                KnowledgeAvatar(contentType = point.contentType, size = 48)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = point.title,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ContentTypeChip(contentType = point.contentType)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = point.content,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )

                            // 来源截图回溯按钮
                            if (point.imageSourceId != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Primary.copy(alpha = 0.1f))
                                        .clickable {
                                            // TODO: 通过 imageSourceId 查询真实 URI
                                            selectedImageUri = "content://media/external/images/media/${point.imageSourceId}"
                                            showImageDialog = true
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "查看来源截图",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Primary
                                    )
                                }
                            }
                        }
                    }
                }

                // ====== 关联知识点列表 ======
                item {
                    Text(
                        text = "关联知识点 (${relations.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (relations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无关联知识点",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(relations.size) { index ->
                        EnhancedRelationCard(
                            relation = relations[index],
                            onViewSourceImage = { uri ->
                                selectedImageUri = uri
                                showImageDialog = true
                            },
                            onClick = { relatedPoint ->
                                // 点击关联知识点可跳转
                                navController.navigate(
                                    Screen.KnowledgeGraph.createRoute(relatedPoint.id)
                                )
                            }
                        )
                    }
                }

                // ====== 可视化知识图谱 ======
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "知识图谱",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EnhancedGraphVisualization(
                        selectedPoint = selectedPoint,
                        relations = relations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }
            }
        }
    }

    // 回收站对话框
    if (showTrashDialog) {
        AlertDialog(
            onDismissRequest = { showTrashDialog = false },
            title = { Text("回收站 (${deletedPoints.size})") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(deletedPoints) { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = point.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = point.contentType.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    viewModel.restorePoint(point)
                                }) {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = "恢复",
                                        tint = Primary
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.permanentlyDeletePoint(point)
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "永久删除",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.emptyTrash()
                        showTrashDialog = false
                    }) { Text("清空回收站") }
                    TextButton(onClick = { showTrashDialog = false }) { Text("关闭") }
                }
            }
        )
    }
}

@Composable
private fun EditKnowledgePointScreen(
    title: String,
    content: String,
    contentType: ContentType,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onContentTypeChange: (ContentType) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "编辑知识点",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SnapInput(
            value = title,
            onValueChange = onTitleChange,
            label = "标题"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SnapInputMultiLine(
            value = content,
            onValueChange = onContentChange,
            label = "内容"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "内容类型",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 内容类型选择器
        val types = ContentType.entries.filter { it != ContentType.UNKNOWN }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (contentType == type) Primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onContentTypeChange(type) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContentTypeChip(contentType = type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SnapOutlineButton(
                text = "取消",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            SnapButton(
                text = "保存",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SnapOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) { Text(text) }
}

@Composable
private fun EnhancedRelationCard(
    relation: KnowledgePointRelation,
    onViewSourceImage: (String) -> Unit,
    onClick: (KnowledgePoint) -> Unit
) {
    SnapCard(onClick = { onClick(relation.relatedPoint) }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RelationAvatar(relationType = relation.relationType)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = relation.relatedPoint.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                RelationTypeChip(relationType = relation.relationType)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 查看来源截图按钮
                if (relation.relatedPoint.imageSourceId != null) {
                    IconButton(onClick = { onViewSourceImage("content://media/external/images/media/${relation.relatedPoint.imageSourceId}") }) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "来源截图",
                            modifier = Modifier.size(20.dp),
                            tint = Primary
                        )
                    }
                }
                if (relation.isAutoGenerated) {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedGraphVisualization(
    selectedPoint: KnowledgePoint?,
    relations: List<KnowledgePointRelation>,
    modifier: Modifier = Modifier
) {
    if (selectedPoint == null || relations.isEmpty()) return

    val colors = listOf(
        com.snapnote.ui.theme.SubordinateColor,
        com.snapnote.ui.theme.PrerequisiteColor,
        com.snapnote.ui.theme.ConfusionColor,
        com.snapnote.ui.theme.CausalColor
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .background(Color(0xFFF8FAFC), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension * 0.35f

        // 绘制中心节点（当前知识点）
        drawCircle(
            color = Primary,
            radius = 28f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = 24f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        drawCircle(
            color = Primary,
            radius = 20f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // 中心节点文字标签
        drawContext.canvas.drawText(
            "当前",
            centerX - 12,
            centerY + 5,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                isAntiAlias = true
            }
        )

        // 绘制关联节点
        relations.forEachIndexed { index, relation ->
            val angle = (index * 360f / relations.size - 90) * (Math.PI / 180f)
            val x = centerX + (radius * kotlin.math.cos(angle)).toFloat()
            val y = centerY + (radius * kotlin.math.sin(angle)).toFloat()

            val color = colors.getOrElse(index % colors.size) { Primary }

            // 根据关系类型绘制不同线型
            val lineColor = when (relation.relationType) {
                com.snapnote.data.model.RelationType.PREREQUISITE -> com.snapnote.ui.theme.PrerequisiteColor
                com.snapnote.data.model.RelationType.CONFUSION -> com.snapnote.ui.theme.ConfusionColor
                com.snapnote.data.model.RelationType.CAUSAL -> com.snapnote.ui.theme.CausalColor
                com.snapnote.data.model.RelationType.SUBORDINATE -> com.snapnote.ui.theme.SubordinateColor
                else -> Primary
            }

            // 连接线（不同关系类型不同样式）
            drawLine(
                color = lineColor.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(x, y),
                strokeWidth = if (relation.strength == "strong") 3f else 2f
            )

            // 节点外圈
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = 26f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )

            // 节点实心
            drawCircle(
                color = color,
                radius = 20f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )

            // 节点标签（关联知识点编号）
            val label = relation.relatedPoint.number
            drawContext.canvas.drawText(
                label,
                x - if (label.length > 1) 10f else 6f,
                y + 6f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    isAntiAlias = true
                }
            )
        }

        // 图例
        val legendY = size.height - 20f
        val legendItems = listOf(
            "前置基础" to com.snapnote.ui.theme.PrerequisiteColor,
            "从属关系" to com.snapnote.ui.theme.SubordinateColor,
            "易混淆" to com.snapnote.ui.theme.ConfusionColor,
            "因果推导" to com.snapnote.ui.theme.CausalColor
        )
        var legendX = 8f
        for ((label, c) in legendItems) {
            drawCircle(color = c, radius = 6f, center = androidx.compose.ui.geometry.Offset(legendX + 6, legendY))
            drawContext.canvas.drawText(
                label,
                legendX + 16,
                legendY + 4,
                android.graphics.Paint().apply {
                    textSize = 16f
                    color = android.graphics.Color.parseColor("#64748B")
                    isAntiAlias = true
                }
            )
            legendX += 80f
        }
    }
}
