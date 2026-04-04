package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Direction
import Position
import applyDirection
import at
import getOrNull
import readInput
import toMatrix
import java.util.PriorityQueue

class Day17 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    private fun turnDirection(direction: Direction, toDirection: Direction) = when (direction) {
        left -> if (toDirection == left) down else up
        right -> if (toDirection == left) up else down
        up -> if (toDirection == left) left else right
        down -> if (toDirection == left) right else left
        else -> error("")
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day17.txt", AOCYear.TwentyThree)

        val matrix = rawInput.toMatrix { value -> value.digitToInt() }
        val startPos = 0 at 0
        val endPosition = matrix.lastIndex at matrix.last().lastIndex

        val partOne = allPaths(matrix, startPos, 2, 0)
            .getValue(endPosition)
            .minOf { (_, value, _) -> value }

        val partTwo = allPaths(matrix, startPos, 9, 3)
            .getValue(endPosition)
            .minOf { (_, value, _) -> value }

        return AOCAnswer(partOne, partTwo)
    }

    private data class PathNode(val directionCount: Int, val pathCost: Int, val direction: Direction)

    private fun allPaths(
        matrix: List<List<Int>>,
        start: Position,
        maxDirCount: Int,
        minTurnCount: Int,
    ): Map<Position, List<PathNode>> {
        val turnCountRange = minTurnCount..maxDirCount

        val paths = hashMapOf<Position, List<PathNode>>()

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

            fun checkTurn(turn: Direction, directionCount: Int) {
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
