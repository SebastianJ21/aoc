@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import AOCYear
import readInput

class Day2 {

    val plays = listOf("A", "B", "C")
    val aliasPlays = listOf("X", "Y", "Z")

    val mapPlays = aliasPlays.zip(plays).toMap()
        .run { plus(mapKeys { (_, value) -> value }) }

    fun playToScore(play: String) = when (play) {
        "A" -> 1
        "B" -> 2
        "C" -> 3
        else -> error("Invalid play $play")
    }

    fun isPlayWinning(play: String, opponentPlay: String) = when {
        play == "A" && opponentPlay == "C" -> true
        play == "B" && opponentPlay == "A" -> true
        play == "C" && opponentPlay == "B" -> true
        else -> false
    }

    fun getRoundScore(play: String, opponentPlay: String) = when {
        isPlayWinning(play, opponentPlay) -> 6
        play == opponentPlay -> 3
        else -> 0
    }

    fun getPlayForOutcome(opponentPlay: String, outcome: String) = when (outcome) {
        "Y" -> opponentPlay
        "Z" -> plays.first { isPlayWinning(it, opponentPlay) }
        "X" -> plays.first { it != opponentPlay && !isPlayWinning(it, opponentPlay) }
        else -> error("")
    }

    fun solve() {
        val rawInput = readInput("day2.txt", AOCYear.TwentyTwo)

        val partOne = rawInput.sumOf { line ->
            val (opponentPlay, myPlay) = line.split(" ").map { mapPlays.getValue(it) }

            playToScore(myPlay) + getRoundScore(myPlay, opponentPlay)
        }

        val partTwo = rawInput.sumOf { line ->
            val (opponentPlay, outcome) = line.split(" ")

            val play = getPlayForOutcome(opponentPlay, outcome)

            playToScore(play) + getRoundScore(play, opponentPlay)
        }

        println(mapPlays)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
