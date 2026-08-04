// MainActivity.kt
package com.pip.cheeseroul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.pip.cheeseroul.ui.navigation.NavGraph
import com.pip.cheeseroul.ui.theme.CheeseRoulTheme
import com.pip.cheeseroul.viewmodel.RouletteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheeseRoulTheme {
                val navController = rememberNavController()
                val viewModel: RouletteViewModel = viewModel()
                NavGraph(navController = navController, viewModel = viewModel)
            }
        }
    }
}