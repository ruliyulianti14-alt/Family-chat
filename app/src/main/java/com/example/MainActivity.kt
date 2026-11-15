package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.MyFamilyChatApp
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.MyFamilyChatTheme
import com.example.util.ThemePreferenceManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val currentTheme by ThemePreferenceManager
                .observeTheme(applicationContext)
                .collectAsStateWithLifecycle(initialValue = AppThemeOption.SKY_WARM)

            LaunchedEffect(currentTheme.isDark) {
                if (currentTheme.isDark) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    )
                }
            }

            MyFamilyChatTheme(themeOption = currentTheme) {
                MyFamilyChatApp(
                    currentTheme = currentTheme,
                    onSelectTheme = { chosen ->
                        scope.launch {
                            ThemePreferenceManager.setTheme(applicationContext, chosen)
                        }
                    }
                )
            }
        }
    }
}
