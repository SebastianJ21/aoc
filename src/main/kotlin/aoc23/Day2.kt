package aoc23

import AOCYear
import product
import readInput
import kotlin.math.max

class Day2 {
    private val colorToCubeLimit = mapOf(
        "red" to 12,
        "green" to 13,
        "blue" to 14,
    )

    fun solve() {
        val rawInput = readInput("day2.txt", AOCYear.TwentyThree)

        val partOne = rawInput.sumOf { line ->
            val (id, gameRounds) = line.replace("Game ", "").split(": ")

            val roundDraws = gameRounds
                .split("; ")
                .flatMap { it.split(", ") }

            val isValid = roundDraws.none {
                val (count, color) = it.split(" ")
                colorToCubeLimit.getValue(color) < count.toInt()
            }

            if (isValid) id.toInt() else 0
        }

        val partTwo = rawInput.sumOf { line ->
            val (_, gameRounds) = line.split(": ")

            val roundDraws = gameRounds
                .split("; ")
                .flatMap { it.split(", ") }

            val colorToMaxCount = colorToCubeLimit.mapValues { Int.MIN_VALUE }.toMutableMap()

            roundDraws.forEach {
                val (count, color) = it.split(" ")
                colorToMaxCount[color] = max(count.toInt(), colorToMaxCount.getValue(color))
            }

            // product -> reduce { acc, i -> acc * i }
            colorToMaxCount.values.product()
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
