package aoc22

import AOCYear
import readInput

class Day6 {

    fun solve() {
        val rawInput = readInput("day6.txt", AOCYear.TwentyTwo).single()

        val partOne = rawInput.asSequence().windowed(4).indexOfFirst { it.distinct().size == it.size }.plus(4)

        val partTwo = rawInput.asSequence().windowed(14).indexOfFirst { it.distinct().size == it.size }.plus(14)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
