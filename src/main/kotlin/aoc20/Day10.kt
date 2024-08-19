package aoc20

import AOCYear
import mapToInt
import readInput

class Day10 {

    fun solve() {
        val rawInput = readInput("day10.txt", AOCYear.Twenty)

        val adapters = rawInput.mapToInt().run {
            // Extra adapter
            this.plus(max() + 3)
                // Charging outlet
                .plus(0)
        }

        val adapterSequence = adapters.sorted()

        val differences = adapterSequence.zipWithNext { a, b -> b - a }

        val partOne = differences.count { it == 1 } * differences.count { it == 3 }

        val nodeToPossiblePaths = adapterSequence.drop(1).fold(mapOf(adapterSequence.first() to 1L)) { paths, adapter ->
            val adapterPaths = (1..3).sumOf { paths[adapter - it] ?: 0L }

            paths + (adapter to adapterPaths)
        }

        val partTwo = nodeToPossiblePaths.getValue(adapterSequence.last())

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
