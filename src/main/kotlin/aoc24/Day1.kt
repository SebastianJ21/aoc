package aoc24

import AOCAnswer
import AOCSolution
import mapToInt
import inputLines
import kotlin.math.abs

class Day1 : AOCSolution {
    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val numberPairs = inputLines.map { line ->
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
