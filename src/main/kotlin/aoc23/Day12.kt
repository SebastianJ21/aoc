package aoc23

import AOCYear
import mapToInt
import readInput

class Day12 {

    private val cache = mutableMapOf<Pair<String, List<Int>>, Long>()

    private fun count(springsConfig: String, nums: List<Int>): Long {
        when {
            springsConfig.isEmpty() -> return if (nums.isEmpty()) 1 else 0
            nums.isEmpty() -> return if ('#' in springsConfig) 0 else 1
        }

        cache[springsConfig to nums]?.let { return it }

        var result = 0L

        if (springsConfig.first() in ".?") {
            result += count(springsConfig.drop(1), nums)
        }

        if (springsConfig.first() in "#?") {
            val firstNum = nums.first()

            result += when {
                firstNum > springsConfig.length -> 0L
                '.' in springsConfig.take(firstNum) -> 0L
                firstNum < springsConfig.length && springsConfig[firstNum] == '#' -> 0L
                else -> count(springsConfig.drop(firstNum + 1), nums.drop(1))
            }
        }

        cache[springsConfig to nums] = result
        return result
    }

    fun solve() {
        val rawInput = readInput("day12.txt", AOCYear.TwentyThree)

        val springConfigWithNums = rawInput.map { line ->
            val (springsConfig, numsStr) = line.split(" ")
            springsConfig to numsStr.split(",").mapToInt()
        }

        val partOne = springConfigWithNums.sumOf { (springsConfig, nums) ->
            count(springsConfig, nums)
        }

        val partTwo = springConfigWithNums.sumOf { (springsConfig, nums) ->
            val unfoldedSprings = (1..5).joinToString("?") { springsConfig }
            val unfoldedNums = (1..5).flatMap { nums }

            count(unfoldedSprings, unfoldedNums)
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
