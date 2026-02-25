package com.example.idiompolisher

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.idiompolisher.ui.theme.IdiomPolisherTheme
import kotlinx.coroutines.launch
import com.example.idiompolisher.data.IdiomResponse
import com.example.idiompolisher.network.PolishingRequest
import com.example.idiompolisher.network.RetrofitClient
import com.example.idiompolisher.data.local.AppDatabase
import com.example.idiompolisher.data.local.IdiomRecord
import kotlinx.coroutines.Dispatchers

/**
 * 文字处理弹窗 Activity
 * 仅从系统文本选择菜单的 "润色" 入口触发
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()

        // 如果没有传入文本（不应该发生，因为只从 PROCESS_TEXT 触发），直接关闭
        if (processText.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            IdiomPolisherTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ProcessTextScreen(
                        initialText = processText,
                        onFinish = { replacement -> finishWithResult(replacement) }
                    )
                }
            }
        }
    }

    private fun copyToClipboard(text: String?) {
        if (text != null) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Idiom", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "已复制: $text", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishWithResult(replacement: String?) {
        if (replacement != null) {
            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, replacement)
            setResult(Activity.RESULT_OK, resultIntent)
            copyToClipboard(replacement)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
}

@Composable
fun ProcessTextScreen(initialText: String, onFinish: (String?) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var isLoading by remember { mutableStateOf(true) }
    var resultData by remember { mutableStateOf<IdiomResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 自动触发加载
    LaunchedEffect(initialText) {
        try {
            val res = RetrofitClient.apiService.polishText(PolishingRequest(initialText))
            resultData = res
            // Save to history
            scope.launch(Dispatchers.IO) {
                db.idiomDao().insert(
                    IdiomRecord(
                        originalText = initialText,
                        idiom = res.idiom,
                        explanation = res.explanation,
                        toneScore = res.toneScore
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "网络请求失败: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(24.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("AI 正在推敲...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(onClick = { errorMessage = null; (context as? Activity)?.finish() }) {
                Text("关闭")
            }
        }

        resultData?.let { data ->
            Text(
                text = "文雅程度: ${data.toneScore}/10",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 核心成语卡片
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFinish(data.idiom) }
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.idiom,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 释义
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "释义与典故",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 备选
            if (data.alternatives.isNotEmpty()) {
                Text(
                    text = "其他备选",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                data.alternatives.forEach { alt ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFinish(alt) }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alt,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
