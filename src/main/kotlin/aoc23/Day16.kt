package aoc23

import AOCYear
import applyDirection
import convertInputToCharArrayMatrix
import getOrNull
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import readInput
import java.util.concurrent.Executors

typealias Position = Pair<Int, Int>

class Day16 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    data class Beam(val position: Position, val direction: Pair<Int, Int>)

    fun solve() {
        val rawInput = readInput("day16.txt", AOCYear.TwentyThree)
        val matrix = convertInputToCharArrayMatrix(rawInput)

        val (rows, cols) = matrix.indices to matrix[0].indices
        val (leftSide, rightSide) = with(cols) { map { Beam(-1 to it, down) } to map { Beam((last + 1) to it, up) } }
        val (topSide, bottomSide) = with(rows) { map { Beam(it to -1, right) } to map { Beam(it to (last + 1), left) } }

        val startingBeamsPartTwo = leftSide + rightSide + topSide + bottomSide

        fun getEnergizedCount(startingBeam: Beam) = buildSet {
            generateSequence(listOf(startingBeam)) { currentBeams ->
                currentBeams.flatMap { getNextBeams(it, matrix) }
                    .filter { add(it) }
                    .ifEmpty { null }
            }.toList()
        }.distinctBy { it.position }.size

        val partOne = getEnergizedCount(Beam(Position(0, -1), right))

        val virtualThreadDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val partTwo = runBlocking(virtualThreadDispatcher) {
            startingBeamsPartTwo
                .map { async { getEnergizedCount(it) } }
                .awaitAll()
                .max()
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun getNextBeams(beam: Beam, matrix: Array<Array<Char>>): List<Beam> {
        val currentDir = beam.direction
        val nextPosition = beam.position.applyDirection(currentDir)

        val newDirections = when (matrix.getOrNull(nextPosition)) {
            '.' -> listOf(currentDir)
            '-' -> if (currentDir in listOf(left, right)) listOf(currentDir) else listOf(left, right)
            '|' -> if (currentDir in listOf(up, down)) listOf(currentDir) else listOf(up, down)
            '/' -> when (currentDir) {
                up -> right
                down -> left
                left -> down
                right -> up
                else -> error("Illegal direction")
            }.let { listOf(it) }

            '\\' -> when (currentDir) {
                up -> left
                down -> right
                left -> up
                right -> down
                else -> error("Illegal direction")
            }.let { listOf(it) }

            null -> emptyList()
            else -> error("Illegal char")
        }
        return newDirections.map { Beam(nextPosition, it) }
    }
}
