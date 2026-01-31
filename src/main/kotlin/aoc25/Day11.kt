package aoc25

import AOCAnswer
import AOCSolution
import product
import readInput

class Day11 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day11.txt", AOCYear.TwentyFive)

        val nodeToConnections = rawInput.associate { line ->
            val (node, connections) = line.split(": ")

            node to connections.split(" ")
        }

        val partOne = count(start = "you", target = "out", nodeToConnections)

        val path1 = listOf("svr", "dac", "fft", "out")
        val path2 = listOf("svr", "fft", "dac", "out")
        val paths = listOf(path1, path2)

        val partTwo = paths.sumOf { path ->
            path.zipWithNext { start, target -> count(start = start, target = target, nodeToConnections) }.product()
        }

        return AOCAnswer(partOne, partTwo)
    }

    private fun count(start: String, target: String, nodeToConnections: Map<String, List<String>>): Long {
        val cache = HashMap<String, Long>(nodeToConnections.size)

        fun run(node: String): Long {
            when (node) {
                target -> return 1
                in cache -> return cache.getValue(node)
            }

            val result = nodeToConnections[node].orEmpty().sumOf { run(it) }

            cache[node] = result
            return result
        }

        return run(start)
    }
}
