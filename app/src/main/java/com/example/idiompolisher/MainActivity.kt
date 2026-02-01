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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.idiompolisher.ui.theme.IdiomPolisherTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.idiompolisher.data.IdiomResponse
import com.example.idiompolisher.network.PolishingRequest
import com.example.idiompolisher.network.RetrofitClient
import com.example.idiompolisher.data.local.AppDatabase
import com.example.idiompolisher.data.local.IdiomRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 获取传入的文本
        val processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        val isStandalone = processText == null

        setContent {
            IdiomPolisherTheme {
                // Surface 做背景，保证弹窗圆角好看
                Surface(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    MainScreen(
                        initialText = processText ?: "",
                        // 将关闭 Activity 的能力传给 UI
                        onFinish = { replacement ->
                            if (isStandalone) {
                                // 独立运行模式：只复制，不关闭
                                copyToClipboard(replacement)
                            } else {
                                // 文本选择模式：复制并关闭，返回结果给调用者
                                finishWithResult(replacement)
                            }
                        }
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

    // 核心逻辑：关闭页面并返回结果
    private fun finishWithResult(replacement: String?) {
        if (replacement != null) {
            // 1. 尝试直接替换 (标准 Android 协议)
            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, replacement)
            setResult(Activity.RESULT_OK, resultIntent)

            // 2. 双保险：自动复制到剪贴板
            copyToClipboard(replacement)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish() // 关闭当前弹窗
    }
}

@Composable
fun MainScreen(initialText: String, onFinish: (String?) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val historyList by db.idiomDao().getAllHistory().collectAsState(initial = emptyList())

    // 关键优化：如果 initialText 不为空，初始状态直接设为 loading = true
    // 这样就不会先闪现输入框，再变成 loading 了
    var isLoading by remember { mutableStateOf(initialText.isNotBlank()) }
    var resultData by remember { mutableStateOf<IdiomResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf(initialText) }
    val scope = rememberCoroutineScope()

    // Helper to save history
    fun saveToHistory(text: String, data: IdiomResponse) {
        scope.launch(Dispatchers.IO) {
            // 先删除旧的相同记录（如果有）
            val existingList = historyList.filter { it.originalText == text }
            existingList.forEach { db.idiomDao().delete(it) }

            // 插入新记录（时间戳会更新，从而排到最前）
            db.idiomDao().insert(
                IdiomRecord(
                    originalText = text,
                    idiom = data.idiom,
                    explanation = data.explanation,
                    toneScore = data.toneScore
                )
            )
        }
    }

    // 如果传入了初始文本，自动触发加载
    LaunchedEffect(initialText) {
        if (initialText.isNotBlank()) {
            // isLoading 已经在初始化时设为 true 了，这里不用重复设
            errorMessage = null
            try {
                val res = RetrofitClient.apiService.polishText(PolishingRequest(initialText))
                resultData = res
                saveToHistory(initialText, res)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "网络请求失败: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        } else {
            // 如果是空文本进来（比如直接点图标打开），确保 loading 是 false
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 如果没有结果且没有正在加载，显示输入框（支持手动输入）
        if (resultData == null && !isLoading) {
             OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("输入或粘贴要润色的话") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val res = RetrofitClient.apiService.polishText(PolishingRequest(inputText))
                                resultData = res
                                saveToHistory(inputText, res)
                            } catch (e: Exception) {
                                errorMessage = "请求失败: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始润色")
            }

            // 历史记录列表
            if (historyList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    items(historyList) { record ->
                        HistoryItem(record, onClick = {
                            resultData = IdiomResponse(
                                original = record.originalText,
                                idiom = record.idiom,
                                alternatives = emptyList(),
                                explanation = record.explanation,
                                toneScore = record.toneScore
                            )
                            inputText = record.originalText
                        })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }

        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(24.dp)) {
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
            Button(onClick = { errorMessage = null }) { // 重试/返回
                Text("返回")
            }
        } 
        
        // 显示结果
        resultData?.let { data ->
            Text(
                text = "文雅程度: ${data.toneScore}/10",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 核心成语卡片
            SuggestionItem(text = data.idiom, isHighlight = true, onClick = { onFinish(data.idiom) })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 释义区域
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
            
            // 备选列表
            if (data.alternatives.isNotEmpty()) {
                Text(
                    text = "其他备选",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                data.alternatives.forEach { alt ->
                    SuggestionItem(text = alt, onClick = { onFinish(alt) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 重新开始按钮
            OutlinedButton(onClick = { 
                resultData = null 
                inputText = ""
            }) {
                Text("再来一次")
            }
        }
    }
}

@Composable
fun SuggestionItem(text: String, isHighlight: Boolean = false, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = if (isHighlight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun HistoryItem(record: IdiomRecord, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.originalText,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = "→ ${record.idiom}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "${record.toneScore}分",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
