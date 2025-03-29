package aoc24

import AOCAnswer
import AOCSolution
import AOCYear
import mapToLong
import readInput

class Day11 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day11.txt", AOCYear.TwentyFour)
        val initialStones = rawInput.single().split(" ").mapToLong()

        val stoneToResolver = buildResolvers(initialStones)

        val partOne = initialStones.sumOf { stoneToResolver.getValue(it)(25) }
        val partTwo = initialStones.sumOf { stoneToResolver.getValue(it)(75) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun buildResolvers(initialStones: List<Long>): Map<Long, (steps: Int) -> Long> {
        val stoneToResolver = mutableMapOf<Long, (steps: Int) -> Long>()
        val cache = hashMapOf<Pair<Long, Int>, Long>()
        val seen = hashSetOf<Long>()

        fun resolve(stone: Long) {
            val evolvedStone = evolveStone(stone)
            evolvedStone.filter { seen.add(it) }.forEach { resolve(it) }

            val resolver = { steps: Int ->
                val cacheKey = stone to steps

                when {
                    cacheKey in cache -> cache.getValue(cacheKey)
                    steps == 1 -> evolvedStone.size.toLong()
                    else -> {
                        val result = evolvedStone.sumOf { stone ->
                            stoneToResolver.getValue(stone).invoke(steps - 1)
                        }

                        cache[cacheKey] = result

                        result
                    }
                }
            }

            stoneToResolver[stone] = resolver
        }

        initialStones.forEach { resolve(it) }

        return stoneToResolver
    }

    private fun evolveStone(value: Long): List<Long> {
        if (value == 0L) return listOf(1)

        val strValue = value.toString()

        return if (strValue.length % 2 == 0) {
            val splitIndex = strValue.length / 2

            val left = strValue.take(splitIndex).toLong()
            val right = strValue.drop(splitIndex).toLong()

            listOf(left, right)
        } else {
            listOf(value * 2024)
        }
    }
}
