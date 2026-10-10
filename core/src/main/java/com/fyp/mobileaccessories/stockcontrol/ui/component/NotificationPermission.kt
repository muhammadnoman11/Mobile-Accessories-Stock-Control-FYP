package com.fyp.mobileaccessories.stockcontrol.ui.component

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestNotificationPermissionEffect(
    onPermissionResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                Log.d("NotificationPermission", "POST_NOTIFICATIONS granted: $isGranted")
                onPermissionResult(isGranted)
            }
        )
        LaunchedEffect(Unit) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            } else {
                onPermissionResult(true)
            }
        }
    } else {
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
    }
}
