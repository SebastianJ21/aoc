package aoc20

import AOCYear
import mapToInt
import readInput

class Day15 {

    fun solve() {
        val rawInput = readInput("day15.txt", AOCYear.Twenty)

        val numbers = rawInput.single().split(',').mapToInt()

        val partOne = vanEck(numbers, 2020)
        val partTwo = vanEck(numbers, 30000000)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun vanEck(initial: List<Int>, n: Int): Int {
        // Sadly, the cost of using immutable data structure for Van Eck is too high
        // (more than double the time complexity)
        val occurrences = Array<Int?>(n) { null }

        initial.dropLast(1).forEachIndexed { index, number -> occurrences[number] = index + 1 }

        val startRound = initial.size + 1
        val initialLastNumber = initial.last()

        return (startRound..n).fold(initialLastNumber) { lastNumber, round ->
            val lastOccurrence = occurrences[lastNumber]
            occurrences[lastNumber] = round - 1

            if (lastOccurrence == null) {
                0
            } else {
                round - lastOccurrence - 1
            }
        }
    }
}
