package com.example.task2mc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.task2mc.ui.screens.RouteScreen
import com.example.task2mc.ui.theme.Task2MCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Task2MCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RouteScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
