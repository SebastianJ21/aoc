package aoc23

import AOCYear
import applyDirection
import convertInputToArrayMatrix
import getOrNull
import readInput
import java.util.PriorityQueue

private typealias Matrix = Array<Array<Int>>

class Day17 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    fun turnDirection(direction: Pair<Int, Int>, toDirection: Pair<Int, Int>) = when (direction) {
        left -> if (toDirection == left) down else up
        right -> if (toDirection == left) up else down
        up -> if (toDirection == left) left else right
        down -> if (toDirection == left) right else left
        else -> error("")
    }

    fun solve() {
        val rawInput = readInput("day17.txt", AOCYear.TwentyThree)

        val matrix = convertInputToArrayMatrix(rawInput) { digitToInt() }
        val startPos = 0 to 0
        val endPos = matrix.size - 1 to matrix.last().size - 1

        val partOne =
            allPaths(matrix, startPos, 2, 0)
                .getValue(endPos)
                .minOf { (_, value, _) -> value }

        val partTwo =
            allPaths(matrix, startPos, 9, 3)
                .getValue(endPos)
                .minOf { (_, value, _) -> value }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    data class PathNode(val directionCount: Int, val pathCost: Int, val direction: Pair<Int, Int>)

    private fun allPaths(
        matrix: Matrix,
        start: Position,
        maxDirCount: Int,
        minTurnCount: Int,
    ): Map<Position, List<PathNode>> {
        val paths = matrix
            .flatMapIndexed { rowI, row ->
                row.mapIndexed { colI, _ -> (rowI to colI) to listOf<PathNode>() }
            }
            .toMap()
            .toMutableMap()

        val compareFunc = { a: Pair<Position, PathNode>, b: Pair<Position, PathNode> ->
            a.second.pathCost.compareTo(
                b.second.pathCost,
            )
        }
        val queue = PriorityQueue(compareFunc)
        queue.add(start to PathNode(0, 0, right))
        queue.add(start to PathNode(0, 0, down))

        while (queue.isNotEmpty()) {
            val (currentPosition, pathNode) = queue.poll()
            val (sameDirectionCount, current, directionVector) = pathNode

            val (leftDirVec, rightDirVec) = listOf(left, right).map { turnDirection(directionVector, it) }

            val (leftPos, rightPos, continuePos) =
                listOf(leftDirVec, rightDirVec, directionVector).map { currentPosition.applyDirection(it) }

            val (left, right, continued) =
                with(matrix) {
                    listOf(
                        getOrNull(leftPos),
                        getOrNull(rightPos),
                        getOrNull(continuePos),
                    )
                }

            fun Int.checkDirection(position: Position, direction: Pair<Int, Int>, directionCount: Int) {
                val pathCost = current + this
                val currentPaths = paths.getValue(position)

                val isBestOption =
                    currentPaths.none { (dirCount, value, dir) ->
                        dirCount == directionCount && value <= pathCost && dir == direction
                    }

                if (!isBestOption) return

                val newPaths = currentPaths + PathNode(directionCount, pathCost, direction)

                paths[position] = newPaths
                queue.offer(position to PathNode(directionCount, pathCost, direction))
            }

            left?.takeIf { sameDirectionCount in minTurnCount..maxDirCount }
                ?.checkDirection(leftPos, leftDirVec, 0)

            right?.takeIf { sameDirectionCount in minTurnCount..maxDirCount }
                ?.checkDirection(rightPos, rightDirVec, 0)

            continued?.takeIf { sameDirectionCount < maxDirCount }
                ?.checkDirection(continuePos, directionVector, sameDirectionCount + 1)
        }

        return paths
    }
}
