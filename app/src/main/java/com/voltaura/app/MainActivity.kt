package com.voltaura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.voltaura.app.ui.VoltAuraApp
import com.voltaura.app.ui.theme.VoltAuraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoltAuraTheme {
                VoltAuraApp()
            }
        }
    }
}
