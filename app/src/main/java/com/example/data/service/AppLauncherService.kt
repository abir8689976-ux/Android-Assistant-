package com.example.data.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

sealed class AppLaunchResult {
    data class Success(val appName: String, val message: String) : AppLaunchResult()
    data class Failed(val appName: String, val reason: String) : AppLaunchResult()
}

interface AppLauncherService {
    fun openApp(appKey: String, displayName: String, isBengali: Boolean): AppLaunchResult
}

class AppLauncherServiceImpl(private val context: Context) : AppLauncherService {

    override fun openApp(appKey: String, displayName: String, isBengali: Boolean): AppLaunchResult {
        // 1. Try launching by specific package if installed
        val packageName = getPackageNameForApp(appKey)
        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(launchIntent)
                    val successMsg = if (isBengali) {
                        "$displayName সফলভাবে চালু করা হয়েছে।"
                    } else {
                        "Successfully launched $displayName."
                    }
                    AppLaunchResult.Success(displayName, successMsg)
                } catch (_: Exception) {
                    tryActionLaunch(appKey, displayName, isBengali)
                }
            }
        }

        // 2. Fallback to generic system action intent
        return tryActionLaunch(appKey, displayName, isBengali)
    }

    private fun tryActionLaunch(appKey: String, displayName: String, isBengali: Boolean): AppLaunchResult {
        val intent = createLaunchIntent(appKey)
        if (intent == null) {
            val notFoundMsg = if (isBengali) {
                "$displayName এই ডিভাইসে পাওয়া যায়নি বা ইনস্টল করা নেই।"
            } else {
                "$displayName is not installed or unavailable on this device."
            }
            return AppLaunchResult.Failed(displayName, notFoundMsg)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            context.startActivity(intent)
            val successMsg = if (isBengali) {
                "$displayName সফলভাবে চালু করা হয়েছে।"
            } else {
                "Successfully launched $displayName."
            }
            AppLaunchResult.Success(displayName, successMsg)
        } catch (_: ActivityNotFoundException) {
            val failMsg = if (isBengali) {
                "$displayName এই ডিভাইসে ইনস্টল করা নেই বা চালু করার উপযোগী অ্যাপ্লিকেশন পাওয়া যায়নি।"
            } else {
                "$displayName is not installed or no compatible application was found."
            }
            AppLaunchResult.Failed(displayName, failMsg)
        } catch (e: Exception) {
            val failMsg = if (isBengali) {
                "$displayName চালু করা সম্ভব হয়নি: ${e.localizedMessage ?: "ত্রুটি"}"
            } else {
                "Failed to launch $displayName: ${e.localizedMessage ?: "Unknown error"}"
            }
            AppLaunchResult.Failed(displayName, failMsg)
        }
    }

    private fun createLaunchIntent(appKey: String): Intent? {
        return when (appKey.lowercase()) {
            "youtube" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            "chrome" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            "camera" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            "settings" -> Intent(Settings.ACTION_SETTINGS)
            "calculator" -> null
            "maps" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
            "clock" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            "phone" -> Intent(Intent.ACTION_DIAL)
            "messages" -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
            }
            "gallery" -> Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
            }
            "gmail" -> Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
            "playstore" -> Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"))
            else -> null
        }
    }

    private fun getPackageNameForApp(appKey: String): String? {
        return when (appKey.lowercase()) {
            "youtube" -> "com.google.android.youtube"
            "chrome" -> "com.android.chrome"
            "camera" -> "com.android.camera"
            "settings" -> "com.android.settings"
            "calculator" -> "com.google.android.calculator"
            "maps" -> "com.google.android.apps.maps"
            "clock" -> "com.google.android.deskclock"
            "phone" -> "com.google.android.dialer"
            "messages" -> "com.google.android.apps.messaging"
            "gallery" -> "com.google.android.apps.photos"
            "gmail" -> "com.google.android.gm"
            "playstore" -> "com.android.vending"
            else -> null
        }
    }
}
