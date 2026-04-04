package aoc20

import AOCAnswer
import AOCSolution
import AOCYear
import applyDirection
import at
import product
import readInput
import toCharMatrix

class Day3 : AOCSolution {

    private val slopes = listOf(1 at 3, 1 at 1, 1 at 5, 1 at 7, 2 at 1)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day3.txt", AOCYear.Twenty)

        val matrix = rawInput.toCharMatrix()

        val movementSequences = slopes.map { slope ->
            generateSequence(0 at 0) { position -> position.applyDirection(slope) }
                .takeWhile { (rowI) -> rowI <= matrix.lastIndex }
        }

        val colSize = matrix.first().size

        val partOne = movementSequences.first().count { (rowI, colI) ->
            matrix[rowI][colI % colSize] == '#'
        }

        val partTwo = movementSequences.map { movementSequence ->
            movementSequence.count { (rowI, colI) -> matrix[rowI][colI % colSize] == '#' }.toLong()
        }.product()

        return AOCAnswer(partOne, partTwo)
    }
}
