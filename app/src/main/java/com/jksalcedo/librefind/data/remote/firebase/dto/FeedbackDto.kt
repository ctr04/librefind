package com.jksalcedo.librefind.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName
import com.jksalcedo.librefind.domain.model.Feedback
import com.jksalcedo.librefind.domain.model.FeedbackStatus
import com.jksalcedo.librefind.domain.model.FeedbackType

data class FeedbackDto(
    @get:PropertyName("uid") @set:PropertyName("uid")
    var uid: String = "",
    @get:PropertyName("username") @set:PropertyName("username")
    var username: String = "",
    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "",
    @get:PropertyName("text") @set:PropertyName("text")
    var text: String = "",
    @get:PropertyName("votes_helpful") @set:PropertyName("votes_helpful")
    var votesHelpful: Int = 0,
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    var createdAt: Long = 0,
    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "PENDING"
) {
    fun toDomain(id: String) = Feedback(
        id = id,
        uid = uid,
        username = username,
        type = FeedbackType.valueOf(type),
        text = text,
        votesHelpful = votesHelpful,
        createdAt = createdAt,
        status = FeedbackStatus.valueOf(status)
    )

    companion object {
        fun fromDomain(feedback: Feedback) = FeedbackDto(
            uid = feedback.uid,
            username = feedback.username,
            type = feedback.type.name,
            text = feedback.text,
            votesHelpful = feedback.votesHelpful,
            createdAt = feedback.createdAt,
            status = feedback.status.name
        )
    }
}
