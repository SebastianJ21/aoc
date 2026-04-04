package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import at
import positionsOf
import product
import readInput
import splitBy

class Day3 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.TwentyThree)

        val symbolsPositions = rawInput.positionsOf { it != '.' && !it.isDigit() }.toSet()

        fun adjacentSymbols(position: Position): List<Position> {
            val (row, col) = position

            val positionsToCheck = listOf(
                row + 1 at col,
                row + 1 at col - 1,
                row + 1 at col + 1,
                row - 1 at col,
                row - 1 at col - 1,
                row - 1 at col + 1,
                row at col - 1,
                row at col + 1,
            )
            return positionsToCheck.filter { it in symbolsPositions }
        }

        val partNumbers = rawInput.flatMapIndexed { rowI, row ->
            val numberPartsGroups = row.mapIndexed { index, char -> index to char }
                .splitBy { !it.second.isDigit() }
                .filter { it.isNotEmpty() }

            val numsWithAdjacentPositions = numberPartsGroups.map { numberParts ->
                val number = numberParts.fold(0) { acc, (_, char) -> acc * 10 + char.digitToInt() }
                val adjacent = numberParts.flatMap { (colI, _) -> adjacentSymbols(rowI at colI) }.toSet()

                number to adjacent
            }

            numsWithAdjacentPositions.filter { (_, adjacentSymbols) -> adjacentSymbols.isNotEmpty() }
        }

        val partOne = partNumbers.sumOf { (number) -> number }

        val partTwo = partNumbers
            // Invert list map
            .flatMap { (number, adjacentSymbols) -> adjacentSymbols.map { position -> position to number } }
            .groupBy({ it.first }, { it.second })
            .entries
            .sumOf { (symbolPosition, adjacentNumbers) ->
                val symbol = rawInput[symbolPosition.first][symbolPosition.second]

                if (adjacentNumbers.size == 2 && symbol == '*') adjacentNumbers.product() else 0
            }

        return AOCAnswer(partOne, partTwo)
    }
}
