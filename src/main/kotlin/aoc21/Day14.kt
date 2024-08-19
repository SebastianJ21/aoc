package aoc21

import readInput

private typealias CharPair = Pair<Char, Char>

class Day14 {

    fun solve() {
        val rawInput = readInput("day14.txt", AOCYear.TwentyOne)
        val polymer: List<CharPair> = rawInput.first().zipWithNext()

        val pairToInsertion = rawInput.drop(2).associate {
            val (from, to) = it.split(" -> ")

            (from[0] to from[1]) to to.single()
        }

        fun answerBySteps(steps: Int) = polymer.map { charCountsAfterSteps(it, pairToInsertion, steps) }
            .plus(mapOf(polymer.last().second to 1L))
            .mergeMaps()
            .values
            .sorted()
            .run { last() - first() }

        val partOne = answerBySteps(10)
        val partTwo = answerBySteps(40)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun charCountsAfterSteps(
        polymerPair: CharPair,
        pairToInsertion: Map<CharPair, Char>,
        steps: Int,
    ): Map<Char, Long> {
        val cache = hashMapOf<CharPair, MutableMap<Int, Map<Char, Long>>>()

        fun CharPair.performCount(stepsLeft: Int): Map<Char, Long> {
            val pairsCache = cache.getOrPut(this) { mutableMapOf() }

            when (stepsLeft) {
                in pairsCache -> return pairsCache[stepsLeft]!!
                0 -> return mapOf(first to 1L)
            }

            val result = makeInsertion(pairToInsertion)
                .map { it.performCount(stepsLeft - 1) }
                .mergeMaps()

            pairsCache[stepsLeft] = result

            return result
        }

        val result = polymerPair.performCount(steps)

        return result
    }

    private fun CharPair.makeInsertion(pairToInsertion: Map<CharPair, Char>): List<CharPair> {
        val insertion = pairToInsertion[this]

        return if (insertion != null) {
            listOf(first to insertion, insertion to second)
        } else {
            listOf(this)
        }
    }

    private fun List<Map<Char, Long>>.mergeMaps() =
        flatMap { it.keys }.distinct().associateWith { key -> sumOf { it[key] ?: 0 } }
}
