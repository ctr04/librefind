package com.jksalcedo.librefind.domain.usecase

import com.jksalcedo.librefind.domain.model.AppItem
import com.jksalcedo.librefind.domain.repository.DeviceInventoryRepo
import kotlinx.coroutines.flow.Flow

/**
 * Use case: Scan device inventory and classify apps
 * 
 * Orchestrates the core workflow:
 * 1. Scan installed packages
 * 2. Classify as FOSS/PROP/UNKN
 * 3. Sort by priority
 * 
 * This is the primary use case for the Dashboard screen.
 */
class ScanInventoryUseCase(
    private val deviceInventoryRepo: DeviceInventoryRepo
) {
    /**
     * Execute the scan and classification
     * 
     * @return Flow emitting the classified and sorted app list
     */
    suspend operator fun invoke(): Flow<List<AppItem>> =
        deviceInventoryRepo.scanAndClassify()
}
