package com.jksalcedo.librefind.domain.model

enum class VoteType(val key: String, val displayName: String, val description: String) {
    USABILITY("usability", "Usability", "How easy is it to use?"),
    PRIVACY("privacy", "Privacy", "How strictly does it respect your data?"),
    FEATURES("features", "Feature Parity", "How close is it to the proprietary alternative?");

    companion object {
        fun fromKey(key: String): VoteType? = entries.find { it.key == key }
    }
}
