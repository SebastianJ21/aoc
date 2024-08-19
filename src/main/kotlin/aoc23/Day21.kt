package aoc23

import AOCYear
import applyDirection
import convertInputToCharMatrix
import getOrNull
import readInput
import kotlin.math.pow

class Day21 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    data class GraphNode(
        val position: Position,
        val isWall: Boolean,
        val neighbours: List<Position>,
    )

    private val partOneSteps = 64
    private val partTwoSteps = 26501365

    fun solve() {
        val rawInput = readInput("day21.txt", AOCYear.TwentyThree)
        val matrix = convertInputToCharMatrix(rawInput)

        val start: Position = matrix.indices.firstNotNullOf { rowI ->
            matrix[rowI].indices.find { colI -> matrix[rowI][colI] == 'S' }?.let { rowI to it }
        }

        val nodes = matrix.flatMapIndexed { rowI, row ->
            row.mapIndexed { colI, value ->
                val position = rowI to colI

                val neighbours = listOf(up, down, left, right).mapNotNull { direction ->
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

        val (oddSquares, evenSquares) = maxMapsVisited.run { inc().pow(2) to pow(2) }

        val oddCornersDistances = reachableShortestDistances.count { isOdd(it) && it > 65 }
        val evenCornersDistances = reachableShortestDistances.count { !isOdd(it) && it > 65 }

        val resultWithCorners = (oddSquares * oddDistances) + (evenSquares * evenDistances)
        val cornersToRemove = (maxMapsVisited.inc() * oddCornersDistances) + (maxMapsVisited * evenCornersDistances)
        val partTwo = resultWithCorners - cornersToRemove

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private val isOdd = { num: Int -> num % 2 == 1 }

    private fun Long.pow(n: Int) = toDouble().pow(n).toLong()

    private fun shortestDistances(graph: Map<Position, GraphNode>, start: Position): MutableMap<Position, Int> {
        val posToShortestDist = graph.keys.associateWith { Int.MAX_VALUE }.toMutableMap()
        posToShortestDist[start] = 0

        val seen = mutableSetOf<Position>()
        val queue = ArrayDeque<GraphNode>()
        queue.add(graph.getValue(start))

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            val currentShortest = posToShortestDist.getValue(current.position)

            if (!seen.add(current.position)) continue

            current.neighbours
                .filter { posToShortestDist.getValue(it) > currentShortest + 1 }
                .map { graph.getValue(it) }
                .filterNot { it.isWall }
                .forEach { neighbour ->
                    posToShortestDist[neighbour.position] = currentShortest + 1
                    queue.addLast(neighbour)
                }
        }

        return posToShortestDist
    }
}
