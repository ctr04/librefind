package com.jksalcedo.librefind.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName
import com.jksalcedo.librefind.domain.model.UserProfile

data class UserProfileDto(
    @get:PropertyName("uid") @set:PropertyName("uid")
    var uid: String = "",
    @get:PropertyName("username") @set:PropertyName("username")
    var username: String = "",
    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",
    @get:PropertyName("joined_at") @set:PropertyName("joined_at")
    var joinedAt: Long = 0,
    @get:PropertyName("submission_count") @set:PropertyName("submission_count")
    var submissionCount: Int = 0,
    @get:PropertyName("approved_count") @set:PropertyName("approved_count")
    var approvedCount: Int = 0
) {
    fun toDomain() = UserProfile(
        uid = uid,
        username = username,
        email = email,
        joinedAt = joinedAt,
        submissionCount = submissionCount,
        approvedCount = approvedCount
    )

    companion object {
        fun fromDomain(profile: UserProfile) = UserProfileDto(
            uid = profile.uid,
            username = profile.username,
            email = profile.email,
            joinedAt = profile.joinedAt,
            submissionCount = profile.submissionCount,
            approvedCount = profile.approvedCount
        )
    }
}
