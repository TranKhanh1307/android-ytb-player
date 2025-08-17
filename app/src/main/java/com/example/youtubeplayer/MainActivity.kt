package com.example.youtubeplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.player.PlayerScreen
import com.example.ui.theme.YoutubePlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YoutubePlayerTheme {
                Scaffold { paddingValues ->
                    PlayerScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

