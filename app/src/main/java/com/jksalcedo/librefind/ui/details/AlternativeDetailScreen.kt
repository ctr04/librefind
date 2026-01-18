package com.jksalcedo.librefind.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeDetailScreen(
    altId: String,
    onBackClick: () -> Unit,
    viewModel: AlternativeDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackType by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }

    LaunchedEffect(altId) {
        viewModel.loadAlternative(altId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.alternative?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            state.alternative?.let { alt ->
                                DropdownMenuItem(
                                    text = { Text("Open in F-Droid") },
                                    onClick = {
                                        menuExpanded = false
                                        val fdroidUri =
                                            "market://details?id=${alt.fdroidId}".toUri()
                                        val webUri =
                                            "https://f-droid.org/packages/${alt.fdroidId}/".toUri()
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, fdroidUri)
                                            intent.setPackage("com.fdroid.fdroid")
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    webUri
                                                )
                                            )
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.Shop, null) }
                                )
                                if (alt.website.isNotBlank()) {
                                    DropdownMenuItem(
                                        text = { Text("Open Website") },
                                        onClick = {
                                            menuExpanded = false
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    alt.website.toUri()
                                                )
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Default.Language, null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("View Source") },
                                    onClick = {
                                        menuExpanded = false
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                alt.repoUrl.toUri()
                                            )
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Code, null) }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Copy Package Name") },
                                    onClick = {
                                        menuExpanded = false
                                        val clipboard =
                                            context.getSystemService(ClipboardManager::class.java)
                                        clipboard?.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "Package",
                                                alt.packageName
                                            )
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.alternative == null -> {
                    Text("Alternative not found", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    val alt = state.alternative!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Header
                        Text(
                            alt.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            alt.license,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (index < alt.ratingAvg.toInt()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${alt.displayRating} (${alt.ratingCount})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Rating
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (state.isSignedIn) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Your rating",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row {
                                            (1..5).forEach { star ->
                                                IconButton(
                                                    onClick = { viewModel.rate(star) },
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        "Rate $star",
                                                        tint = if (star <= (alt.userRating
                                                                ?: 0)
                                                        ) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Description
                        if (alt.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(alt.description, style = MaterialTheme.typography.bodyMedium)
                        }

                        // Features
                        if (alt.features.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Features",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            alt.features.forEach { feature ->
                                Text(
                                    "• $feature",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Pros
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Pros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (state.isSignedIn) {
                                IconButton(onClick = {
                                    feedbackType = 0; showFeedbackDialog = true
                                }) {
                                    Icon(Icons.Default.Add, "Add pro")
                                }
                            }
                        }
                        if (alt.pros.isEmpty()) {
                            Text(
                                "No pros yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            alt.pros.forEach { pro ->
                                Text(
                                    "• $pro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Cons
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (state.isSignedIn) {
                                IconButton(onClick = {
                                    feedbackType = 1; showFeedbackDialog = true
                                }) {
                                    Icon(Icons.Default.Add, "Add con")
                                }
                            }
                        }
                        if (alt.cons.isEmpty()) {
                            Text(
                                "No cons yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            alt.cons.forEach { con ->
                                Text(
                                    "• $con",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Feedback dialog
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text(if (feedbackType == 0) "Add a Pro" else "Add a Con") },
            text = {
                Column {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = feedbackType == 0,
                            onClick = { feedbackType = 0 },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("Pro") }
                        SegmentedButton(
                            selected = feedbackType == 1,
                            onClick = { feedbackType = 1 },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("Con") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Your feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.submitFeedback(
                            if (feedbackType == 0) "PRO" else "CON",
                            feedbackText
                        )
                        feedbackText = ""
                        showFeedbackDialog = false
                    },
                    enabled = feedbackText.isNotBlank()
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview
@Composable
fun AltDetailsPreview() {
    AlternativeDetailScreen(
        "",
        onBackClick = {}
    )
}