package com.jksalcedo.librefind.domain.model

data class Feedback(
    val id: String = "",
    val uid: String,
    val username: String,
    val type: FeedbackType,
    val text: String,
    val votesHelpful: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val status: FeedbackStatus = FeedbackStatus.PENDING
)

enum class FeedbackType { PRO, CON }
enum class FeedbackStatus { PENDING, APPROVED, REJECTED }
