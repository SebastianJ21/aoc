package aoc21

import AOCYear
import mapToInt
import readInput

class Day1 {

    fun solve() {
        val input = readInput("day1.txt", AOCYear.TwentyOne).mapToInt()

        val partOne = input.zipWithNext().count { (a, b) -> b > a }

        val partTwo = input
            .windowed(3, 1) { window -> window.sum() }
            .zipWithNext()
            .count { (a, b) -> b > a }

        println("Part One: $partOne")
        println("Part One: $partTwo")
    }
}
