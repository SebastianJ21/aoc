package aoc25

import AOCAnswer
import AOCSolution
import readInput

class Day3 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.TwentyFive)

        val batteries = rawInput.map { line -> line.map { it.digitToInt().toLong() } }

        val partOne = batteries.sumOf { battery -> turnOnScore(battery, 2) }
        val partTwo = batteries.sumOf { battery -> turnOnScore(battery, 12) }

        return AOCAnswer(partOne, partTwo)
    }

    fun turnOnScore(battery: List<Long>, count: Int): Long {
        fun selectNext(values: List<IndexedValue<Long>>, n: Int): List<Long> {
            if (n == 0) return emptyList()

            // Pick first a value that leaves enough entries to match the needed count
            val (newIndex, newValue) = values.first { (selectedIndex) ->
                val leftAfterRemove = values.count { it.index > selectedIndex }

                leftAfterRemove >= n - 1
            }

            val nextIndexToValue = values.filter { (index) -> index > newIndex }

            return listOf(newValue) + selectNext(nextIndexToValue, n - 1)
        }

        val sorted = battery.withIndex().sortedByDescending { (_, value) -> value }
        val selected = selectNext(sorted, count)

        return selected.joinToString("").toLong()
    }
}
