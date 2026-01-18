package com.jksalcedo.librefind.domain.usecase

import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.repository.KnowledgeGraphRepo

class GetAlternativeUseCase(
    private val knowledgeGraphRepo: KnowledgeGraphRepo
) {
    suspend operator fun invoke(packageName: String): List<Alternative> {
        return knowledgeGraphRepo.getAlternatives(packageName)
            .sortedByDescending { it.ratingAvg }
    }
}

