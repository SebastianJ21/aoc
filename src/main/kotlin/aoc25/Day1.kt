package aoc25

import AOCAnswer
import AOCSolution
import readInput
import kotlin.math.abs

class Day1 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day1.txt", AOCYear.TwentyFive)

        val transformations = rawInput.map { line ->
            val direction = line.first()
            val value = line.drop(1).toInt()

            val transformFn: (Int) -> Int = when (direction) {
                'L' -> { it -> it - value }
                'R' -> { it -> it + value }
                else -> error("Invalid input $line")
            }

            transformFn
        }

        val startValue = 50
        val totalValues = 100

        val values = transformations.runningFold(startValue) { current, transform -> transform(current) }
        val normValues = values.map { value -> Math.floorMod(value, totalValues) }

        val partOne = normValues.count { it == 0 }

        val partTwo = normValues.zip(transformations) { value, transform ->
            val nextValue = transform(value)
            val crossings = abs(nextValue / totalValues) // Counts crossings and points at 0 when right crossed

            val extra = when {
                nextValue < 0 && value != 0 -> 1 // Left cross
                nextValue == 0 -> 1 // Landed exactly of 0
                else -> 0
            }

            crossings + extra
        }.sum()

        return AOCAnswer(partOne, partTwo)
    }
}
