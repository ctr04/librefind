package com.jksalcedo.librefind.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jksalcedo.librefind.ui.dashboard.components.GaugeDetailsDialog
import com.jksalcedo.librefind.ui.dashboard.components.ScanList
import com.jksalcedo.librefind.ui.dashboard.components.SovereigntyGauge
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAppClick: (String, String) -> Unit,
    onSubmitClick: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LibreFind",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.scan() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh scan"
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSubmitClick) {
                Icon(Icons.Default.Add, contentDescription = "Submit App")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.scan() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    // App List
                    if (state.apps.isEmpty()) {
                        // Show gauge even when empty
                        Column(modifier = Modifier.fillMaxSize()) {
                            state.sovereigntyScore?.let { score ->
                                SovereigntyGauge(
                                    score = score,
                                    modifier = Modifier.padding(16.dp)
                                ) { showDialog = true }
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No apps found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        ScanList(
                            apps = state.apps,
                            onAppClick = onAppClick,
                            modifier = Modifier.fillMaxSize(),
                            headerContent = {
                                state.sovereigntyScore?.let { score ->
                                    SovereigntyGauge(
                                        score = score,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) { showDialog = true }
                                }
                            }
                        )
                    }
                }
            }
            
            if (showDialog) {
                state.sovereigntyScore?.let { score ->
                    GaugeDetailsDialog(
                        score = score,
                        onDismissRequest = { showDialog = false }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDashboard() {
    DashboardScreen(
        onAppClick = {_, _ -> },
        viewModel = koinViewModel()
    )
}