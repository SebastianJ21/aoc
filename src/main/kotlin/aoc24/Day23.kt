package aoc24

import AOCAnswer
import AOCSolution
import inputLines

class Day23 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        // qj-jv
        val pairs = inputLines.map { line -> line.split("-").let { (a, b) -> a to b } }
        // qj-jv + jv-qj (connections are not directional)
        val allPairs = pairs + pairs.map { (a, b) -> b to a }

        val nodeToConnections = allPairs.groupBy({ (a) -> a }, { (_, b) -> b })
            .mapValues { (_, connections) -> connections.distinct() }

        val partOne = nodeToConnections
            .filterKeys { it.startsWith("t") }
            .mapValues { (_, connections) ->
                connections.flatMapIndexed { index, first ->
                    connections.drop(index + 1).map { second -> first to second }
                }
            }
            .mapValues { (_, allPairs) ->
                allPairs.filter { (first, second) -> second in nodeToConnections[first].orEmpty() }
            }
            .flatMap { (node, pairs) -> pairs.map { (a, b) -> setOf(node, a, b) } }
            .distinct()
            .size

        val nodeClusters = nodeToConnections.map { (node, connections) ->
            val clusters = connections.flatMap { nodeToConnections.getValue(it) }
                .groupingBy { it }
                .eachCount()
                .minus(node)

            val largestClusterSize = clusters.maxOf { it.value }
            clusters.filterValues { it == largestClusterSize }.keys + node
        }

        val largestClusterSize = nodeClusters.maxOf { it.size }
        val largestCluster = nodeClusters.filter { it.size == largestClusterSize }.flatten().distinct()

        check(largestCluster.all { node -> nodeToConnections.getValue(node).containsAll(largestCluster - node) })

        val partTwo = largestCluster.sortedBy { it }.joinToString(",")

        return AOCAnswer(partOne, partTwo)
    }
}
