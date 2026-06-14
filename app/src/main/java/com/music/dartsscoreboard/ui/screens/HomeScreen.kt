package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.SavedPlayerEntity
import com.music.dartsscoreboard.model.GameType
import com.music.dartsscoreboard.ui.theme.*

@Composable
fun HomeScreen(
    savedPlayers: List<SavedPlayerEntity>,
    onAddPlayer: (String) -> Unit,
    onDeletePlayer: (SavedPlayerEntity) -> Unit,
    onRenamePlayer: (Long, String) -> Unit,
    onStartGame: (GameType, List<String>, Boolean, Boolean) -> Unit,
    onViewHistory: () -> Unit,
    onViewStats: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var selectedGameType by remember { mutableStateOf(GameType.FIVE_O_ONE) }
    var doubleOut by remember { mutableStateOf(false) }
    var isTeamMode by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    // Ordre de selection (definit l'ordre de jeu) ; peut etre melange aleatoirement.
    var selectedPlayerIds by remember { mutableStateOf(listOf<Long>()) }
    var editingPlayerId by remember { mutableStateOf<Long?>(null) }
    var editingName by remember { mutableStateOf("") }

    val selectedNames = selectedPlayerIds.mapNotNull { id -> savedPlayers.find { it.id == id }?.name }

    val supportsTeamMode = selectedNames.size >= 4 && (
            selectedGameType == GameType.THREE_O_ONE ||
            selectedGameType == GameType.FIVE_O_ONE ||
            selectedGameType == GameType.CRICKET)

    // Reset team mode when conditions no longer met
    LaunchedEffect(selectedNames.size, selectedGameType) {
        if (!supportsTeamMode) isTeamMode = false
    }

    // Force exactly 4 selected in team mode
    LaunchedEffect(isTeamMode) {
        if (isTeamMode && selectedNames.size != 4) {
            val first4 = savedPlayers.take(4).map { it.id }
            selectedPlayerIds = first4
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Fermer", color = StarGold, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("NovaDartz", fontWeight = FontWeight.ExtraBold, color = StarGold)
            },
            text = {
                Column {
                    Text(
                        "Application de scoreboard pour fléchettes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Créateur", style = MaterialTheme.typography.labelLarge, color = NovaCyan)
                    Text("Julien V", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Licence", style = MaterialTheme.typography.labelLarge, color = NovaCoral)
                    Text(
                        "Toute republication, redistribution ou copie de cette application, en tout ou en partie, est strictement interdite sans l'autorisation écrite préalable du créateur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary
                    )
                }
            },
            containerColor = SpaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Brand header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "NovaDartz",
                style = TextStyle(
                    brush = NovaPrimaryGradient,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                "Configurez votre partie",
                style = MaterialTheme.typography.bodyMedium,
                color = NovaTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Game mode ----
        SectionHeader("Mode de jeu", modifier = Modifier.align(Alignment.Start), accent = NovaViolet)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(GameType.THREE_O_ONE, GameType.FIVE_O_ONE, GameType.CRICKET).forEach { type ->
                GameTypeChip(type, selectedGameType == type, { selectedGameType = type }, Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(GameType.AROUND_THE_CLOCK, GameType.COUNT_UP, GameType.KILLER).forEach { type ->
                GameTypeChip(type, selectedGameType == type, { selectedGameType = type }, Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            selectedGameType.description,
            style = MaterialTheme.typography.bodySmall,
            color = NovaTextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )

        // ---- Options ----
        if ((selectedGameType == GameType.THREE_O_ONE || selectedGameType == GameType.FIVE_O_ONE) || supportsTeamMode) {
            Spacer(modifier = Modifier.height(14.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                if (selectedGameType == GameType.THREE_O_ONE || selectedGameType == GameType.FIVE_O_ONE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Double Out", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Finir sur un double", style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
                        }
                        Switch(
                            checked = doubleOut,
                            onCheckedChange = { doubleOut = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StarGold,
                                checkedTrackColor = StarGold.copy(alpha = 0.45f),
                                checkedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                if (supportsTeamMode) {
                    if (selectedGameType == GameType.THREE_O_ONE || selectedGameType == GameType.FIVE_O_ONE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = HairlineDivider)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Mode Équipe (2v2)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("4 joueurs requis", style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
                        }
                        Switch(
                            checked = isTeamMode,
                            onCheckedChange = { isTeamMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaViolet,
                                checkedTrackColor = NovaViolet.copy(alpha = 0.45f),
                                checkedBorderColor = Color.Transparent
                            )
                        )
                    }
                    if (isTeamMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Équipe 1 : ${selectedNames.getOrElse(0) { "J1" }} & ${selectedNames.getOrElse(1) { "J2" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NovaCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Équipe 2 : ${selectedNames.getOrElse(2) { "J3" }} & ${selectedNames.getOrElse(3) { "J4" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NovaCoral,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Ordre : ${selectedNames.getOrElse(0) { "J1" }} → ${selectedNames.getOrElse(2) { "J3" }} → ${selectedNames.getOrElse(1) { "J2" }} → ${selectedNames.getOrElse(3) { "J4" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ---- Players ----
        SectionHeader("Joueurs", modifier = Modifier.align(Alignment.Start), accent = NovaCyan)
        Spacer(modifier = Modifier.height(12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(12.dp)) {
            if (savedPlayers.isEmpty()) {
                Text(
                    "Aucun joueur enregistré. Ajoutez-en ci-dessous.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            savedPlayers.forEach { player ->
                val isSelected = player.id in selectedPlayerIds
                val maxReached = selectedNames.size >= 8 && !isSelected
                val isEditing = editingPlayerId == player.id

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        enabled = !maxReached && !isTeamMode,
                        onCheckedChange = { checked ->
                            selectedPlayerIds = if (checked) selectedPlayerIds + player.id
                            else selectedPlayerIds - player.id
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = StarGold,
                            checkmarkColor = SpaceBlack,
                            uncheckedColor = NovaTextMuted
                        )
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = novaTextFieldColors()
                        )
                        IconButton(onClick = {
                            if (editingName.isNotBlank()) onRenamePlayer(player.id, editingName)
                            editingPlayerId = null
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Valider", tint = StarGold)
                        }
                    } else {
                        Text(
                            text = player.name,
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StarGold else NovaTextPrimary
                        )
                        IconButton(onClick = {
                            editingPlayerId = player.id
                            editingName = player.name
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Renommer",
                                tint = NovaTextSecondary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = {
                            selectedPlayerIds = selectedPlayerIds - player.id
                            onDeletePlayer(player)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer",
                                tint = NovaCoral, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Add new player
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nom du joueur", color = NovaTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = novaTextFieldColors()
                )
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NovaViolet)
                        .clickable {
                            if (newPlayerName.isNotBlank()) {
                                onAddPlayer(newPlayerName.trim())
                                newPlayerName = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
                }
            }
        }

        if (selectedNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${selectedNames.size} joueur${if (selectedNames.size > 1) "s" else ""} sélectionné${if (selectedNames.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = StarGold
                )
                if (selectedNames.size >= 2) {
                    TextButton(
                        onClick = { selectedPlayerIds = selectedPlayerIds.shuffled() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = NovaViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mélanger", color = NovaViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (selectedNames.size >= 2 && !isTeamMode) {
                Text(
                    "Ordre : ${selectedNames.joinToString("  →  ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NovaTextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Start ----
        val canStart = selectedNames.isNotEmpty() &&
                !(selectedGameType == GameType.KILLER && selectedNames.size < 2) &&
                !(isTeamMode && selectedNames.size != 4)
        GradientButton(
            onClick = { onStartGame(selectedGameType, selectedNames, doubleOut, isTeamMode) },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Démarrer la partie", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (selectedNames.isEmpty()) {
            HintText("Sélectionnez au moins 1 joueur")
        } else if (selectedGameType == GameType.KILLER && selectedNames.size < 2) {
            HintText("Killer nécessite au moins 2 joueurs")
        } else if (isTeamMode && selectedNames.size != 4) {
            HintText("Le mode équipe nécessite exactement 4 joueurs")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Stats & History ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GhostButton(onClick = onViewStats, modifier = Modifier.weight(1f).height(50.dp), accent = NovaCyan) {
                Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stats", fontWeight = FontWeight.Bold)
            }
            GhostButton(onClick = onViewHistory, modifier = Modifier.weight(1f).height(50.dp), accent = NovaCyan) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Historique", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { showAboutDialog = true }) {
            Icon(Icons.Default.Info, contentDescription = null, tint = NovaTextMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("À propos", color = NovaTextMuted)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = NovaCoral,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun GameTypeChip(
    type: GameType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val fill = if (isSelected) {
        Brush.verticalGradient(listOf(NovaViolet.copy(alpha = 0.9f), NovaViolet.copy(alpha = 0.55f)))
    } else {
        Brush.verticalGradient(listOf(Color(0x14FFFFFF), Color(0x08FFFFFF)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill, shape)
            .border(1.dp, if (isSelected) NovaViolet else GlassStroke, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            type.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = if (type.displayName.length > 6) 11.sp else 14.sp,
            color = if (isSelected) Color.White else NovaTextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun novaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovaCyan,
    unfocusedBorderColor = GlassStroke,
    focusedTextColor = NovaTextPrimary,
    unfocusedTextColor = NovaTextPrimary,
    cursorColor = NovaCyan,
    focusedContainerColor = Color(0x0AFFFFFF),
    unfocusedContainerColor = Color(0x0AFFFFFF)
)
