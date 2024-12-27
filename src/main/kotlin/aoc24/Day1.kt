package aoc24

import AOCAnswer
import AOCSolution
import AOCYear
import mapToInt
import readInput
import kotlin.math.abs

class Day1 : AOCSolution {
    override fun solve(): AOCAnswer {
        val rawInput = readInput("day1.txt", AOCYear.TwentyFour)

        val numberPairs = rawInput.map { line ->
            val (number1, number2) = line.split("   ").mapToInt()

            number1 to number2
        }

        val (leftNumbers, rightNumbers) = numberPairs.unzip()
        val partOne = leftNumbers.sorted().zip(rightNumbers.sorted()) { a, b -> abs(a - b) }.sum()

        val rightNumberToCount = rightNumbers.groupingBy { it }.eachCount()
        val partTwo = leftNumbers.sumOf { it * (rightNumberToCount[it] ?: 0) }

        return AOCAnswer(partOne, partTwo)
    }
}
