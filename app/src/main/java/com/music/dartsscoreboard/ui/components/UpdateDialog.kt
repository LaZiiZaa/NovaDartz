package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.dartsscoreboard.viewmodel.UpdateUiState
import com.music.dartsscoreboard.ui.theme.*

/**
 * Boîte de dialogue « Mise à jour disponible ».
 * Affiche la version, les notes, et une barre de progression pendant le téléchargement.
 */
@Composable
fun UpdateDialog(
    state: UpdateUiState,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val release = state.release ?: return

    AlertDialog(
        onDismissRequest = { if (!state.downloading) onDismiss() },
        icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = NovaCyan) },
        title = {
            Text("Mise à jour disponible", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column {
                Text(
                    "Version ${release.versionName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = StarGold,
                    fontWeight = FontWeight.Bold
                )
                if (release.notes.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        release.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary,
                        modifier = Modifier
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
                if (state.downloading) {
                    Spacer(Modifier.height(16.dp))
                    Text("Téléchargement… ${state.progress}%", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = NovaCyan,
                        trackColor = NovaTextMuted.copy(alpha = 0.3f)
                    )
                }
                state.message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = NovaCoral)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpdate,
                enabled = !state.downloading,
                colors = ButtonDefaults.textButtonColors(contentColor = NovaCyan)
            ) {
                Text(if (state.downloading) "En cours…" else "Mettre à jour", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!state.downloading) {
                TextButton(onClick = onDismiss) {
                    Text("Plus tard", color = NovaTextSecondary)
                }
            }
        },
        containerColor = SpaceCard,
        shape = RoundedCornerShape(20.dp)
    )
}
