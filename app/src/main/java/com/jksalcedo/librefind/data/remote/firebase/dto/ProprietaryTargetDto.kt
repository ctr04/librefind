package com.jksalcedo.librefind.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object for Firestore "proprietary_targets" collection
 * 
 * Firestore Schema:
 * - doc_id: Sanitized package name (e.g., "com_whatsapp")
 * - name: Human-readable name
 * - icon: Icon URL
 * - category: App category
 * - alternatives: Array of alternative IDs
 */
data class ProprietaryTargetDto(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",
    
    @get:PropertyName("icon") @set:PropertyName("icon")
    var icon: String = "",
    
    @get:PropertyName("category") @set:PropertyName("category")
    var category: String = "",
    
    @get:PropertyName("alternatives") @set:PropertyName("alternatives")
    var alternatives: List<String> = emptyList(),
    
    @get:PropertyName("package_name") @set:PropertyName("package_name")
    var packageName: String = ""
)
