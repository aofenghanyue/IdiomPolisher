package com.example.idiompolisher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.idiompolisher.ui.navigation.AppNavHost
import com.example.idiompolisher.ui.theme.IdiomPolisherTheme

/**
 * 主入口 Activity —— 从应用图标启动
 * 包含底部导航和四个 Tab 页面
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IdiomPolisherTheme {
                AppNavHost()
            }
        }
    }
}
