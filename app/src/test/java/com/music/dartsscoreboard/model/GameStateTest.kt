package com.music.dartsscoreboard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitaires des calculs de statistiques des modèles de jeu (logique pure,
 * sans dépendance Android).
 */
class GameStateTest {

    private fun player(name: String = "P", index: Int = 0) = Player(name, index)

    // ---------------- X01 ----------------

    @Test
    fun x01_average_isThreeDartAverage() {
        val state = X01PlayerState(
            player = player(),
            scoreRemaining = 201,
            throwHistory = listOf(60, 100, 140), // somme = 300
            dartsThrown = 9
        )
        // (300 / 9) * 3 = 100.0
        assertEquals(100.0, state.average, 0.0001)
    }

    @Test
    fun x01_average_isZeroWhenNoDarts() {
        val state = X01PlayerState(player = player(), scoreRemaining = 501)
        assertEquals(0.0, state.average, 0.0001)
    }

    @Test
    fun x01_countsAndExtremes() {
        val state = X01PlayerState(
            player = player(),
            scoreRemaining = 0,
            throwHistory = listOf(180, 140, 100, 99, 26),
            dartsThrown = 15
        )
        assertEquals(1, state.count180)
        assertEquals(2, state.count140Plus)   // 180, 140
        assertEquals(3, state.count100Plus)   // 180, 140, 100
        assertEquals(180, state.bestThrow)
        assertEquals(26, state.worstThrow)
        assertEquals(5, state.rounds)
    }

    @Test
    fun x01_checkoutPercentage() {
        val state = X01PlayerState(
            player = player(),
            scoreRemaining = 0,
            checkoutAttempts = 4,
            checkoutHits = 1
        )
        assertEquals(25.0, state.checkoutPercentage, 0.0001)
    }

    @Test
    fun x01_checkoutPercentage_isZeroWhenNoAttempts() {
        val state = X01PlayerState(player = player(), scoreRemaining = 100)
        assertEquals(0.0, state.checkoutPercentage, 0.0001)
    }

    // ---------------- Cricket ----------------

    @Test
    fun cricket_numberClosedAtThreeMarks() {
        val marks = cricketNumbers.associateWith { 0 }.toMutableMap()
        marks[20] = 3
        marks[19] = 2
        val state = CricketPlayerState(player = player(), marks = marks)
        assertTrue(state.isNumberClosed(20))
        assertFalse(state.isNumberClosed(19))
        assertEquals(1, state.closedCount)
        assertFalse(state.allClosed())
    }

    @Test
    fun cricket_allClosed() {
        val marks = cricketNumbers.associateWith { 3 }
        val state = CricketPlayerState(player = player(), marks = marks)
        assertTrue(state.allClosed())
        assertEquals(cricketNumbers.size, state.closedCount)
    }

    @Test
    fun cricket_marksPerRound() {
        val state = CricketPlayerState(
            player = player(),
            totalMarksHit = 10,
            roundsPlayed = 4
        )
        assertEquals(2.5, state.marksPerRound, 0.0001)
    }

    // ---------------- Around the Clock ----------------

    @Test
    fun atc_targetsHit_progression() {
        assertEquals(0, ATCPlayerState(player = player(), currentTarget = 1).targetsHit)
        assertEquals(4, ATCPlayerState(player = player(), currentTarget = 5).targetsHit)
        // arrivé au Bull mais pas encore touché
        assertEquals(20, ATCPlayerState(player = player(), currentTarget = 25).targetsHit)
        // Bull touché → terminé
        assertEquals(
            21,
            ATCPlayerState(player = player(), currentTarget = 25, isFinished = true).targetsHit
        )
    }

    @Test
    fun atc_accuracy() {
        val state = ATCPlayerState(
            player = player(),
            currentTarget = 11, // 10 cibles touchées
            dartsThrown = 20
        )
        assertEquals(50.0, state.accuracy, 0.0001)
    }

    @Test
    fun atc_nextTargetDisplay() {
        assertEquals("7", ATCPlayerState(player = player(), currentTarget = 7).nextTargetDisplay)
        assertEquals("Bull", ATCPlayerState(player = player(), currentTarget = 25).nextTargetDisplay)
    }

    // ---------------- Count Up ----------------

    @Test
    fun countUp_average() {
        val state = CountUpPlayerState(
            player = player(),
            totalScore = 300,
            throwHistory = listOf(100, 100, 100),
            dartsThrown = 9
        )
        assertEquals(100.0, state.average, 0.0001)
        assertEquals(3, state.rounds)
        assertEquals(3, state.count100Plus)
        assertEquals(0, state.count180)
    }
}
