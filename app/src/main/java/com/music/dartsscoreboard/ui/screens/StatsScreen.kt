package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.*
import com.music.dartsscoreboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    games: List<GameHistoryEntity>,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

    val analytics = remember(games) { StatsAnalytics.analyze(games) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NovaCoral) },
            title = { Text("Tout réinitialiser ?", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(
                    "Cela supprimera définitivement toutes les statistiques et l'historique des parties. Cette action est irréversible.",
                    color = NovaTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onReset(); showResetDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = NovaCoral)
                ) { Text("Réinitialiser", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annuler", color = NovaTextSecondary)
                }
            },
            containerColor = SpaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Statistiques", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                if (!analytics.isEmpty) {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Réinitialiser", tint = NovaCoral)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (analytics.isEmpty) {
            EmptyState(Icons.Default.BarChart, "Aucune statistique", "Jouez des parties pour voir vos stats !")
            return@Column
        }

        SegmentedTabs(
            options = listOf("Global", "Joueurs", "Duel"),
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (tab) {
            0 -> GlobalTab(analytics)
            1 -> PlayersTab(analytics)
            else -> DuelTab(analytics)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Onglet GLOBAL : podium + résumé + répartition par mode + records  */
/* ------------------------------------------------------------------ */

@Composable
private fun GlobalTab(a: DartsAnalytics) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionHeader("Classement", accent = StarGold) }
        item { PodiumCard(a.players.filter { it.played > 0 }.take(3)) }

        item { SectionHeader("Vue d'ensemble", accent = NovaCyan) }
        item { GlobalSummaryCard(a.global) }

        if (a.global.gamesPerMode.isNotEmpty()) {
            item { SectionHeader("Modes joués", accent = NovaViolet) }
            item { ModeDistributionCard(a.global.gamesPerMode) }
        }

        if (a.records.isNotEmpty()) {
            item { SectionHeader("Records", accent = StarGold) }
            items(a.records.size) { i -> RecordCard(a.records[i]) }
        }
    }
}

@Composable
private fun GlobalSummaryCard(g: GlobalSummary) {
    val tiles = buildList {
        add(Triple("Parties", "${g.totalGames}", NovaCyan))
        add(Triple("Joueurs", "${g.totalPlayers}", NovaViolet))
        add(Triple("Fléchettes", "${g.totalDarts}", NovaCyan))
        add(Triple("Temps de jeu", formatMinutes(g.totalPlayMinutes), StarGold))
        add(Triple("Durée moyenne", "%.0f min".format(g.avgDurationMin), NovaTextPrimary))
        add(Triple("Mode favori", g.mostPlayedMode ?: "-", NovaViolet))
        add(Triple("Total 180", "${g.total180}", StarGold))
        add(Triple("Total 140+", "${g.total140}", NovaGreen))
        add(Triple("Total 100+", "${g.total100}", NovaGreen))
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        StatGrid(tiles)
        g.firstPlayed?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                "Depuis le ${dateOnly(it)}",
                style = MaterialTheme.typography.labelSmall,
                color = NovaTextMuted
            )
        }
    }
}

@Composable
private fun ModeDistributionCard(perMode: List<Pair<String, Int>>) {
    val maxPlayed = perMode.maxOf { it.second }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        perMode.forEachIndexed { i, (mode, count) ->
            if (i > 0) Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(mode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NovaCyan)
            }
            Spacer(Modifier.height(4.dp))
            ThinBar(progress = count.toFloat() / maxPlayed, color = NovaViolet)
        }
    }
}

@Composable
private fun RecordCard(r: StatRecord) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StarGold.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StarGold, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.labelMedium, color = NovaTextSecondary)
                Text(r.holder, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
                Text(r.context, style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
            }
            Text(r.value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = StarGold)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Onglet JOUEURS : sélecteur + détail complet + graphique + par mode */
/* ------------------------------------------------------------------ */

@Composable
private fun PlayersTab(a: DartsAnalytics) {
    val players = a.players.filter { it.played > 0 }
    if (players.isEmpty()) {
        EmptyState(Icons.Default.Person, "Aucun joueur", "Jouez une partie d'abord.")
        return
    }
    var selected by remember(players) { mutableStateOf(players.first().name) }
    val player = players.find { it.name == selected } ?: players.first()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ChipRow(
                names = players.map { it.name },
                selected = selected,
                accent = NovaCyan,
                onSelect = { selected = it }
            )
        }

        item { SectionHeader(player.name, accent = StarGold) }
        item { PlayerOverviewCard(player) }

        item { SectionHeader("Évolution", accent = NovaCyan) }
        item { EvolutionCard(player) }

        if (player.perMode.isNotEmpty()) {
            item { SectionHeader("Par mode de jeu", accent = NovaViolet) }
            item { PerModeCard(player) }
        }
    }
}

@Composable
private fun PlayerOverviewCard(p: PlayerAnalytics) {
    val streakText = when {
        p.currentStreak > 0 -> "${p.currentStreak} ✓"
        p.currentStreak < 0 -> "${-p.currentStreak} ✗"
        else -> "-"
    }
    val tiles = buildList {
        add(Triple("Parties", "${p.played}", NovaCyan))
        add(Triple("Victoires", "${p.won}", NovaGreen))
        add(Triple("Défaites", "${p.lost}", NovaCoral))
        add(Triple("Taux de victoire", "${p.winRate.roundToInt()}%", StarGold))
        add(Triple("Série en cours", streakText, if (p.currentStreak >= 0) NovaGreen else NovaCoral))
        add(Triple("Meilleure série", "${p.bestWinStreak} ✓", NovaGreen))
        if (p.avgThreeDart > 0) add(Triple("Moyenne 3 fléch.", "%.1f".format(p.avgThreeDart), NovaCyan))
        if (p.bestAverage > 0) add(Triple("Meilleure moyenne", "%.1f".format(p.bestAverage), NovaViolet))
        if (p.checkoutAttempts > 0) add(Triple("Checkout %", "${p.checkoutRate.roundToInt()}%", NovaGreen))
        if (p.highestFinish > 0) add(Triple("Plus haut finish", "${p.highestFinish}", StarGold))
        if (p.total180 > 0) add(Triple("Total 180", "${p.total180}", StarGold))
        if (p.total140 > 0) add(Triple("Total 140+", "${p.total140}", NovaGreen))
        if (p.total100 > 0) add(Triple("Total 100+", "${p.total100}", NovaGreen))
        if (p.total60 > 0) add(Triple("Total 60+", "${p.total60}", NovaTextPrimary))
        if (p.totalKills > 0) add(Triple("Éliminations", "${p.totalKills}", NovaCoral))
        if (p.totalMarks > 0) add(Triple("Marques (Cricket)", "${p.totalMarks}", NovaViolet))
        if (p.bestCountUp > 0) add(Triple("Record Count Up", "${p.bestCountUp}", StarGold))
        add(Triple("Fléchettes", "${p.totalDarts}", NovaTextPrimary))
        add(Triple("Temps de jeu", formatMinutes(p.playMinutes), NovaTextPrimary))
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        StatGrid(tiles)
        Spacer(Modifier.height(12.dp))
        Text("Taux de victoire", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
        Spacer(Modifier.height(6.dp))
        ThinBar(progress = (p.winRate / 100).toFloat(), color = NovaCyan, height = 8.dp)
    }
}

@Composable
private fun EvolutionCard(p: PlayerAnalytics) {
    val avgPoints = p.timeline.filter { it.average > 0 }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        if (avgPoints.size >= 2) {
            Text("Moyenne 3 fléchettes par partie", style = MaterialTheme.typography.labelMedium, color = NovaTextSecondary)
            Spacer(Modifier.height(10.dp))
            LineChart(
                values = avgPoints.map { it.average.toFloat() },
                lineColor = NovaCyan,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("min %.1f".format(avgPoints.minOf { it.average }), style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
                Text("max %.1f".format(avgPoints.maxOf { it.average }), style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
            }
            Spacer(Modifier.height(14.dp))
        } else {
            Text(
                "Pas assez de données chiffrées pour tracer une courbe (joue des parties X01 ou Count Up).",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextMuted
            )
            Spacer(Modifier.height(12.dp))
        }

        Text("Résultats récents", style = MaterialTheme.typography.labelMedium, color = NovaTextSecondary)
        Spacer(Modifier.height(8.dp))
        ResultStrip(p.timeline)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(NovaGreen, "Victoire")
            LegendDot(NovaCoral.copy(alpha = 0.75f), "Défaite")
        }
    }
}

@Composable
private fun PerModeCard(p: PlayerAnalytics) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        p.perMode.forEachIndexed { i, m ->
            if (i > 0) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = HairlineDivider)
                Spacer(Modifier.height(10.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(m.mode, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                Text("${m.won}/${m.played} · ${m.winRate.roundToInt()}%", fontWeight = FontWeight.Bold, color = NovaCyan)
            }
            Spacer(Modifier.height(4.dp))
            ThinBar(progress = (m.winRate / 100).toFloat(), color = StarGold)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Onglet DUEL : tête-à-tête entre deux joueurs                       */
/* ------------------------------------------------------------------ */

@Composable
private fun DuelTab(a: DartsAnalytics) {
    val names = a.players.filter { it.played > 0 }.map { it.name }
    if (names.size < 2) {
        EmptyState(Icons.Default.People, "Pas assez de joueurs", "Il faut au moins 2 joueurs ayant joué ensemble.")
        return
    }
    var playerA by remember(names) { mutableStateOf(names[0]) }
    var playerB by remember(names) { mutableStateOf(names[1]) }
    val h2h = remember(playerA, playerB, a) { a.headToHead(playerA, playerB) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Joueur 1", style = MaterialTheme.typography.labelMedium, color = NovaCyan)
            Spacer(Modifier.height(6.dp))
            ChipRow(names, playerA, NovaCyan, onSelect = { playerA = it })
        }
        item {
            Text("Joueur 2", style = MaterialTheme.typography.labelMedium, color = NovaCoral)
            Spacer(Modifier.height(6.dp))
            ChipRow(names, playerB, NovaCoral, onSelect = { playerB = it })
        }
        item {
            if (playerA == playerB) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Choisis deux joueurs différents.", color = NovaTextMuted)
                }
            } else {
                HeadToHeadCard(h2h)
            }
        }
    }
}

@Composable
private fun HeadToHeadCard(h: HeadToHead) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        if (h.commonGames == 0) {
            Text("${h.a} et ${h.b} n'ont encore jamais joué ensemble.", color = NovaTextMuted)
            return@GlassCard
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DuelSide(h.a, h.aWins, NovaCyan, Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("VS", fontWeight = FontWeight.Black, color = NovaTextMuted)
                Text("${h.commonGames} parties", style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
            }
            DuelSide(h.b, h.bWins, NovaCoral, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))
        // Barre de domination
        val total = (h.aWins + h.bWins).coerceAtLeast(1)
        Row(
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
        ) {
            Box(modifier = Modifier.weight(h.aWins.toFloat().coerceAtLeast(0.001f)).fillMaxHeight().background(NovaCyan))
            Box(modifier = Modifier.weight(h.bWins.toFloat().coerceAtLeast(0.001f)).fillMaxHeight().background(NovaCoral))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "${(h.aWins * 100 / total)}% — ${(h.bWins * 100 / total)}%" +
                if (h.draws > 0) "  (${h.draws} nuls)" else "",
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextMuted
        )

        if (h.perMode.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = HairlineDivider)
            Spacer(Modifier.height(10.dp))
            Text("Détail par mode", style = MaterialTheme.typography.labelMedium, color = NovaTextSecondary)
            Spacer(Modifier.height(8.dp))
            h.perMode.forEach { (mode, aw, bw) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$aw", fontWeight = FontWeight.Bold, color = NovaCyan, modifier = Modifier.width(28.dp))
                    Text(mode, style = MaterialTheme.typography.bodySmall, color = NovaTextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("$bw", fontWeight = FontWeight.Bold, color = NovaCoral, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun DuelSide(name: String, wins: Int, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, fontWeight = FontWeight.Bold, color = NovaTextPrimary, maxLines = 1, textAlign = TextAlign.Center)
        Text("$wins", fontSize = 34.sp, fontWeight = FontWeight.Black, color = accent)
        Text("victoires", style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
    }
}

/* ------------------------------------------------------------------ */
/*  Composants partagés                                                */
/* ------------------------------------------------------------------ */

@Composable
private fun SegmentedTabs(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, GlassStroke, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (active) Modifier.background(NovaPrimaryGradient)
                        else Modifier
                    )
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) Color.White else NovaTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ChipRow(
    names: List<String>,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        names.forEach { name ->
            val active = name == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (active) accent.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f))
                    .border(1.dp, if (active) accent else GlassStroke, RoundedCornerShape(20.dp))
                    .clickable { onSelect(name) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    name,
                    color = if (active) accent else NovaTextSecondary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatGrid(tiles: List<Triple<String, String, Color>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value, color) ->
                    StatTile(label, value, color, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, GlassStroke, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary, maxLines = 2)
    }
}

@Composable
private fun ThinBar(progress: Float, color: Color, height: Dp = 6.dp) {
    val p = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(p)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

@Composable
private fun ResultStrip(timeline: List<GamePoint>) {
    val last = timeline.takeLast(22)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        last.forEach { gp ->
            Box(
                modifier = Modifier
                    .size(width = 9.dp, height = 20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (gp.isWin) NovaGreen else NovaCoral.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = NovaTextMuted)
    }
}

@Composable
private fun LineChart(
    values: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxV = values.max()
        val minV = values.min()
        val range = (maxV - minV).takeIf { it > 0f } ?: 1f
        val padY = size.height * 0.12f
        val usableH = size.height - padY * 2

        fun pointAt(i: Int): Offset {
            val x = if (values.size == 1) size.width / 2 else size.width * i / (values.size - 1)
            val y = padY + usableH - ((values[i] - minV) / range) * usableH
            return Offset(x, y)
        }

        val pts = values.indices.map { pointAt(it) }

        if (pts.size == 1) {
            drawCircle(lineColor, radius = 6f, center = pts.first())
            return@Canvas
        }

        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (k in 1 until pts.size) lineTo(pts[k].x, pts[k].y)
        }
        val fill = Path().apply {
            moveTo(pts.first().x, size.height)
            for (p in pts) lineTo(p.x, p.y)
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(
            fill,
            brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), Color.Transparent))
        )
        drawPath(line, color = lineColor, style = Stroke(width = 4f))
        pts.forEach { drawCircle(lineColor, radius = 4f, center = it) }
    }
}

@Composable
private fun PodiumCard(topPlayers: List<PlayerAnalytics>) {
    if (topPlayers.isEmpty()) return
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val order = when (topPlayers.size) {
                1 -> listOf(topPlayers[0])
                2 -> listOf(topPlayers[1], topPlayers[0])
                else -> listOf(topPlayers[1], topPlayers[0], topPlayers[2])
            }
            val heights = when (topPlayers.size) {
                1 -> listOf(120); 2 -> listOf(90, 120); else -> listOf(90, 120, 70)
            }
            val medals = when (topPlayers.size) {
                1 -> listOf(StarGold)
                2 -> listOf(Color(0xFFC0C0C0), StarGold)
                else -> listOf(Color(0xFFC0C0C0), StarGold, Color(0xFFCD7F32))
            }
            val positions = when (topPlayers.size) {
                1 -> listOf("1er"); 2 -> listOf("2e", "1er"); else -> listOf("2e", "1er", "3e")
            }

            order.forEachIndexed { index, player ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text(player.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, textAlign = TextAlign.Center, color = NovaTextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heights[index].dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            .background(medals[index].copy(alpha = 0.22f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(positions[index], fontSize = 12.sp, color = medals[index], fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = medals[index], modifier = Modifier.size(28.dp))
                        Text("${player.won}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = medals[index])
                        Text("victoires", fontSize = 10.sp, color = NovaTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = NovaTextMuted)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = NovaTextSecondary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NovaTextMuted)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Helpers de formatage                                               */
/* ------------------------------------------------------------------ */

private fun formatMinutes(min: Int): String =
    if (min >= 60) "${min / 60}h${(min % 60).toString().padStart(2, '0')}" else "$min min"

private fun dateOnly(ts: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(ts))
