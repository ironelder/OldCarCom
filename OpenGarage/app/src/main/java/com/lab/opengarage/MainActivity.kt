package com.lab.opengarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lab.opengarage.ui.AppRoot
import com.lab.opengarage.ui.RootViewModel
import com.lab.opengarage.ui.theme.OpenGarageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val rootViewModel: RootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        // 세션 복원 + 첫 피드 프리페치가 끝날 때까지 시스템 스플래시 유지
        splash.setKeepOnScreenCondition { !rootViewModel.ready.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenGarageTheme {
                AppRoot(rootViewModel)
            }
        }
    }
}
