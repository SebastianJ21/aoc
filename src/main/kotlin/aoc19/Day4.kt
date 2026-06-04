package aoc19

import AOCAnswer
import AOCSolution
import firstAndRest
import mapToInt
import inputLines

class Day4 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val (from, to) = inputLines.single().split("-").mapToInt()

        val range = from..to

        fun isValidPartOne(num: Int): Boolean {
            val (first, rest) = num.toString().firstAndRest()

            val (_, pairExists) = rest.fold(first.digitToInt() to false) { (previous, pairFound), value ->
                val valueDigit = value.digitToInt()

                when {
                    valueDigit < previous -> return false
                    pairFound -> valueDigit to true
                    else -> valueDigit to (valueDigit == previous)
                }
            }

            return pairExists
        }

        fun isValidPartTwo(num: Int): Boolean {
            val (first, rest) = num.toString().firstAndRest()
            val firstDigit = first.digitToInt()

            val countArray = Array(10) { 0 }
            countArray[firstDigit] = 1

            rest.fold(firstDigit) { previous, value ->
                val valueDigit = value.digitToInt()

                if (valueDigit < previous) return false

                countArray[valueDigit]++

                valueDigit
            }

            return countArray.any { it == 2 }
        }

        val partOne = range.count { isValidPartOne(it) }
        val partTwo = range.count { isValidPartTwo(it) }

        return AOCAnswer(partOne, partTwo)
    }
}
