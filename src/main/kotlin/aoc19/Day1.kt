package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import mapToInt
import readInput

class Day1 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day1.txt", AOCYear.Nineteen)

        val numbers = rawInput.mapToInt()

        fun calculateFuel(value: Int) = value.floorDiv(3) - 2

        val partOne = numbers.sumOf { calculateFuel(it) }

        val partTwo = numbers.sumOf { mass ->
            generateSequence(calculateFuel(mass)) { fuel ->
                calculateFuel(fuel).takeIf { it > 0 }
            }.sum()
        }

        return AOCAnswer(partOne, partTwo)
    }
}
