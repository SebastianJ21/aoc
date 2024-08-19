package aoc20

import AOCYear
import mapToInt
import readInput

class Day2 {

    fun solve() {
        val rawInput = readInput("day2.txt", AOCYear.Twenty)

        val inputs = rawInput.map { line ->
            val (policy, password) = line.split(": ")

            val (policyRange, policyLetterStr) = policy.split(" ")
            val policyLetter = policyLetterStr.single()

            val range = policyRange.split("-").mapToInt().let { (min, max) -> min..max }

            Triple(password, policyLetter, range)
        }

        val partOne = inputs.count { (password, policyLetter, range) ->
            password.count { it == policyLetter } in range
        }

        val partTwo = inputs.count { (password, policyLetter, range) ->
            val (first, second) = range.first - 1 to range.last - 1

            listOf(password[first] == policyLetter, password[second] == policyLetter).count { it } == 1
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
