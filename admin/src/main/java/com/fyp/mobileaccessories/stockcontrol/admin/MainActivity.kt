package com.fyp.mobileaccessories.stockcontrol.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fyp.mobileaccessories.stockcontrol.admin.navigation.AdminNavigation
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AdminTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminTheme {
                Box(modifier = Modifier.fillMaxSize().imePadding()) {
                    val navController = rememberNavController()
                    AdminNavigation(navController = navController)
                }
            }
        }
    }
}
