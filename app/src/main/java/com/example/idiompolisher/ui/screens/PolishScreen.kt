package com.example.idiompolisher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.idiompolisher.data.IdiomResponse
import com.example.idiompolisher.data.local.AppDatabase
import com.example.idiompolisher.data.local.IdiomRecord
import com.example.idiompolisher.network.PolishingRequest
import com.example.idiompolisher.network.RetrofitClient
import com.example.idiompolisher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 语感磨练 - Polish Screen
 * 输入文字，AI 润色为成语/诗词
 */
@Composable
fun PolishScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resultData by remember { mutableStateOf<IdiomResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var polishMode by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    // 润色请求
    fun doPolish() {
        if (inputText.isNotBlank() && !isLoading) {
            scope.launch {
                isLoading = true
                errorMessage = null
                resultData = null
                try {
                    val res = RetrofitClient.apiService.polishText(PolishingRequest(inputText))
                    resultData = res
                    launch(Dispatchers.IO) {
                        db.idiomDao().insert(
                            IdiomRecord(
                                originalText = inputText,
                                idiom = res.idiom,
                                explanation = res.explanation,
                                toneScore = res.toneScore
                            )
                        )
                    }
                } catch (e: Exception) {
                    errorMessage = "请求失败: ${e.localizedMessage}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
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
                text = "语感磨练",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }

        // ── 风格 + 润色模式 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Style,
                    contentDescription = "风格",
                    tint = InkLight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "风格：古风雅韵",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "润色模式",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
                Switch(
                    checked = polishMode,
                    onCheckedChange = { polishMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Cinnabar,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = StoneLight
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 输入区域（宣纸纹理） ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = PaperTexture
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // 四角装饰
                val cornerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                val cs = 20.dp
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(cs)
                        .drawBehind {
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(cs)
                        .drawBehind {
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(cs)
                        .drawBehind {
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(cs)
                        .drawBehind {
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                            drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        }
                )

                // TextField
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "请在此处书写，落笔即成章...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkLight.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Cinnabar
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = NotoSerifSC,
                        lineHeight = 28.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── "磨练" 印章按钮 —— 居中在输入框下方 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { doPolish() },
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SealRed,
                    contentColor = Color.White,
                    disabledContainerColor = SealRed.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                enabled = inputText.isNotBlank() && !isLoading,
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "推敲中…",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSerifSC,
                        letterSpacing = 3.sp
                    )
                } else {
                    Text(
                        text = "磨 练",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSerifSC,
                        letterSpacing = 8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 错误信息 ──
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 润色预览/结果 ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // 右上角引号装饰
                Icon(
                    Icons.Outlined.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = Cinnabar,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "润色预览",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (resultData != null) {
                        // 成语结果
                        Text(
                            text = resultData!!.idiom,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NotoSerifSC,
                            color = Cinnabar,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 文雅程度
                        Text(
                            text = "文雅程度: ${resultData!!.toneScore}/10",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 释义
                        Text(
                            text = resultData!!.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )

                        // 备选成语
                        if (resultData!!.alternatives.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "其他备选",
                                style = MaterialTheme.typography.labelMedium,
                                color = InkLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                resultData!!.alternatives.forEach { alt ->
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                alt,
                                                fontFamily = NotoSerifSC,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Bamboo
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                resultData = null
                                inputText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cinnabar
                            )
                        ) {
                            Text("再来一次", letterSpacing = 2.sp)
                        }
                    } else {
                        // 默认占位
                        Text(
                            text = "\"在此处，您的文字将如墨汁晕染纸上，化作优美的诗篇...\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
