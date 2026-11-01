package com.putzplan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.putzplan.app.ui.PutzplanApp
import com.putzplan.app.ui.theme.PutzplanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var letzterCrash by mutableStateOf<String?>(null)
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            val prefs = getSharedPreferences("putzplan", MODE_PRIVATE)
            prefs.edit().putString("letzter_crash", e.stackTraceToString().take(4000)).apply()
            handler?.uncaughtException(t, e)
        }
        val gespeicherter = getSharedPreferences("putzplan", MODE_PRIVATE)
            .getString("letzter_crash", null)
        if (gespeicherter != null) {
            getSharedPreferences("putzplan", MODE_PRIVATE).edit().remove("letzter_crash").apply()
            letzterCrash = gespeicherter
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PutzplanTheme {
                PutzplanApp()
                letzterCrash?.let { crash ->
                    AlertDialog(
                        onDismissRequest = { letzterCrash = null },
                        title = { Text("Letzter Absturz") },
                        text = { Text(crash, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) },
                        confirmButton = {
                            TextButton(onClick = { letzterCrash = null }) { Text("OK") }
                        }
                    )
                }
            }
        }
    }
}
