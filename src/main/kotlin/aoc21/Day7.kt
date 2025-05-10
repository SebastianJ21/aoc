package aoc21

import AOCAnswer
import AOCSolution
import mapToInt
import median
import readInput
import kotlin.math.abs
import kotlin.math.roundToInt

class Day7 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day7.txt", AOCYear.TwentyOne)
        val positions = rawInput.single().split(",").mapToInt()

        val partOne = positions.let { _ ->
            val desiredPosition = positions.median().roundToInt()

            positions.sumOf { position -> abs(position - desiredPosition) }
        }

        val partTwo = positions.let { _ ->
            val minPosition = positions.min()
            val maxPosition = positions.max()

            val lowestScore = (minPosition..maxPosition).minOf { testingPosition ->
                positions.sumOf { position ->
                    val requiredToMove = abs(position - testingPosition)
                    // Sum of natural numbers formula
                    (requiredToMove * (requiredToMove + 1)) / 2
                }
            }

            lowestScore
        }

        return AOCAnswer(partOne, partTwo)
    }
}
