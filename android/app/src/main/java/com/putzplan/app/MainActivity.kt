package com.putzplan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.putzplan.app.ui.PutzplanApp
import com.putzplan.app.ui.theme.PutzplanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PutzplanTheme {
                PutzplanApp()
            }
        }
    }
}
