package aoc19

import AOCAnswer
import AOCSolution
import inputLines
import structures.IntList
import structures.toIntList
import kotlin.math.absoluteValue

class Day16 : AOCSolution {

    private val basePattern = listOf(0, 1, 0, -1).toIntList()

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()
        val inputList = inputLines.single().map { it.digitToInt() }.toIntList()

        val partOne = (1..100).fold(inputList) { acc, _ -> acc.next() }.take(8).joinToString("")

        // Skip this many places before reading the answer
        val skip = inputList.take(7).joinToString("").toInt()
        val expandedSize = 10_000 * inputList.size

        // The core assumption that allows us to runningReduce the tail
        check(skip > expandedSize / 2)

        val expandedSkipped = generateSequence { inputList }.flatten().take(expandedSize).drop(skip).toList()

        val partTwo = (1..100)
            .fold(expandedSkipped.asReversed().toIntList()) { acc, _ ->
                acc.runningReduce { acc, value -> (acc + value).last() }
            }
            .asReversed()
            .take(8)
            .joinToString("")

        return AOCAnswer(partOne, partTwo)
    }

    private fun IntList.next() = IntList(size) { index -> nextAt(index = index) }

    private fun IntList.nextAt(index: Int): Int {
        val pattern = basePattern.repeated(repeat = index + 1, size = size + 1).drop(1)

        return this.zip(pattern) { a, b -> a * b }.sum().last().absoluteValue
    }

    private fun IntList.repeated(repeat: Int, size: Int): IntList {
        require(repeat > 0)

        val result = IntArray(size)
        var index = 0

        loop@ while (true) {
            this.forEachInt { elem ->
                repeat(repeat) {
                    if (index >= size) break@loop

                    result[index++] = elem
                }
            }
        }

        return IntList.wrap(result)
    }

    private fun Int.last() = this % 10
}
