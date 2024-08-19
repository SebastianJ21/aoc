package aoc23

import AOCYear
import mapToInt
import readInput
import kotlin.math.min
import kotlin.math.pow

class Day4 {
    fun solve() {
        val rawInput = readInput("day4.txt", AOCYear.TwentyThree)

        fun String.getWinningNumbersSize() = this
            .split(" | ")
            .let { (drawn, winning) ->
                val winningNumbers = winning.trim().split("  ", " ").mapToInt().toSet()
                val drawnWinningNumbers = drawn.trim().split("  ", " ").filter { it.toInt() in winningNumbers }
                drawnWinningNumbers.size
            }

        val partOne = rawInput.sumOf { line ->
            line.split(": ")
                .drop(1)
                .single()
                .getWinningNumbersSize()
                .let { 2.0.pow(it - 1).toInt() }
        }

        val cardToWinning = rawInput.associate { line ->
            line.split(": ")
                .let { (cardInfo, cardNumbers) ->
                    val cardId = cardInfo.replace(" ", "").replace("Card", "").toInt()
                    cardId to cardNumbers.getWinningNumbersSize()
                }
        }

        val cardToCount = cardToWinning.mapValues { 1 }.toMutableMap()

        val partTwo = cardToWinning.entries.sumOf { (id, winningSize) ->
            val count = cardToCount.getValue(id)
            // Cards index from 1
            if (id == cardToWinning.size) return@sumOf count

            (id + 1..id + winningSize).forEach {
                val cappedId = min(cardToWinning.size, it)

                cardToCount[cappedId] = cardToCount[cappedId]!! + cappedId
            }
            count
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
