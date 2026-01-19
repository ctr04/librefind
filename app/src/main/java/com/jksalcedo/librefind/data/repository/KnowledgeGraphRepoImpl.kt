package com.jksalcedo.librefind.data.repository

import com.jksalcedo.librefind.data.remote.firebase.DuplicateResult
import com.jksalcedo.librefind.data.remote.firebase.FirestoreService
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.Submission
import com.jksalcedo.librefind.domain.repository.KnowledgeGraphRepo

class KnowledgeGraphRepoImpl(
    private val firestoreService: FirestoreService
) : KnowledgeGraphRepo {

    override suspend fun isProprietary(packageName: String): Boolean {
        return firestoreService.isProprietaryPackage(packageName)
    }

    override suspend fun getAlternatives(packageName: String): List<Alternative> {
        return firestoreService.getAlternatives(packageName)
    }

    override suspend fun submitAlternative(
        proprietaryPackage: String,
        alternativeId: String,
        userId: String
    ): Boolean {
        return firestoreService.submitProposal(
            proprietaryPackage = proprietaryPackage,
            alternativeId = alternativeId,
            userId = userId
        )
    }

    override suspend fun voteForAlternative(
        alternativeId: String,
        category: String,
        userId: String
    ): Boolean {
        return firestoreService.voteForAlternative(
            alternativeId = alternativeId,
            category = category
        )
    }

    override suspend fun getMySubmissions(userId: String): List<Submission> {
        return firestoreService.getMySubmissions(userId)
    }

    override suspend fun checkDuplicateApp(name: String, packageName: String): DuplicateResult {
        return firestoreService.checkDuplicateApp(name, packageName)
    }
}

