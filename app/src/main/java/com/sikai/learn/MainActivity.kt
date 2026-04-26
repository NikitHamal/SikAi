package com.sikai.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sikai.learn.core.navigation.SikAiNavGraph
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.ui.theme.SikAiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = "system")
            SikAiTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    SikAiNavGraph()
                }
            }
        }
    }
}
