package com.jksalcedo.librefind.domain.model

data class Report(
    val id: String = "",
    val title: String,
    val description: String,
    val type: ReportType,
    val status: ReportStatus = ReportStatus.OPEN,
    val priority: ReportPriority = ReportPriority.LOW,
    val submitterUid: String,
    val submitterUsername: String = "",
    val adminResponse: String? = null,
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReportType { BUG, SUGGESTION, QUESTION, OTHER }

enum class ReportStatus {
    OPEN,           // New, not yet reviewed
    IN_PROGRESS,    // Being worked on
    RESOLVED,       // Fixed/Implemented
    WONTFIX,        // Won't be implemented
    DUPLICATE,      // Same as another report
    CLOSED          // Closed without resolution
}

enum class ReportPriority { LOW, MEDIUM, HIGH, CRITICAL }