package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import getOrNull
import readInput
import toMatrix
import java.util.PriorityQueue

class Day17 : AOCSolution {
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

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day17.txt", AOCYear.TwentyThree)

        val matrix = rawInput.toMatrix { value -> value.digitToInt() }
        val startPos = 0 to 0
        val endPosition = matrix.lastIndex to matrix.last().lastIndex

        val partOne = allPaths(matrix, startPos, 2, 0)
            .getValue(endPosition)
            .minOf { (_, value, _) -> value }

        val partTwo = allPaths(matrix, startPos, 9, 3)
            .getValue(endPosition)
            .minOf { (_, value, _) -> value }

        return AOCAnswer(partOne, partTwo)
    }

    data class PathNode(val directionCount: Int, val pathCost: Int, val direction: Pair<Int, Int>)

    private fun allPaths(
        matrix: List<List<Int>>,
        start: Position,
        maxDirCount: Int,
        minTurnCount: Int,
    ): Map<Position, List<PathNode>> {
        val turnCountRange = minTurnCount..maxDirCount

        val paths = hashMapOf<Pair<Int, Int>, List<PathNode>>()

        val compareFunc = { (_, aNode): Pair<Position, PathNode>, (_, bNode): Pair<Position, PathNode> ->
            aNode.pathCost.compareTo(bNode.pathCost)
        }
        val queue = PriorityQueue(compareFunc)
        queue.add(start to PathNode(0, 0, right))
        queue.add(start to PathNode(0, 0, down))

        while (queue.isNotEmpty()) {
            val (currentPosition, pathNode) = queue.poll()
            val (sameDirectionCount, current, directionVector) = pathNode

            val (leftTurn, rightTurn, noTurn) =
                listOf(left, right).map { turnDirection(directionVector, it) }.plus(directionVector)

            fun checkTurn(turn: Pair<Int, Int>, directionCount: Int) {
                val position = currentPosition.applyDirection(turn)
                val value = matrix.getOrNull(position) ?: return

                val pathCost = current + value
                val currentPaths = paths[position] ?: emptyList()

                val isBestOption = currentPaths.none { node ->
                    node.directionCount == directionCount && node.pathCost <= pathCost && node.direction == turn
                }

                if (!isBestOption) return
                val newNode = PathNode(directionCount, pathCost, turn)

                val newPaths = currentPaths + newNode

                paths[position] = newPaths
                queue.offer(position to newNode)
            }

            if (sameDirectionCount in turnCountRange) {
                checkTurn(leftTurn, 0)
                checkTurn(rightTurn, 0)
            }

            if (sameDirectionCount < maxDirCount) {
                checkTurn(noTurn, sameDirectionCount + 1)
            }
        }

        return paths
    }
}
