package aoc20

import AOCYear
import mapToLong
import readInput

class Day9 {

    fun solve() {
        val rawInput = readInput("day9.txt", AOCYear.Twenty)

        val numbers = rawInput.mapToLong()

        val partOne = numbers.asSequence()
            .windowed(26, 1)
            .first { window -> !twoSum(window.dropLast(1), window.last()) }
            .last()

        val prefixSums = numbers.runningReduce { acc, num -> acc + num }

        val (fromIndex, toIndex) = prefixSums.withIndex().firstNotNullOf { (toIndex, sum) ->
            if (sum < partOne) return@firstNotNullOf null

            val fromIndex = prefixSums.take(toIndex).indexOfFirst { sum - it == partOne }

            if (fromIndex == -1) null else fromIndex to toIndex
        }

        val partTwo = numbers.subList(fromIndex + 1, toIndex + 1).run { min() + max() }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun twoSum(numbers: List<Long>, target: Long): Boolean {
        buildSet {
            return numbers.any { num ->
                (target - num in this).also { add(num) }
            }
        }
    }
}
