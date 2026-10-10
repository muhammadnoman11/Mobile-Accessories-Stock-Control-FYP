package com.fyp.mobileaccessories.stockcontrol.admin

import android.app.Application
import com.fyp.mobileaccessories.stockcontrol.data.repository.RemoteSyncManager
import com.fyp.mobileaccessories.stockcontrol.util.Constant.PROJECT_ID
import com.fyp.mobileaccessories.stockcontrol.util.Constant.SERVICE_ACCOUNT_JSON
import com.github.muhammadnoman11.easyfirebasenotifier.config.FCMConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AdminApp : Application() {
    @Inject
    lateinit var remoteSyncManager: RemoteSyncManager

    override fun onCreate() {
        super.onCreate()
        remoteSyncManager.startRealtimeSync()

        FCMConfig.initialize(
            projectId = PROJECT_ID,
            serviceAccountJson = SERVICE_ACCOUNT_JSON.trimIndent()
        )
    }
}
