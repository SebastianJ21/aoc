package aoc19

import AOCAnswer
import AOCSolution
import mapToInt
import inputLines

class Day1 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()
        val numbers = inputLines.mapToInt()

        val partOne = numbers.sumOf { calculateFuel(it) }

        val partTwo = numbers.sumOf { mass ->
            val fuelSequence = generateSequence(calculateFuel(mass)) { fuel -> calculateFuel(fuel) }

            fuelSequence.takeWhile { it > 0 }.sum()
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun calculateFuel(value: Int) = value.floorDiv(3) - 2
}
