package aoc22

import AOCYear
import mapToInt
import readInput

class Day4 {

    fun solve() {
        val rawInput = readInput("day4.txt", AOCYear.TwentyTwo)

        fun mapRange(rawRange: String) = rawRange.split("-").mapToInt().let { (from, to) -> from..to }

        val ranges = rawInput.map { line ->
            line.split(",").let { (range1, range2) ->
                mapRange(range1) to mapRange(range2)
            }
        }

        operator fun IntRange.contains(other: IntRange) = first in other && last in other

        val partOne = ranges.count { (range1, range2) ->
            range1 in range2 || range2 in range1
        }

        fun IntRange.overlaps(other: IntRange) = first in other || last in other

        val partTwo = ranges.count { (range1, range2) ->
            range1.overlaps(range2) || range2.overlaps(range1)
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
