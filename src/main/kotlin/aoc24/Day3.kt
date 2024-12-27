package aoc24

import AOCAnswer
import AOCSolution
import AOCYear
import mapToInt
import readInput

class Day3 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.TwentyFour)

        val regex = Regex("(mul\\(\\d*,\\d*\\))")
        val input = rawInput.joinToString("")

        val matchResults = regex.findAll(input).toList()

        val startIndexToValuePairs = matchResults.map { matchResult ->
            val value = matchResult.value
                .replace(Regex("[mul()]"), "")
                .split(",")
                .mapToInt()
                .reduce(Int::times)

            matchResult.range.first to value
        }

        val partOne = startIndexToValuePairs.sumOf { (_, value) -> value }

        val conditionRegex = Regex("(do\\(\\))|(don't\\(\\))")
        val indexToIsSkippedPairs = conditionRegex
            .findAll(input)
            .fold(listOf(0 to false)) { acc, matchResult ->
                val isSkip = matchResult.value == "don't()"
                val index = matchResult.range.last

                // Skip 'duplicate' isSkip values as they are a subset of their preceding interval
                if (acc.lastOrNull()?.second == isSkip) acc else acc + Pair(index, isSkip)
            }
            .toList()

        val lastRange = indexToIsSkippedPairs.last().let { (index, skip) -> (index..input.lastIndex) to skip }

        val indexRangeSkipPairs = indexToIsSkippedPairs
            .zipWithNext { (aIndex, skipA), (bIndex) -> (aIndex until bIndex) to skipA }
            .plusElement(lastRange)

        val partTwo = startIndexToValuePairs.sumOf { (startIndex, value) ->
            val index = indexRangeSkipPairs.binarySearch { (range) ->
                when {
                    startIndex in range -> 0
                    startIndex > range.last -> -1
                    else -> 1
                }
            }
            val isSkipped = indexToIsSkippedPairs[index].second

            if (isSkipped) 0 else value
        }

        return AOCAnswer(partOne, partTwo)
    }
}
