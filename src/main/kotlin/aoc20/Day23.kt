@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import product
import readInput

class Day23 {
    data class CircularNode<T>(val value: T) {
        lateinit var previous: CircularNode<T>
        lateinit var next: CircularNode<T>

        fun insertAfter(value: T) {
            val newNode = CircularNode(value)

            insertAfter(newNode)
        }

        fun insertAfter(newNode: CircularNode<T>) {
            if (this::next.isInitialized) {
                next.previous = newNode
                newNode.next = next
            }

            next = newNode
            newNode.previous = this
        }

        fun remove(): CircularNode<T> {
            previous.next = next
            next.previous = previous

            return this
        }
    }

    fun createNodes(inputCups: List<Int>): List<CircularNode<Int>> {
        val nodes = inputCups.drop(1).runningFold(CircularNode(inputCups.first())) { node, newValue ->
            node.insertAfter(newValue)

            node.next
        }

        nodes.first().previous = nodes.last()
        nodes.last().next = nodes.first()

        return nodes
    }

    fun findNodeByValue(start: CircularNode<Int>, value: Int): CircularNode<Int> {
        val searchSeq = generateSequence(start.next) { current ->
            when (current.value) {
                value -> null
                start.value -> error("Value not found")
                else -> current.next
            }
        }

        return searchSeq.last()
    }

    fun runningWalk(head: CircularNode<Int>): List<CircularNode<Int>> {
        val walkSeq = generateSequence(head.next) { currentNode ->
            if (currentNode.next.value == head.value) null else currentNode.next
        }

        return walkSeq.toList()
    }

    fun solve() {
        val rawInput = readInput("day23.txt", AOCYear.Twenty)

        val input = rawInput.single().map { it.digitToInt() }
        val inputPartTwo = input + (input.max().inc()..1_000_000).toList()

        val nodesPartOne = createNodes(input)
        val nodesPartTwo = createNodes(inputPartTwo)

        val resultPartOne = getResultNode(nodesPartOne, 100)
        val resultPartTwo = getResultNode(nodesPartTwo, 10_000_000)

        val partOneFinalNodes = runningWalk(findNodeByValue(resultPartOne, 1))
        val partTwoFinalNodes = findNodeByValue(resultPartTwo, 1).let { oneNode ->
            listOf(oneNode.next, oneNode.next.next)
        }

        val partOne = partOneFinalNodes.joinToString("") { it.value.toString() }
        val partTwo = partTwoFinalNodes.map { it.value.toLong() }.product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun getResultNode(nodes: List<CircularNode<Int>>, moves: Int): CircularNode<Int> {
        val valueToNode = nodes.associateBy { it.value }

        val maxVal = nodes.maxOf { it.value }

        val resultNode = (1..moves).fold(nodes.first()) { currentNode, _ ->
            val a = currentNode.next.remove()
            val b = currentNode.next.remove()
            val c = currentNode.next.remove()

            val destinationValue = generateSequence(currentNode.value - 1) { lowerValue ->
                when {
                    lowerValue < 1 -> maxVal
                    lowerValue == a.value || lowerValue == b.value || lowerValue == c.value -> lowerValue - 1
                    else -> null
                }
            }.last()

            val destinationNode = valueToNode.getValue(destinationValue)

            destinationNode.insertAfter(c)
            destinationNode.insertAfter(b)
            destinationNode.insertAfter(a)

            currentNode.next
        }

        return resultNode
    }
}
