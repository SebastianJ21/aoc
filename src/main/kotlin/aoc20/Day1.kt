package aoc20

import AOCYear
import mapToInt
import product
import readInput

class Day1 {

    fun solve() {
        val rawInput = readInput("day1.txt", AOCYear.Twenty)

        val numbers = rawInput.mapToInt()
        val target = 2020

        fun twoSum(target: Int): Pair<Int, Int>? {
            buildSet {
                val firstNum = numbers.firstOrNull { num ->
                    (target - num in this).also { add(num) }
                } ?: return null

                val secondNum = target - firstNum
                return firstNum to secondNum
            }
        }

        val partOne = twoSum(target)!!.toList().product()

        val triple = numbers.firstNotNullOf { num ->
            twoSum(target - num)?.let { (first, second) -> Triple(first, second, num) }
        }

        val partTwo = triple.toList().product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
