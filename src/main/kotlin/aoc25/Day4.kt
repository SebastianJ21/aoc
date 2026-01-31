package aoc25

import AOCAnswer
import AOCSolution
import countUntil
import positionsOf
import readInput

class Day4 : AOCSolution {

    private val up = Position(-1, 0)
    private val down = Position(1, 0)
    private val left = Position(0, -1)
    private val right = Position(0, 1)

    private data class Position(val first: Int, val second: Int)
    private operator fun Position.plus(other: Position): Position = Position(first + other.first, second + other.second)

    private val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day4.txt", AOCYear.TwentyFive)

        val initialRollPositions = rawInput.map { line -> line.toList() }
            .positionsOf { it == '@' }
            .map { Position(it.first, it.second) }
            .toSet()

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
