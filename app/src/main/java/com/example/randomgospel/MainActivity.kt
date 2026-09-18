package com.example.randomgospel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.randomgospel.ui.RandomGospelScreen
import com.example.randomgospel.ui.SettingsScreen
import com.example.randomgospel.ui.theme.RandomGospelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RandomGospelTheme {
                var screen by rememberSaveable { mutableStateOf("home") }
                when (screen) {
                    "settings" -> SettingsScreen(onBack = { screen = "home" })
                    else       -> RandomGospelScreen(onOpenSettings = { screen = "settings" })
                }
            }
        }
    }
}
