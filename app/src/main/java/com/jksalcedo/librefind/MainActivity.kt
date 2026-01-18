package com.jksalcedo.librefind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.jksalcedo.librefind.ui.navigation.NavGraph
import com.jksalcedo.librefind.ui.theme.LibreFindTheme


/**
 * Main activity - entry point for Fossia app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibreFindTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}