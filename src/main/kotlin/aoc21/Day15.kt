package aoc21

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import at
import getOrNull
import readInput
import toMatrix
import transposed
import java.util.PriorityQueue
import kotlin.math.min

class Day15 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private val directions = listOf(up, down, left, right)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day15.txt", AOCYear.TwentyOne)
        val matrix = rawInput.toMatrix { value -> value.digitToInt() }

        val startPosition = 0 at 0
        val targetPartOne = matrix.lastIndex at matrix.first().lastIndex

        val bufferedMatrix = matrix.bufferedWithItself(4)

        val targetPartTwo = bufferedMatrix.lastIndex at bufferedMatrix.first().lastIndex

        val partOne = dijkstra(matrix, startPosition)[targetPartOne]!!
        val partTwo = dijkstra(bufferedMatrix, startPosition)[targetPartTwo]!!

        return AOCAnswer(partOne, partTwo)
    }

    private fun dijkstra(matrix: List<List<Int>>, startPosition: Position): Map<Position, Int> {
        val visited = hashSetOf<Position>()

        val queue = PriorityQueue<Pair<Int, Position>>(compareBy { it.first })
        queue.add(0 to startPosition)

        val resultMap: Map<Position, Int> = buildMap {
            put(startPosition, 0)

            while (queue.isNotEmpty()) {
                val (currentDistance, position) = queue.remove()

                if (!visited.add(position)) continue

                val positionsToCheck = directions.mapNotNull { direction ->
                    val positionToCheck = position.applyDirection(direction)

                    matrix.getOrNull(positionToCheck)?.let { positionToCheck to it }
                }.filter { (positionToCheck, _) -> positionToCheck !in visited }

                positionsToCheck.forEach { (positionToCheck, value) ->
                    val minDistance = min(this[positionToCheck] ?: Int.MAX_VALUE, currentDistance + value)

                    this[positionToCheck] = minDistance

                    queue.add(minDistance to positionToCheck)
                }
            }
        }

        return resultMap
    }

    private fun List<List<Int>>.bufferedWithItself(bufferTimes: Int): List<List<Int>> {
        fun performBuffer(originMatrix: List<List<Int>>, n: Int): List<List<Int>> {
            val matrices = (1..n).fold(listOf(originMatrix)) { matrixAcc, _ ->
                val lastMatrix = matrixAcc.last()

                matrixAcc.plusElement(lastMatrix.map { row -> row.map { (it % 9).inc() } })
            }

            val composedMatrices = matrices.reduce { acc, newMatrix ->
                acc.zip(newMatrix).map { (oldRow, newRow) -> oldRow + newRow }
            }

            return composedMatrices
        }

        return performBuffer(performBuffer(this, bufferTimes).transposed(), bufferTimes).transposed()
    }
}
