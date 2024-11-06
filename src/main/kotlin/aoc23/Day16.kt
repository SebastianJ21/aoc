package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import convertInputToCharMatrix
import getOrNull
import readInput

class Day16 : AOCSolution {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    data class Beam(val position: Position, val direction: Direction)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day16.txt", AOCYear.TwentyThree)
        val matrix = convertInputToCharMatrix(rawInput)

        val (rows, cols) = matrix.indices to matrix[0].indices
        val (leftSide, rightSide) = with(cols) { map { Beam(-1 to it, down) } to map { Beam((last + 1) to it, up) } }
        val (topSide, bottomSide) = with(rows) { map { Beam(it to -1, right) } to map { Beam(it to (last + 1), left) } }

        val startingBeamsPartTwo = leftSide + rightSide + topSide + bottomSide

        fun getEnergizedCount(startingBeam: Beam): Int {
            val seen = hashSetOf<Beam>()

            val beamSequence = generateSequence(listOf(startingBeam)) { currentBeams ->
                val newBeams = currentBeams
                    .flatMap { getNextBeams(it, matrix) }
                    .filter { seen.add(it) }

                newBeams
            }

            return beamSequence.takeWhile { beams -> beams.isNotEmpty() }.flatten().map { it.position }.toSet().size
        }

        val partOne = getEnergizedCount(Beam(Position(0, -1), right))
        val partTwo = startingBeamsPartTwo.maxOf { getEnergizedCount(it) }

        return AOCAnswer(partOne, partTwo)
    }

    fun getNextBeams(beam: Beam, matrix: List<List<Char>>): List<Beam> {
        val direction = beam.direction
        val nextPosition = beam.position.applyDirection(direction)

        return when (matrix.getOrNull(nextPosition)) {
            '.' -> listOf(Beam(nextPosition, direction))
            '-' -> {
                if (direction == left || direction == right) {
                    listOf(Beam(nextPosition, direction))
                } else {
                    listOf(Beam(nextPosition, left), Beam(nextPosition, right))
                }
            }
            '|' -> {
                if (direction == up || direction == down) {
                    listOf(Beam(nextPosition, direction))
                } else {
                    listOf(Beam(nextPosition, up), Beam(nextPosition, down))
                }
            }
            '/' -> {
                val nextDirection = when (direction) {
                    up -> right
                    down -> left
                    left -> down
                    right -> up
                    else -> error("Invalid direction")
                }

                listOf(Beam(nextPosition, nextDirection))
            }

            '\\' -> {
                val nextDirection = when (direction) {
                    up -> left
                    down -> right
                    left -> up
                    right -> down
                    else -> error("Invalid direction")
                }

                listOf(Beam(nextPosition, nextDirection))
            }
            null -> emptyList()
            else -> error("Invalid matrix element")
        }
    }
}
