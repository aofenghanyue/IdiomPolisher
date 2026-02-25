package com.example.idiompolisher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.idiompolisher.ui.theme.Bamboo
import com.example.idiompolisher.ui.theme.Cinnabar
import com.example.idiompolisher.ui.theme.InkLight
import com.example.idiompolisher.ui.theme.NotoSerifSC

/**
 * 书房首页 - Home Screen
 * 展示每日一词、发现成语、精选专题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // ── 顶部标题栏 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "书房",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Text(
                    text = "语感磨练",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkLight,
                    letterSpacing = 4.sp
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ── 每日一词卡片 ──
        DailyIdiomCard(
            idiom = "磨杵成针",
            pinyin = "mó chǔ chéng zhēn",
            verse = "只要功夫深，\n铁杵磨成针。",
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 发现 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "⊙ 发现",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 横向滚动竹简卡片 ──
        val idioms = listOf("温故知新", "上善若水", "厚德载物", "知行合一", "道法自然")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            idioms.forEach { idiom ->
                BambooScrollCard(idiom = idiom)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── 精选专题 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "精选专题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            TextButton(onClick = { }) {
                Text(
                    text = "查看全部",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 专题网格
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopicCard(
                title = "山水自然",
                emoji = "🏔️",
                modifier = Modifier.weight(1f)
            )
            TopicCard(
                title = "历史典故",
                emoji = "📜",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopicCard(
                title = "处世哲学",
                emoji = "🧘",
                modifier = Modifier.weight(1f)
            )
            TopicCard(
                title = "诗词歌赋",
                emoji = "✍️",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * 每日一词大卡片
 */
@Composable
private fun DailyIdiomCard(
    idiom: String,
    pinyin: String,
    verse: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // 四角装饰
            CornerDecorations()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：水墨画区域 + 诗句
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 模拟水墨画区域
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE8E0D4),
                                        Color(0xFFD4C8B4),
                                        Color(0xFFC8BCA5)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 水墨文字装饰
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🏔️",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "山水",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkLight
                            )
                        }
                    }

                    // "悟" 印章 + 诗句
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(1.dp, Cinnabar, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "悟",
                                fontSize = 9.sp,
                                color = Cinnabar,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = verse,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 右侧：每日一词标签 + 成语 + 拼音
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // "每日一词" 标签
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.5.dp,
                                color = Cinnabar.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                Cinnabar.copy(alpha = 0.05f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            "每日一词".forEach { char ->
                                Text(
                                    text = char.toString(),
                                    fontSize = 10.sp,
                                    color = Cinnabar,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NotoSerifSC,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 成语竖排
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // 拼音
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            pinyin.split(" ").forEach { py ->
                                Text(
                                    text = py,
                                    fontSize = 9.sp,
                                    color = InkLight,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // 分隔线
                        Box(
                            modifier = Modifier
                                .width(0.5.dp)
                                .height(140.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        // 成语大字
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            idiom.forEach { char ->
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NotoSerifSC,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 四角装饰线
 */
@Composable
private fun BoxScope.CornerDecorations() {
    val cornerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
    val cornerSize = 24.dp
    val cornerStroke = 1.5.dp

    // 左上
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .size(cornerSize)
            .drawBehind {
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = cornerStroke.toPx())
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = cornerStroke.toPx())
            }
    )
    // 右上
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(cornerSize)
            .drawBehind {
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = cornerStroke.toPx())
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = cornerStroke.toPx())
            }
    )
    // 左下
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
            .size(cornerSize)
            .drawBehind {
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = cornerStroke.toPx())
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = cornerStroke.toPx())
            }
    )
    // 右下
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .size(cornerSize)
            .drawBehind {
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = cornerStroke.toPx())
                drawLine(cornerColor, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = cornerStroke.toPx())
            }
    )
}

/**
 * 竹简风格成语卡片
 */
@Composable
private fun BambooScrollCard(idiom: String) {
    Card(
        modifier = Modifier
            .width(72.dp)
            .height(220.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Bamboo
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部圆点装饰
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            )

            // 竖排成语
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                idiom.forEach { char ->
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSerifSC,
                        letterSpacing = 2.sp
                    )
                }
            }

            // 底部圆点装饰
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * 精选专题卡片
 */
@Composable
private fun TopicCard(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.6f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3D3229)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 背景装饰
            Text(
                text = emoji,
                fontSize = 36.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
