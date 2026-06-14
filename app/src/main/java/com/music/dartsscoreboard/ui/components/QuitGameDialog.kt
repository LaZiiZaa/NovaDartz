package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.dartsscoreboard.ui.theme.NovaCoral
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.SpaceCard

@Composable
fun QuitGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = NovaCoral) },
        title = { Text("Quitter la partie ?", fontWeight = FontWeight.ExtraBold) },
        text = {
            Text(
                "La partie est encore en cours. Si vous quittez maintenant, votre progression sera perdue.",
                color = NovaTextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = NovaCoral)
            ) {
                Text("Quitter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = NovaCyan)
            ) {
                Text("Continuer", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SpaceCard,
        shape = RoundedCornerShape(20.dp)
    )
}
