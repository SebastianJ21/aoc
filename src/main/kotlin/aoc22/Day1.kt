package aoc22

import AOCYear
import readInput
import splitBy

class Day1 {

    fun solve() {
        val rawInput = readInput("day1.txt", AOCYear.TwentyTwo)

        val inputs = rawInput.splitBy({ it.isEmpty() }) { it.toInt() }

        val rowSums = inputs.map { it.sum() }

        val partOne = rowSums.max()
        val partTwo = rowSums.sortedDescending().take(3).sum()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
