package com.example.voidstep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voidstep.game.viewmodel.GameViewModel
import com.example.voidstep.game.viewmodel.GameViewModelFactory
import com.example.voidstep.ui.navigation.AppNavGraph
import com.example.voidstep.ui.theme.VoidStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel(
                factory = GameViewModelFactory(application)
            )
            val preferences by gameViewModel.preferences.collectAsState()

            VoidStepTheme(themeMode = preferences.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(gameViewModel = gameViewModel)
                }
            }
        }
    }
}
