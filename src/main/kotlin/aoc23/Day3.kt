package aoc23

import AOCYear
import Position
import product
import readInput
import splitBy

class Day3 {
    fun solve() {
        val rawInput = readInput("day3.txt", AOCYear.TwentyThree)

        val symbolsPositions = rawInput.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, char ->
                when {
                    char == '.' -> null
                    char.isDigit() -> null
                    else -> rowI to colI
                }
            }
        }.toSet()

        fun adjacentSymbols(position: Position): List<Pair<Int, Int>> {
            val (row, col) = position

            val positionsToCheck = listOf(
                row + 1 to col,
                row + 1 to col - 1,
                row + 1 to col + 1,
                row - 1 to col,
                row - 1 to col - 1,
                row - 1 to col + 1,
                row to col - 1,
                row to col + 1,
            )
            return positionsToCheck.filter { it in symbolsPositions }
        }

        val partNumbers = rawInput.flatMapIndexed { rowI, row ->
            val numberPartsGroups = row.mapIndexed { index, char -> index to char }
                .splitBy { !it.second.isDigit() }
                .filter { it.isNotEmpty() }

            val numsWithAdjacentPositions = numberPartsGroups.map { numberParts ->
                val number = numberParts.fold("") { acc, (_, char) -> acc + char }.toInt()
                val adjacent = numberParts.flatMap { (colI, _) -> adjacentSymbols(rowI to colI) }.toSet()

                number to adjacent
            }

            numsWithAdjacentPositions.filter { (_, adjacentSymbols) -> adjacentSymbols.isNotEmpty() }
        }

        val partOne = partNumbers.sumOf { it.first }

        val partTwo = partNumbers
            // Invert list map
            .flatMap { (number, adjacentSymbols) -> adjacentSymbols.map { position -> position to number } }
            .groupBy({ it.first }, { it.second })
            .entries
            .sumOf { (symbolPosition, adjacentNumbers) ->
                val symbol = rawInput[symbolPosition.first][symbolPosition.second]

                if (adjacentNumbers.size == 2 && symbol == '*') adjacentNumbers.product() else 0
            }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
