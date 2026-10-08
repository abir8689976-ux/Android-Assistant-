package com.example.data.model

enum class CommandType {
    NORMAL_AI_QUERY,
    OPEN_APP_COMMAND,
    UNSUPPORTED_DEVICE_COMMAND
}

sealed class CommandClassification(val type: CommandType) {
    data class NormalAiQuery(
        val query: String
    ) : CommandClassification(CommandType.NORMAL_AI_QUERY)

    data class OpenAppCommand(
        val rawQuery: String,
        val appKey: String,
        val targetName: String
    ) : CommandClassification(CommandType.OPEN_APP_COMMAND)

    data class UnsupportedDeviceCommand(
        val rawQuery: String,
        val requestedAction: String,
        val userMessage: String
    ) : CommandClassification(CommandType.UNSUPPORTED_DEVICE_COMMAND)
}
