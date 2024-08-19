package aoc22

import AOCYear
import readInput

class Day3 {

    fun solve() {
        val rawInput = readInput("day3.txt", AOCYear.TwentyTwo)

        val charScore = { char: Char ->
            if (char.isLowerCase()) {
                char.code % 96
            } else {
                (char.code % 64) + 26
            }
        }

        val partOne = rawInput.map { line ->
            val set = line.substring(line.length / 2).toSet()

            line.first { it in set }
        }.sumOf(charScore)

        val partTwo = rawInput.chunked(3).map { group ->
            group.flatMap { it.toList().distinct() }
                .groupingBy { it }
                .eachCount()
                .entries
                .first { (_, count) -> count == 3 }
                .key
        }.sumOf(charScore)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
