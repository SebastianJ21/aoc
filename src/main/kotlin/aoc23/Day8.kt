package aoc23

import kotlinx.collections.immutable.persistentHashSetOf
import readInput
import java.math.BigInteger

class Day8 {

    fun solve() {
        val rawInput = readInput("day8.txt", AOCYear.TwentyThree)

        val instructions = rawInput.first().map {
            when (it) {
                'L' -> Pair<String, String>::first
                'R' -> Pair<String, String>::second
                else -> error("Invalid direction")
            }
        }

        val nodesMap = rawInput.drop(2).associate { line ->
            val (from, toPair) = line.split(" = ")
            val (toLeft, toRight) = toPair.replace(Regex("[()]"), "").split(", ")

            from to (toLeft to toRight)
        }

        // Slower, but pure and more beautiful
        fun firstOccurrenceOrNullPure(start: String, target: String): Int? {
            val set = persistentHashSetOf<Pair<String, Int>>()

            val occurrenceSeq = generateSequence(Triple(start, 0, set)) { (node, index, seen) ->
                val nodeToIndex = node to index
                if (node == target || seen.contains(nodeToIndex)) return@generateSequence null

                val instruction = instructions[index]

                Triple(instruction(nodesMap.getValue(node)), index.inc() % instructions.size, seen.add(nodeToIndex))
            }

            val (lastNode, _, seen) = occurrenceSeq.last()

            return if (lastNode == target) seen.size else null
        }

        fun firstOccurrenceOrNull(start: String, target: String): Int? {
            val seen = hashSetOf<Pair<String, Int>>()

            val occurrenceSeq = generateSequence(start to 0) { (node, index) ->

                if (node == target || !seen.add(node to index)) return@generateSequence null

                val instruction = instructions[index]

                instruction(nodesMap.getValue(node)) to index.inc() % instructions.size
            }

            val (lastNode, _) = occurrenceSeq.last()

            return if (lastNode == target) seen.size else null
        }

        val partOne = firstOccurrenceOrNullPure("AAA", "ZZZ")!!

        val nodesEndingWithA = nodesMap.keys.filter { it.last() == 'A' }
        val nodesEndingWithZ = nodesMap.keys.filter { it.last() == 'Z' }

        val startToPossibleEnds = nodesEndingWithA.associateWith { startNode ->
            nodesEndingWithZ.mapNotNull { endNode ->
                firstOccurrenceOrNullPure(startNode, endNode)?.let { endNode to it }
            }
        }

        val stepsToReachEndNodes = startToPossibleEnds.values
            .map { endNodes -> endNodes.single() }
            // Check that Start -> End nodes are paired uniquely OneToOne
            .also { endNodes -> check(endNodes.distinctBy { (nodeName, _) -> nodeName }.size == endNodes.size) }
            .map { (_, stepsToReach) -> stepsToReach.toBigInteger() }

        fun BigInteger.lcm(other: BigInteger) = this.times(other).div(this.gcd(other))

        val partTwo = stepsToReachEndNodes.reduce { acc, stepsToReach -> acc.lcm(stepsToReach) }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
