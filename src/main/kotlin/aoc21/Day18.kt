package aoc21

import AOCYear
import readInput
import kotlin.math.ceil
import kotlin.math.floor

sealed class SnailTree {
    abstract val depth: Int

    data class ValueNode(val value: Int, override val depth: Int) : SnailTree()
    data class PairNode(val left: SnailTree, val right: SnailTree, override val depth: Int) : SnailTree()
}

private typealias ValuePair_Depth_Occurrence = Triple<Pair<Int, Int>, Int, Int>

class Day18 {

    data class SnailNumber(
        val raw: String,
        val tree: SnailTree = raw.parseToSnailTree(),
    )

    fun solve() {
        val rawInput = readInput("day18.txt", AOCYear.TwentyOne)

        val snailNumbers = rawInput.map { SnailNumber(it) }

        val partOne = snailNumbers.reduce { acc, current ->
            reduceSnailNumber(addSnailNumbers(acc, current))
        }.getMagnitude()

        val allPairs = snailNumbers.run {
            flatMapIndexed { index, first -> drop(index + 1).map { second -> first to second } }
        }

        val partTwo = allPairs.maxOf { (a, b) -> reduceSnailNumber(addSnailNumbers(a, b)).getMagnitude() }

        println("Part ine: $partOne")
        println("Part two: $partTwo")
    }

    private fun SnailNumber.reducedOrNull(): SnailNumber? {
        val explode = findExplode()

        return if (explode != null) {
            val (explodingNode, _, occurrence) = explode

            performExplode(explodingNode, occurrence)
        } else {
            findSplit()?.let { performSplit(it) }
        }
    }

    private fun reduceSnailNumber(number: SnailNumber): SnailNumber {
        return generateSequence(number) { current -> current.reducedOrNull() }.last()
    }

    private fun addSnailNumbers(a: SnailNumber, b: SnailNumber) = SnailNumber("[${a.raw},${b.raw}]")

    private fun SnailNumber.getMagnitude(): Int {
        fun SnailTree.getMagnitude(): Int = when (this) {
            is SnailTree.ValueNode -> value
            is SnailTree.PairNode -> (3 * left.getMagnitude() + 2 * right.getMagnitude())
        }

        return tree.getMagnitude()
    }

    private fun SnailNumber.performExplode(valuePair: Pair<Int, Int>, pairOccurrence: Int): SnailNumber {
        val (left, right) = valuePair

        val pairStr = "[$left,$right]"

        tailrec fun String.getStartIndex(n: Int): Int =
            if (n == 1) indexOf(pairStr) else replaceFirst(pairStr, "-".repeat(pairStr.length)).getStartIndex(n - 1)

        val startIndex = raw.getStartIndex(pairOccurrence)

        val endIndex = startIndex + pairStr.length

        val newLeft = raw.substring(0, startIndex).run {
            val numberStr = dropLastWhile { !it.isDigit() }.takeLastWhile { it.isDigit() }

            if (numberStr.isEmpty()) {
                this
            } else {
                val newValue = numberStr.toInt() + left
                val indexStart = lastIndexOf(numberStr)
                val indexEnd = indexStart + numberStr.length

                replaceRange(indexStart, indexEnd, newValue.toString())
            }
        }

        val newRight = raw.substring(endIndex).run {
            val numberStr = dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }

            if (numberStr.isEmpty()) {
                this
            } else {
                val newValue = numberStr.toInt() + right
                val oldNumStartIndex = indexOf(numberStr)
                val oldNumEndIndex = oldNumStartIndex + numberStr.length

                replaceRange(oldNumStartIndex, oldNumEndIndex, newValue.toString())
            }
        }

        return SnailNumber(newLeft + "0" + newRight)
    }

    private fun SnailNumber.findExplode(): ValuePair_Depth_Occurrence? {
        return getValuePairsWithOccurrences().firstOrNull { (_, depth, _) -> depth >= 4 }
    }

    private fun SnailNumber.performSplit(value: Int): SnailNumber {
        val valueStr = value.toString()

        // Split is always the first value regardless of depth
        val startIndex = raw.indexOf(valueStr)
        val endIndex = startIndex + valueStr.length

        val dividedValue = value.div(2.0)
        val leftValue = floor(dividedValue).toInt()
        val rightValue = ceil(dividedValue).toInt()

        val newPair = "[$leftValue,$rightValue]"

        return SnailNumber(raw.replaceRange(startIndex, endIndex, newPair))
    }

    private fun SnailNumber.findSplit(): Int? {
        return getValueNodesValues().firstOrNull { it >= 10 }
    }

    private fun SnailNumber.getValuePairsWithOccurrences(): List<ValuePair_Depth_Occurrence> {
        val queue = ArrayDeque(listOf(this.tree))

        return buildList {
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()

                if (node is SnailTree.PairNode) {
                    if (node.left is SnailTree.ValueNode && node.right is SnailTree.ValueNode) {
                        val valuePair = node.left.value to node.right.value

                        val occurrence = lastOrNull { (pair) -> pair == valuePair }?.third?.inc() ?: 1

                        add(Triple(valuePair, node.depth, occurrence))
                    }

                    if (node.right is SnailTree.PairNode) {
                        queue.addFirst(node.right)
                    }

                    if (node.left is SnailTree.PairNode) {
                        queue.addFirst(node.left)
                    }
                }
            }
        }
    }

    private fun SnailNumber.getValueNodesValues(): List<Int> {
        val queue = ArrayDeque(listOf(this.tree))

        return buildList {
            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()

                when (node) {
                    is SnailTree.ValueNode -> add(node.value)
                    is SnailTree.PairNode -> {
                        queue.addFirst(node.right)
                        queue.addFirst(node.left)
                    }
                }
            }
        }
    }
}

private fun String.parseToSnailTree(depth: Int = 0): SnailTree {
    if (toIntOrNull() != null) {
        return SnailTree.ValueNode(toInt(), depth)
    }

    val withoutFirstLast = substring(1, lastIndex)

    val splittingPoint = withoutFirstLast.asSequence().runningFold(0 to ' ') { (depth, _), char ->
        when (char) {
            '[' -> depth + 1 to char
            ']' -> depth - 1 to char
            else -> depth to char
        }
    }.indexOfFirst { (depth, char) -> char == ',' && depth == 0 }.dec()

    val left = withoutFirstLast.substring(0, splittingPoint)
    val right = withoutFirstLast.substring(splittingPoint + 1)

    val node = SnailTree.PairNode(left.parseToSnailTree(depth + 1), right.parseToSnailTree(depth + 1), depth)
    return node
}
