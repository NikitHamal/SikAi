package com.sikai.learn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.presentation.SikAiRoot
import com.sikai.learn.presentation.boot.BootViewModel
import com.sikai.learn.ui.theme.NeoVedicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }

        // Safety timeout: dismiss splash after 3s even if boot isn't ready
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 3000)

        setContent {
            val bootVm: BootViewModel = hiltViewModel()
            val state by bootVm.state.collectAsState()
            keepSplash = !state.ready

            NeoVedicTheme(themeMode = state.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SikAiRoot(bootState = state)
                }
            }
        }
    }
}
