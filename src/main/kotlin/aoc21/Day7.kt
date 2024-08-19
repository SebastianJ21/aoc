package aoc21

import mapToInt
import median
import readInput
import kotlin.math.abs
import kotlin.math.round

class Day7 {

    fun solve() {
        val rawInput = readInput("day7.txt", AOCYear.TwentyOne)
        val positions = rawInput.single().split(",").mapToInt()

        val partOne = positions.run {
            val desiredPos = round(median()).toInt()
            sumOf { position -> abs(position - desiredPos) }
        }

        val partTwo = positions.run {
            val minPosition = minOf { it }
            val maxPosition = maxOf { it }

            val lowestScore = (minPosition..maxPosition).minOf { testingPosition ->
                sumOf { position ->
                    val requiredToMove = abs(position - testingPosition)
                    // Sum of natural numbers formula
                    (requiredToMove * (requiredToMove + 1)) / 2
                }
            }
            lowestScore
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
