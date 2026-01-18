package com.jksalcedo.librefind.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jksalcedo.librefind.domain.model.SovereigntyScore
import com.jksalcedo.librefind.ui.theme.CapturedOrange
import com.jksalcedo.librefind.ui.theme.FossGreen
import com.jksalcedo.librefind.ui.theme.SovereignGold
import com.jksalcedo.librefind.ui.theme.TransitionBlue

@Composable
fun GaugeDetailsDialog(
    score: SovereigntyScore,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Sovereignty Score")
        },
        text = {
            Column {
                Text(
                    text = "Your score represents how much of your digital life is powered by Free and Open Source Software.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Breakdown:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                StatRow(label = "FOSS Apps", value = "${score.fossCount}", color = FossGreen)
                StatRow(label = "Proprietary Apps", value = "${score.proprietaryCount}", color = CapturedOrange)
                StatRow(label = "Unknown Apps", value = "${score.unknownCount}", color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Levels:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LevelRow(label = "Sovereign (80%+)", color = SovereignGold)
                LevelRow(label = "Transitioning (40-79%)", color = TransitionBlue)
                LevelRow(label = "Captured (<40%)", color = CapturedOrange)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun LevelRow(label: String, color: Color) {
    Row(
       modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
       verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
   ) {
       androidx.compose.foundation.layout.Box(
           modifier = Modifier
               .size(12.dp)
               .background(color, CircleShape)
       )
       Spacer(modifier = Modifier.width(8.dp))
       Text(text = label, style = MaterialTheme.typography.bodySmall)
   }
}

@Preview
@Composable
fun PreviewGaugeDetailsDialog() {
    MaterialTheme {
        GaugeDetailsDialog(
            score = SovereigntyScore(100, 45, 45, 10),
            onDismissRequest = {}
        )
    }
}
