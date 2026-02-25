package com.example.idiompolisher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.idiompolisher.data.local.AppDatabase
import com.example.idiompolisher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 设置 - Settings Screen
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 顶部标题 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 偏好设置组 ──
        SettingsGroup(title = "偏好设置") {
            SettingsItem(
                icon = Icons.Outlined.Style,
                title = "润色风格",
                subtitle = "古风雅韵",
                onClick = { }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Palette,
                title = "主题模式",
                subtitle = "跟随系统",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 数据管理组 ──
        SettingsGroup(title = "数据管理") {
            SettingsItem(
                icon = Icons.Outlined.DeleteSweep,
                title = "清除历史记录",
                subtitle = "删除所有润色历史",
                onClick = { showClearDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 关于组 ──
        SettingsGroup(title = "关于") {
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "版本信息",
                subtitle = "v1.0.0",
                onClick = { }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Description,
                title = "开源许可",
                subtitle = "",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── 底部品牌 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "语感磨练",
                style = MaterialTheme.typography.titleSmall,
                color = InkLight.copy(alpha = 0.4f),
                fontFamily = NotoSerifSC,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(
                text = "以文会友，以墨传情",
                style = MaterialTheme.typography.labelSmall,
                color = InkLight.copy(alpha = 0.3f),
                letterSpacing = 2.sp
            )
        }
    }

    // 清除历史确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    "确认清除",
                    fontFamily = NotoSerifSC,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("确定要删除所有历史记录吗？此操作不可恢复。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            // Clear all records, using raw query since DAO doesn't have deleteAll
                            val allRecords = db.idiomDao().getAllHistory()
                            // We need a different approach since Flow isn't suspendable directly
                        }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SealRed)
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = InkLight,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkLight
                    )
                }
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = InkLight.copy(alpha = 0.3f)
            )
        }
    }
}
