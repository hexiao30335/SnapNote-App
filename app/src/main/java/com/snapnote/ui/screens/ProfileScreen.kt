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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.snapnote.ui.components.SnapButton
import com.snapnote.ui.components.SnapInput
import com.snapnote.ui.theme.Background
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel(context) }
    val totalNotes by viewModel.totalNotes.collectAsState()
    val totalKnowledgePoints by viewModel.totalKnowledgePoints.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val nasStatus by viewModel.nasStatus.collectAsState()

    var nasUrl by remember { mutableStateOf("") }
    var nasUsername by remember { mutableStateOf("") }
    var nasPassword by remember { mutableStateOf("") }
    var showNasConfig by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatisticsCard(
                    totalNotes = totalNotes,
                    totalKnowledgePoints = totalKnowledgePoints
                )
            }

            item {
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExportCard(
                    onExportClick = { viewModel.exportAllToMarkdown() },
                    status = exportStatus
                )
            }

            item {
                NasSyncCard(
                    nasUrl = nasUrl,
                    onNasUrlChange = { nasUrl = it },
                    nasUsername = nasUsername,
                    onNasUsernameChange = { nasUsername = it },
                    nasPassword = nasPassword,
                    onNasPasswordChange = { nasPassword = it },
                    showConfig = showNasConfig,
                    onToggleConfig = { showNasConfig = !showNasConfig },
                    onSyncClick = {
                        viewModel.syncToNas(nasUrl, nasUsername, nasPassword)
                    },
                    status = nasStatus
                )
            }

            item {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                SettingsCard()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SnapNote v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(totalNotes: Int, totalKnowledgePoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "$totalNotes", label = "笔记")
            StatItem(value = "$totalKnowledgePoints", label = "知识点")
            StatItem(value = "0", label = "关联")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExportCard(onExportClick: () -> Unit, status: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = Primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "导出 Markdown",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "将所有笔记导出为 Markdown 文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SnapButton(
                text = "导出",
                onClick = onExportClick
            )
            status?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (it.contains("成功")) com.snapnote.ui.theme.Success else com.snapnote.ui.theme.Error
                )
            }
        }
    }
}

@Composable
private fun NasSyncCard(
    nasUrl: String,
    onNasUrlChange: (String) -> Unit,
    nasUsername: String,
    onNasUsernameChange: (String) -> Unit,
    nasPassword: String,
    onNasPasswordChange: (String) -> Unit,
    showConfig: Boolean,
    onToggleConfig: () -> Unit,
    onSyncClick: () -> Unit,
    status: String?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleConfig() }
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NAS 同步",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "配置 NAS 网络存储进行备份",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showConfig) {
                Spacer(modifier = Modifier.height(12.dp))
                SnapInput(
                    value = nasUrl,
                    onValueChange = onNasUrlChange,
                    label = "服务器地址"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SnapInput(
                    value = nasUsername,
                    onValueChange = onNasUsernameChange,
                    label = "用户名"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SnapInput(
                    value = nasPassword,
                    onValueChange = onNasPasswordChange,
                    label = "密码"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SnapButton(
                    text = "立即同步",
                    onClick = onSyncClick
                )
            }

            status?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (it.contains("成功")) com.snapnote.ui.theme.Success else com.snapnote.ui.theme.Error
                )
            }
        }
    }
}

@Composable
private fun SettingsCard() {
    var darkMode by remember { mutableStateOf(false) }
    var autoSync by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            ListItem(
                headlineContent = { Text("暗色模式") },
                leadingContent = {
                    Icon(Icons.Default.Settings, contentDescription = null)
                },
                trailingContent = {
                    Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                }
            )
            Divider()
            ListItem(
                headlineContent = { Text("自动同步") },
                supportingContent = { Text("退出应用时自动同步到 NAS") },
                leadingContent = {
                    Icon(Icons.Default.Cloud, contentDescription = null)
                },
                trailingContent = {
                    Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                }
            )
            Divider()
            ListItem(
                headlineContent = { Text("关于") },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
            )
        }
    }
}
