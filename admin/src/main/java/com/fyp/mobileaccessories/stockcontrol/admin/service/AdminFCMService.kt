package com.fyp.mobileaccessories.stockcontrol.admin.service

import com.fyp.mobileaccessories.stockcontrol.admin.MainActivity
import com.fyp.mobileaccessories.stockcontrol.admin.R
import com.fyp.mobileaccessories.stockcontrol.util.NotificationTopics
import com.github.muhammadnoman11.easyfirebasenotifier.receiver.EasyNotificationReceiver


class AdminFCMService : EasyNotificationReceiver() {
    override fun getActivityClass() = MainActivity::class.java
    override fun getChannelId() = "admin_notifications_channel"
    override fun getChannelName() = "Admin Notifications"
    override fun getNotificationIcon() = R.drawable.admin_icon

    override fun getSubscriptionTopics(): List<String> {
        return listOf(
            NotificationTopics.ADMIN_NOTIFICATIONS
        )
    }
}

