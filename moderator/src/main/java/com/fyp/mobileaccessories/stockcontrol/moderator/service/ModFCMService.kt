package com.fyp.mobileaccessories.stockcontrol.moderator.service

import com.fyp.mobileaccessories.stockcontrol.moderator.MainActivity
import com.fyp.mobileaccessories.stockcontrol.moderator.R
import com.fyp.mobileaccessories.stockcontrol.util.NotificationTopics
import com.github.muhammadnoman11.easyfirebasenotifier.receiver.EasyNotificationReceiver

class ModFCMService : EasyNotificationReceiver() {
    override fun getActivityClass() = MainActivity::class.java
    override fun getChannelId() = "mod_notifications_channel"
    override fun getChannelName() = "Moderator Notifications"
    override fun getNotificationIcon() = R.drawable.moderator_icon

    override fun getSubscriptionTopics(): List<String> {
        return listOf(
            NotificationTopics.MODERATOR_NOTIFICATIONS
        )
    }
}

