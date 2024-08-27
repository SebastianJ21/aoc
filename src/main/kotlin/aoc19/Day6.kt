@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import invertListMap
import printAOCAnswers
import readInput
import java.util.PriorityQueue
import kotlin.math.min

class Day6 {

    fun solve() {
        val rawInput = readInput("day6.txt", AOCYear.Nineteen)

        val planetToOrbiters = rawInput.map { line ->
            val (orbited, orbiter) = line.split(")")

            orbited to orbiter
        }.groupBy({ it.first }, { it.second })

        fun countPredecessors(planet: String): Int {
            val orbiters = planetToOrbiters[planet]

            return orbiters?.sumOf { 1 + countPredecessors(it) } ?: 0
        }

        val partOne = planetToOrbiters.keys.sumOf { countPredecessors(it) }

        val inverted = invertListMap(planetToOrbiters)
        val bidirectional = inverted.mapValues { (key, values) -> values + (planetToOrbiters[key] ?: emptyList()) }

        val shortestPaths = shortestPaths(bidirectional, "YOU")

        val partTwo = shortestPaths.getValue("SAN") - 2

        printAOCAnswers(partOne, partTwo)
    }

    fun shortestPaths(graph: Map<String, List<String>>, start: String): Map<String, Int> {
        val visited = mutableSetOf<String>()

        val queue = PriorityQueue<Pair<Int, String>>(compareBy { it.first })
        queue.add(0 to start)

        val resultMap: Map<String, Int> = buildMap {
            put(start, 0)

            while (queue.isNotEmpty()) {
                val (currentDistance, node) = queue.remove()
                val neighbors = graph[node]

                if (!visited.add(node) || neighbors == null) continue

                val positionsToCheck = neighbors.filter { neighbor -> neighbor !in visited }

                positionsToCheck.forEach { nodeToCheck ->
                    val minDistance = min(this[nodeToCheck] ?: Int.MAX_VALUE, currentDistance + 1)

                    this[nodeToCheck] = minDistance

                    queue.add(minDistance to nodeToCheck)
                }
            }
        }

        return resultMap
    }
}
