package com.basecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.basecalc.data.AppContainer
import com.basecalc.ui.CalculatorScreen
import com.basecalc.ui.theme.BaseCalcTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        AppContainer.init(this)
        
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsState()
            BaseCalcTheme(colorMode = state.colorMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
