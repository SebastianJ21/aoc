package aoc25

import AOCAnswer
import AOCSolution
import applyDirection
import countUntil
import plus
import positionsOf
import readInput

class Day4 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    private val neighborhood = listOf(up + left, up, up + right, right, down + right, down, down + left, left)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day4.txt", AOCYear.TwentyFive)

        val initialRollPositions = rawInput.map { line -> line.toList() }.positionsOf { it == '@' }.toSet()

        val rollsCountSequence = generateSequence(initialRollPositions) { rollPositions ->
            val nextPositions = rollPositions.filter { position ->
                neighborhood.countUntil(4) { direction -> position.applyDirection(direction) in rollPositions } >= 4
            }

            nextPositions.toSet()
        }.map { it.size }

        val partOne = rollsCountSequence.zipWithNext { initial, second -> initial - second }.first()

        // Take while some rolls are removed
        val finalCount = rollsCountSequence.zipWithNext().takeWhile { (a, b) -> a > b }.last().second
        val partTwo = initialRollPositions.size - finalCount

        return AOCAnswer(partOne, partTwo)
    }
}
