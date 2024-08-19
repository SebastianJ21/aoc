package aoc23

import readInput

class Day1 {
    private val wordToDigitPairs = listOf(
        "one" to '1',
        "two" to '2',
        "three" to '3',
        "four" to '4',
        "five" to '5',
        "six" to '6',
        "seven" to '7',
        "eight" to '8',
        "nine" to '9',
    )

    fun solve() {
        val rawInput = readInput("day1.txt", AOCYear.TwentyThree)

        val partOne = rawInput.sumOf { line ->
            line.filter { it.isDigit() }
                .run { "" + first() + last() }
                .toInt()
        }

        val partTwo = rawInput.sumOf { line ->
            val allValuesWithDigit = wordToDigitPairs.flatMap { (word, digit) ->
                listOf(word to digit, digit.toString() to digit)
            }

            val indicesWithDigit = allValuesWithDigit.map { (word, digit) ->
                val firstIndex = line.indexOf(word)
                val lastIndex = line.lastIndexOf(word)

                Triple(firstIndex, lastIndex, digit)
            }

            val initialBest = Pair(Int.MAX_VALUE, '-') to Pair(Int.MIN_VALUE, '-')
            val (bestFirst, bestLast) = indicesWithDigit.fold(initialBest) { (bestFirst, bestLast), (min, max, value) ->
                val newBestMin = if (min != -1 && bestFirst.first > min) min to value else bestFirst
                val newBestMax = if (bestLast.first < max) max to value else bestLast

                newBestMin to newBestMax
            }

            ("" + bestFirst.second + bestLast.second).toInt()
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
