package com.fyp.mobileaccessories.stockcontrol.moderator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fyp.mobileaccessories.stockcontrol.moderator.navigation.ModNavigation
import com.fyp.mobileaccessories.stockcontrol.ui.theme.ModeratorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModeratorTheme {
                Box(modifier = Modifier.fillMaxSize().imePadding()) {
                    val navController = rememberNavController()
                    ModNavigation(navController = navController)
                }
            }
        }
    }
}
