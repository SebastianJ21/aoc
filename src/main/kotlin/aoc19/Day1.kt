package aoc19

import AOCYear
import mapToInt
import printAOCAnswers
import readInput

class Day1 {

    fun solve() {
        val rawInput = readInput("day1.txt", AOCYear.Nineteen)

        val numbers = rawInput.mapToInt()

        fun calculateFuel(value: Int) = value.floorDiv(3) - 2

        val partOne = numbers.sumOf { calculateFuel(it) }

        val partTwo = numbers.sumOf { mass ->
            generateSequence(calculateFuel(mass)) { fuel ->
                calculateFuel(fuel).takeIf { it > 0 }
            }.sum()
        }

        printAOCAnswers(partOne, partTwo)
    }
}
