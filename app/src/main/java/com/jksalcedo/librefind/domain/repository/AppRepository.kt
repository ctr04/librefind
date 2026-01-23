package com.jksalcedo.librefind.domain.repository

import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.AppItem
import com.jksalcedo.librefind.domain.model.Submission
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun isProprietary(packageName: String): Boolean
    suspend fun getAlternatives(packageName: String): List<Alternative>
    suspend fun getAlternative(packageName: String): Alternative?
    suspend fun getProprietaryTargets(): List<String>
    
    suspend fun submitAlternative(
        proprietaryPackage: String,
        alternativePackage: String,
        appName: String,
        description: String,
        userId: String
    ): Result<Unit>

    suspend fun castVote(
        packageName: String,
        voteType: String, // 'privacy' or 'usability'
        value: Int // 1-5 star rating
    ): Result<Unit>

    suspend fun getMySubmissions(userId: String): List<Submission>
    
    suspend fun submitFeedback(
        packageName: String,
        type: String, // 'PRO' or 'CON'
        text: String
    ): Result<Unit>

    // Kept from KnowledgeGraphRepo if needed, or can be refactored
    suspend fun checkDuplicateApp(name: String, packageName: String): Boolean

    suspend fun getUserVote(packageName: String, userId: String): Int?
}
