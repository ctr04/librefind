package com.jksalcedo.librefind.data.repository

import android.content.pm.PackageInfo
import com.jksalcedo.librefind.data.local.InventorySource
import com.jksalcedo.librefind.data.local.SafeSignatureDb
import com.jksalcedo.librefind.domain.model.AppItem
import com.jksalcedo.librefind.domain.model.AppStatus
import com.jksalcedo.librefind.domain.repository.DeviceInventoryRepo
import com.jksalcedo.librefind.domain.repository.KnowledgeGraphRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Implementation of DeviceInventoryRepo
 * 
 * Implements the three-step classification logic:
 * A. Source Check (Fast Filter)
 * B. Signature Check (Verification)
 * C. Database Query (LibreFind Cloud)
 */
/**
 * Implementation of DeviceInventoryRepo
 * 
 * Implements the three-step classification logic:
 * A. Source Check (Fast Filter)
 * B. Signature Check (Verification)
 * C. Database Query (LibreFind Cloud)
 */
class DeviceInventoryRepoImpl(
    private val localSource: InventorySource,
    private val signatureDb: SafeSignatureDb,
    private val knowledgeRepo: KnowledgeGraphRepo
) : DeviceInventoryRepo {

    companion object {
        private const val FDROID_INSTALLER = "org.fdroid.fdroid"
        private const val PLAY_STORE_INSTALLER = "com.android.vending"
    }

    override suspend fun scanAndClassify(): Flow<List<AppItem>> = flow {
        val rawApps = localSource.getRawApps()
        
        val classifiedApps = coroutineScope {
            rawApps.map { pkg ->
                async { classifyApp(pkg) }
            }.awaitAll()
        }
        
        // Sort by classification priority (PROP > UNKN > FOSS)
        val sorted = classifiedApps.sortedBy { it.status.sortWeight }
        
        emit(sorted)
    }.flowOn(Dispatchers.IO)

    /**
     * Classifies a single app using the three-step logic
     */
    private suspend fun classifyApp(pkg: PackageInfo): AppItem {
        val packageName = pkg.packageName
        val label = localSource.getAppLabel(packageName)
        val installer = localSource.getInstaller(packageName)
        
        // STEP A: Fast Filter - Check installer
        if (installer == FDROID_INSTALLER) {
            return createAppItem(packageName, label, AppStatus.FOSS, installer)
        }
        
        // STEP B: Signature Check (for FOSS apps on Play Store)
        if (signatureDb.isKnownFossApp(packageName)) {
            return createAppItem(packageName, label, AppStatus.FOSS, installer)
        }
        
        // STEP C: Database Query
        val isProprietary = try {
            knowledgeRepo.isProprietary(packageName)
        } catch (e: Exception) {
            false
        }
        
        val status = if (isProprietary) AppStatus.PROP else AppStatus.UNKN
        
        return createAppItem(packageName, label, status, installer)
    }

    /**
     * Creates AppItem with alternatives count
     */
    private suspend fun createAppItem(
        packageName: String,
        label: String,
        status: AppStatus,
        installer: String?
    ): AppItem {
        val alternativesCount = if (status == AppStatus.PROP) {
            try {
                knowledgeRepo.getAlternatives(packageName).size
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
        
        return AppItem(
            packageName = packageName,
            label = label,
            status = status,
            installerId = installer,
            knownAlternatives = alternativesCount
        )
    }

    override fun getInstaller(packageName: String): String? {
        return localSource.getInstaller(packageName)
    }
}
