package aoc25

import AOCAnswer
import AOCSolution
import at
import countUntil
import plus
import positionsOf
import readInput

class Day4 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day4.txt", AOCYear.TwentyFive)

        val initialRollPositions = rawInput.map { line -> line.toList() }.positionsOf { it == '@' }.toSet()

        val rollsCountSequence = generateSequence(initialRollPositions) { rollPositions ->
            val nextPositions = rollPositions.filter { position ->
                neighborhood.countUntil(4) { direction -> position + direction in rollPositions } >= 4
            }

            nextPositions.toSet()
        }.map { it.size }

        val partOne = rollsCountSequence.take(2).toList().let { (initial, second) -> initial - second }

        // Take while some rolls are removed
        val finalCount = rollsCountSequence.zipWithNext().takeWhile { (a, b) -> a > b }.last().second
        val partTwo = initialRollPositions.size - finalCount

        return AOCAnswer(partOne, partTwo)
    }
}
