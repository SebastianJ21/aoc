package aoc20

import AOCYear
import applyDirection
import convertInputToCharMatrix
import product
import readInput

class Day3 {

    private val slopes = listOf(1 to 3, 1 to 1, 1 to 5, 1 to 7, 2 to 1)

    fun solve() {
        val rawInput = readInput("day3.txt", AOCYear.Twenty)

        val matrix = convertInputToCharMatrix(rawInput)

        val movementSequences = slopes.map { slope ->
            generateSequence(0 to 0) { position ->
                position.applyDirection(slope).takeIf { (newRowI, _) -> newRowI <= matrix.lastIndex }
            }
        }

        val colSize = matrix.first().size

        val partOne = movementSequences.first().count { (rowI, colI) ->
            matrix[rowI][colI % colSize ] == '#'
        }

        val partTwo = movementSequences.map { movementSequence ->
            movementSequence.count { (rowI, colI) ->
                matrix[rowI][colI % colSize ] == '#'
            }.toLong()
        }.product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
