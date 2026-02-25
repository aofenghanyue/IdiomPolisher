package com.example.idiompolisher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.idiompolisher.data.local.AppDatabase
import com.example.idiompolisher.data.local.IdiomRecord
import com.example.idiompolisher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 藏经阁 - Collection Screen
 * 展示历史润色记录
 */
@Composable
fun CollectionScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val historyList by db.idiomDao().getAllHistory().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "藏经阁",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }

        if (historyList.isEmpty()) {
            // ── 空态 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.LibraryBooks,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = InkLight.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "尚无收藏",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkLight.copy(alpha = 0.5f),
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "在磨练页面润色文字后，记录将自动保存至此",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkLight.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            // ── 记录数统计 ──
            Text(
                text = "共 ${historyList.size} 条记录",
                style = MaterialTheme.typography.labelMedium,
                color = InkLight,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // ── 历史列表 ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { record ->
                    HistoryCard(
                        record = record,
                        dateFormat = dateFormat,
                        onDelete = {
                            scope.launch(Dispatchers.IO) {
                                db.idiomDao().delete(record)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    record: IdiomRecord,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 原文
            Text(
                text = record.originalText,
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 箭头 + 成语
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkLight
                )
                Text(
                    text = record.idiom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NotoSerifSC,
                    color = Cinnabar,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 释义
            Text(
                text = record.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = InkLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 底部行：分数 + 时间 + 删除
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "文雅 ${record.toneScore}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = Cinnabar
                    )
                    Text(
                        text = dateFormat.format(Date(record.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkLight.copy(alpha = 0.5f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = InkLight.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
