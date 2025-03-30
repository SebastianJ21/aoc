package aoc23

import AOCYear
import readInput
import toCharMatrix
import transposed
import kotlin.math.max
import kotlin.math.min

class Day11 {

    fun solve() {
        val input = readInput("day11.txt", AOCYear.TwentyThree)
        val transposedMatrix = input.toCharMatrix().transposed()

        val emptyRows = input.mapIndexedNotNull { rowI, row -> rowI.takeIf { row.all { it == '.' } } }
        val emptyCols = transposedMatrix.mapIndexedNotNull { rowI, row -> rowI.takeIf { row.all { it == '.' } } }

        val galaxies = input.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, char ->
                if (char == '#') rowI to colI else null
            }
        }

        val galaxyPairs = galaxies.flatMapIndexed { galaxyI, galaxyPos ->
            galaxies
                .slice(galaxyI + 1 until galaxies.size)
                .map { galaxyPos to it }
        }

        fun sumOfShortestDistances(expansionWeight: Long = 2L) = galaxyPairs.sumOf { (galaxyA, galaxyB) ->
            val (rowA, colA) = galaxyA
            val (rowB, colB) = galaxyB
            val rows = min(rowA, rowB)..max(rowA, rowB)
            val cols = min(colA, colB)..max(colA, colB)

            val expansionCount = emptyRows.count { rows.contains(it) } + emptyCols.count { cols.contains(it) }

            val expansionOffset = expansionCount * (expansionWeight - 1)
            val shortestPath = rows.last - rows.first + cols.last - cols.first

            expansionOffset + shortestPath
        }

        val partOne = sumOfShortestDistances(expansionWeight = 2)
        val partTwo = sumOfShortestDistances(expansionWeight = 1000000)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
