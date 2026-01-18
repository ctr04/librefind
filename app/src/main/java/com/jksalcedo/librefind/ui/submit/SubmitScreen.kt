package com.jksalcedo.librefind.ui.submit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jksalcedo.librefind.domain.model.SubmissionType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SubmitViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var type by remember { mutableStateOf(SubmissionType.NEW_ALTERNATIVE) }
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var repoUrl by remember { mutableStateOf("") }
    var fdroidId by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var proprietaryPackage by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit App") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What are you submitting?",
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == SubmissionType.NEW_ALTERNATIVE,
                    onClick = { type = SubmissionType.NEW_ALTERNATIVE },
                    label = { Text("FOSS Alternative") }
                )
                FilterChip(
                    selected = type == SubmissionType.NEW_PROPRIETARY,
                    onClick = { type = SubmissionType.NEW_PROPRIETARY },
                    label = { Text("Proprietary App") }
                )
            }

            HorizontalDivider()

            OutlinedTextField(
                value = appName,
                onValueChange = { appName = it },
                label = { Text("App Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Package Name *") },
                placeholder = { Text("com.example.app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            if (type == SubmissionType.NEW_ALTERNATIVE) {
                HorizontalDivider()

                Text(
                    text = "Alternative Details",
                    style = MaterialTheme.typography.titleMedium
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = proprietaryPackage,
                        onValueChange = { proprietaryPackage = it },
                        label = { Text("Target Proprietary App") },
                        placeholder = { Text("Select or type package") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        uiState.proprietaryTargets.filter {
                            it.contains(proprietaryPackage, ignoreCase = true)
                        }.take(5).forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text(pkg) },
                                onClick = {
                                    proprietaryPackage = pkg
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://github.com/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fdroidId,
                    onValueChange = { fdroidId = it },
                    label = { Text("F-Droid ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it },
                    label = { Text("License") },
                    placeholder = { Text("GPL-3.0, Apache-2.0, MIT, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.submit(
                        type = type,
                        appName = appName,
                        packageName = packageName,
                        description = description,
                        repoUrl = repoUrl,
                        fdroidId = fdroidId,
                        license = license,
                        proprietaryPackage = proprietaryPackage
                    )
                },
                enabled = appName.isNotBlank() && packageName.isNotBlank() && description.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit for Review")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
