package aoc21

import AOCYear
import Position
import applyDirection
import convertInputToMatrix
import getOrNull
import readInput
import transposed
import java.util.PriorityQueue
import kotlin.math.min

class Day15 {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    fun solve() {
        val rawInput = readInput("day15.txt", AOCYear.TwentyOne)
        val matrix = convertInputToMatrix(rawInput) { value -> value.digitToInt() }

        val startPosition = 0 to 0
        val targetPartOne = matrix.lastIndex to matrix.first().lastIndex

        val bufferedMatrix = matrix.bufferedWithItself(4)

        val targetPartTwo = bufferedMatrix.lastIndex to bufferedMatrix.first().lastIndex

        val partOne = dijkstra(matrix, startPosition)[targetPartOne]!!
        val partTwo = dijkstra(bufferedMatrix, startPosition)[targetPartTwo]!!

        println("Part one: $partOne")
        println("Part two: $partTwo")
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
