package com.jksalcedo.librefind.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName

data class RatingDto(
    @get:PropertyName("stars") @set:PropertyName("stars")
    var stars: Int = 0,
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    var createdAt: Long = 0,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at")
    var updatedAt: Long = 0
)
