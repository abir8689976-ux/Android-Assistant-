package com.example.data.service

import com.example.data.model.CommandClassification
import java.util.Locale

interface CommandRouter {
    fun classify(rawInput: String): CommandClassification
}

class CommandRouterImpl : CommandRouter {

    override fun classify(rawInput: String): CommandClassification {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return CommandClassification.NormalAiQuery("")
        }

        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Unsupported Device / Hardware Control commands
        val unsupportedHardwareMatch = checkUnsupportedDeviceCommands(lower, trimmed)
        if (unsupportedHardwareMatch != null) {
            return unsupportedHardwareMatch
        }

        // 2. Check for App Launching commands (English and Bengali)
        val appLaunchMatch = checkAppLaunchCommands(lower, trimmed)
        if (appLaunchMatch != null) {
            return appLaunchMatch
        }

        // 3. Fallback to normal AI query
        return CommandClassification.NormalAiQuery(trimmed)
    }

    private fun checkUnsupportedDeviceCommands(
        lower: String,
        original: String
    ): CommandClassification.UnsupportedDeviceCommand? {
        val isBengali = hasBengaliScript(original)

        // Bluetooth
        if (lower.contains("bluetooth") || lower.contains("ব্লুটুথ")) {
            val isToggle = lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("enable") || lower.contains("disable") ||
                lower.contains("switch on") || lower.contains("switch off") ||
                lower.contains("চালু") || lower.contains("বন্ধ") || lower.contains("অন") || lower.contains("অফ")

            if (isToggle) {
                val msg = if (isBengali) {
                    "সিস্টেম নোটিশ: সরাসরি ব্লুটুথ নিয়ন্ত্রণ বর্তমানে সক্রিয় নয় (ধাপ ১ সুরক্ষা নীতি)।"
                } else {
                    "System notice: Direct Bluetooth hardware control is not supported in Step 1 architecture."
                }
                return CommandClassification.UnsupportedDeviceCommand(
                    rawQuery = original,
                    requestedAction = "Toggle Bluetooth",
                    userMessage = msg
                )
            }
        }

        // Wi-Fi
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ওয়াইফাই") || lower.contains("ওয়াইফাই")) {
            val isToggle = lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("enable") || lower.contains("disable") ||
                lower.contains("চালু") || lower.contains("বন্ধ") || lower.contains("অন") || lower.contains("অফ")

            if (isToggle) {
                val msg = if (isBengali) {
                    "সিস্টেম নোটিশ: সরাসরি ওয়াইফাই হার্ডওয়্যার নিয়ন্ত্রণ বর্তমানে সক্রিয় নয়।"
                } else {
                    "System notice: Direct Wi-Fi hardware control is not supported in Step 1 architecture."
                }
                return CommandClassification.UnsupportedDeviceCommand(
                    rawQuery = original,
                    requestedAction = "Toggle Wi-Fi",
                    userMessage = msg
                )
            }
        }

        // Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("ফ্ল্যাশলাইট") || lower.contains("টর্চ")) {
            if (lower.contains("turn on") || lower.contains("turn off") || lower.contains("on") || lower.contains("off") ||
                lower.contains("জ্বালাও") || lower.contains("বন্ধ") || lower.contains("চালু")) {
                val msg = if (isBengali) {
                    "সিস্টেম নোটিশ: ফ্ল্যাশলাইট নিয়ন্ত্রণ বর্তমানে সমর্থিত নয়।"
                } else {
                    "System notice: Flashlight hardware control is not supported in Step 1."
                }
                return CommandClassification.UnsupportedDeviceCommand(
                    rawQuery = original,
                    requestedAction = "Toggle Flashlight",
                    userMessage = msg
                )
            }
        }

        // Airplane Mode
        if (lower.contains("airplane mode") || lower.contains("flight mode") || lower.contains("এয়ারপ্লেন মোড")) {
            val msg = if (isBengali) {
                "সিস্টেম নোটিশ: এয়ারপ্লেন মোড পরিবর্তন অ্যান্ড্রয়েড সিস্টেমের অনুমতি সাপেক্ষ।"
            } else {
                "System notice: Modifying Airplane Mode requires elevated system permissions."
            }
            return CommandClassification.UnsupportedDeviceCommand(
                rawQuery = original,
                requestedAction = "Airplane Mode",
                userMessage = msg
            )
        }

        // Power / Reboot
        if (lower.contains("reboot") || lower.contains("restart phone") || lower.contains("shutdown phone") ||
            lower.contains("রিস্টার্ট") || lower.contains("শাটডাউন")) {
            val msg = if (isBengali) {
                "সিস্টেম নোটিশ: ডিভাইস রিস্টার্ট বা পাওয়ার অফ করার সুবিধা সমর্থিত নয়।"
            } else {
                "System notice: Device reboot/shutdown cannot be executed from application sandbox."
            }
            return CommandClassification.UnsupportedDeviceCommand(
                rawQuery = original,
                requestedAction = "Device Power State",
                userMessage = msg
            )
        }

        return null
    }

    private fun checkAppLaunchCommands(
        lower: String,
        original: String
    ): CommandClassification.OpenAppCommand? {
        // App mappings: key -> Pair(canonical name, list of aliases in EN & BN)
        val appDefinitions = listOf(
            Triple("youtube", "YouTube", listOf("youtube", "ইউটিউব")),
            Triple("chrome", "Chrome", listOf("chrome", "ক্রোম", "browser", "ব্রাউজার", "google chrome")),
            Triple("camera", "Camera", listOf("camera", "ক্যামেরা")),
            Triple("settings", "Settings", listOf("settings", "সেটিংস", "system settings")),
            Triple("calculator", "Calculator", listOf("calculator", "ক্যালকুলেটর")),
            Triple("maps", "Google Maps", listOf("maps", "google maps", "ম্যাপস", "ম্যাপ", "গুগল ম্যাপ")),
            Triple("clock", "Clock", listOf("clock", "alarm", "ঘড়ি", "ঘড়ি", "অ্যালার্ম")),
            Triple("phone", "Phone", listOf("phone", "dialer", "কল", "ডায়াল", "ডায়াল", "ফোন")),
            Triple("messages", "Messages", listOf("messages", "message", "মেসেজ", "বার্তা", "sms")),
            Triple("gallery", "Gallery", listOf("gallery", "photos", "গ্যালারি", "ছবি")),
            Triple("gmail", "Gmail", listOf("gmail", "email", "জিমেইল", "ইমেইল")),
            Triple("playstore", "Play Store", listOf("play store", "playstore", "google play", "প্লে স্টোর"))
        )

        // English prefix patterns: "open [app]", "launch [app]", "start [app]", "run [app]", "go to [app]"
        val englishPrefixes = listOf("open ", "launch ", "start ", "run ", "go to ")
        for (prefix in englishPrefixes) {
            if (lower.startsWith(prefix)) {
                val candidateTarget = lower.removePrefix(prefix).trim()
                for ((appKey, canonicalName, aliases) in appDefinitions) {
                    if (aliases.any { alias -> candidateTarget == alias || candidateTarget.startsWith(alias) }) {
                        return CommandClassification.OpenAppCommand(
                            rawQuery = original,
                            appKey = appKey,
                            targetName = canonicalName
                        )
                    }
                }
            }
        }

        // Bengali suffix and prefix patterns:
        // "[app] খোলো", "[app] খুলে দাও", "[app] ওপেন করো", "[app] চালু করো", "[app] খোল"
        val bengaliKeywords = listOf("খোলো", "খুলে দাও", "খুলে দিন", "ওপেন করো", "ওপেন করুন", "চালু করো", "চালু করুন", "খোল")
        for ((appKey, canonicalName, aliases) in appDefinitions) {
            for (alias in aliases) {
                if (lower.contains(alias)) {
                    // Check if query contains any of the bengali trigger keywords or "open"
                    val hasBengaliTrigger = bengaliKeywords.any { lower.contains(it) }
                    val hasEnglishTrigger = lower.contains("open") || lower.contains("launch")

                    if (hasBengaliTrigger || hasEnglishTrigger) {
                        return CommandClassification.OpenAppCommand(
                            rawQuery = original,
                            appKey = appKey,
                            targetName = canonicalName
                        )
                    }
                }
            }
        }

        return null
    }

    private fun hasBengaliScript(text: String): Boolean {
        return text.any { it in '\u0980'..'\u09FF' }
    }
}
