package com.jksalcedo.librefind.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName
import com.jksalcedo.librefind.domain.model.Alternative

data class FossSolutionDto(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",
    
    @get:PropertyName("license") @set:PropertyName("license")
    var license: String = "",
    
    @get:PropertyName("repo_url") @set:PropertyName("repo_url")
    var repoUrl: String = "",
    
    @get:PropertyName("fdroid_id") @set:PropertyName("fdroid_id")
    var fdroidId: String = "",
    
    @get:PropertyName("icon_url") @set:PropertyName("icon_url")
    var iconUrl: String? = null,
    
    @get:PropertyName("package_name") @set:PropertyName("package_name")
    var packageName: String = "",
    
    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",
    
    @get:PropertyName("website") @set:PropertyName("website")
    var website: String = "",
    
    @get:PropertyName("features") @set:PropertyName("features")
    var features: List<String> = emptyList(),
    
    @get:PropertyName("pros") @set:PropertyName("pros")
    var pros: List<String> = emptyList(),
    
    @get:PropertyName("cons") @set:PropertyName("cons")
    var cons: List<String> = emptyList(),
    
    @get:PropertyName("rating_avg") @set:PropertyName("rating_avg")
    var ratingAvg: Float = 0f,
    
    @get:PropertyName("rating_count") @set:PropertyName("rating_count")
    var ratingCount: Int = 0
) {
    fun toDomain(id: String, userRating: Int? = null): Alternative {
        return Alternative(
            id = id,
            name = name,
            packageName = packageName,
            license = license,
            repoUrl = repoUrl,
            fdroidId = fdroidId,
            iconUrl = iconUrl,
            ratingAvg = ratingAvg,
            ratingCount = ratingCount,
            userRating = userRating,
            description = description,
            website = website,
            features = features,
            pros = pros,
            cons = cons
        )
    }
}

