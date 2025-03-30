package aoc23

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import get
import getOrNull
import positionsSequence
import readInput
import toCharMatrix
import kotlin.math.pow

class Day21 : AOCSolution {

    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    data class GraphNode(val position: Position, val isWall: Boolean, val neighbours: List<Position>)

    private val partOneSteps = 64
    private val partTwoSteps = 26501365

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day21.txt", AOCYear.TwentyThree)
        val matrix = rawInput.toCharMatrix()

        val start: Position = matrix.positionsSequence().first { matrix[it] == 'S' }

        val nodes = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexed { colI, value ->
                val position = rowI to colI

                val neighbours = directions.mapNotNull { direction ->
                    position.applyDirection(direction).takeIf { matrix.getOrNull(it) != null }
                }

                GraphNode(position, value == '#', neighbours)
            }
        }

        val positionToNode = nodes.associateBy { it.position }

        val shortestDistances = shortestDistances(positionToNode, start)
        val reachableShortestDistances = shortestDistances.values.filter { it != Int.MAX_VALUE }

        val partOne = reachableShortestDistances.count { !isOdd(it) && it <= partOneSteps }

        val (startX, startY) = start

        val stepsToEdge = matrix.size - startX - 1

        check(stepsToEdge == matrix[0].size - startY - 1) {
            "Assumption that the starting position is in the middle was not met"
        }

        val maxMapsVisited = (partTwoSteps - stepsToEdge) / matrix.size.toLong()

        val oddDistances = reachableShortestDistances.count(isOdd)
        val evenDistances = reachableShortestDistances.size - oddDistances

        val (oddSquares, evenSquares) = maxMapsVisited.let { it.inc().pow(2) to it.pow(2) }

        val oddCornersDistances = reachableShortestDistances.count { isOdd(it) && it > 65 }
        val evenCornersDistances = reachableShortestDistances.count { !isOdd(it) && it > 65 }

        val resultWithCorners = (oddSquares * oddDistances) + (evenSquares * evenDistances)
        val cornersToRemove = (maxMapsVisited.inc() * oddCornersDistances) + (maxMapsVisited * evenCornersDistances)
        val partTwo = resultWithCorners - cornersToRemove

        return AOCAnswer(partOne, partTwo)
    }

    private val isOdd = { num: Int -> num % 2 == 1 }

    private fun Long.pow(n: Int) = toDouble().pow(n).toLong()

    private fun shortestDistances(graph: Map<Position, GraphNode>, start: Position): Map<Position, Int> {
        val positionToShortestDist = graph.keys.associateWith { Int.MAX_VALUE }.toMutableMap()
        positionToShortestDist[start] = 0

        val seen = mutableSetOf<Position>()
        val queue = ArrayDeque<GraphNode>()
        queue.add(graph.getValue(start))

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            val currentShortest = positionToShortestDist.getValue(current.position)

            if (!seen.add(current.position)) continue

            current.neighbours
                .filter { positionToShortestDist.getValue(it) > currentShortest + 1 }
                .map { graph.getValue(it) }
                .filterNot { it.isWall }
                .forEach { neighbour ->
                    positionToShortestDist[neighbour.position] = currentShortest + 1
                    queue.addLast(neighbour)
                }
        }

        return positionToShortestDist
    }
}
