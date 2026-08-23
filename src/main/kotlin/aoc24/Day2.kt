package aoc24

import AOCAnswer
import AOCSolution
import inputLines
import kotlinx.collections.immutable.toPersistentList
import mapToInt

class Day2 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val allLevels = inputLines.map { row -> row.split(" ").mapToInt() }

        val (safe, unsafe) = allLevels.partition { levels -> levels.isSafe() }

        val partOne = safe.size

        val fixableUnsafeLevels = unsafe.count { unsafeLevels ->
            val levelsPersistent = unsafeLevels.toPersistentList()

            unsafeLevels.indices.any { index -> levelsPersistent.removingAt(index).isSafe() }
        }

        val partTwo = safe.size + fixableUnsafeLevels

        return AOCAnswer(partOne, partTwo)
    }

    private fun List<Int>.isSafe(): Boolean {
        val diffs = zipWithNext { a, b -> a - b }

        return diffs.all { it < 0 && it in -3..-1 } || diffs.all { it > 0 && it in 1..3 }
    }
}
