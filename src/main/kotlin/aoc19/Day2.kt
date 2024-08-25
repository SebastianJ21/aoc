package aoc19

import AOCYear
import kotlinx.collections.immutable.toPersistentList
import mapToInt
import printAOCAnswers
import readInput

class Day2 {

    fun solve() {
        val rawInput = readInput("day2.txt", AOCYear.Nineteen)

        val inputOpCodes = rawInput.single().split(",").mapToInt().toPersistentList()

        fun executeOpCodes(replacementValues: Pair<Int, Int>): Int {
            val initialOpCodes = inputOpCodes.set(1, replacementValues.first).set(2, replacementValues.second)

            val sequence = generateSequence(initialOpCodes to 0) { (opCodes, index) ->
                val instruction = opCodes[index]

                val func: (Int, Int) -> Int = when (instruction) {
                    99 -> return@generateSequence null
                    1 -> Int::plus
                    2 -> Int::times
                    else -> error("Illegal instruction $instruction")
                }

                val operandAIndex = opCodes[index + 1]
                val operandBIndex = opCodes[index + 2]
                val resultIndex = opCodes[index + 3]

                val result = func(opCodes[operandAIndex], opCodes[operandBIndex])
                val newOpCodes = opCodes.set(resultIndex, result)

                newOpCodes to index + 4
            }

            val (finalOpCodes) = sequence.last()

            return finalOpCodes.first()
        }

        val partOne = executeOpCodes(12 to 2)

        val target = 19690720
        val replacementRange = 0..99

        val resultReplacement = replacementRange.firstNotNullOf { firstReplacement ->
            replacementRange.firstNotNullOfOrNull { secondReplacement ->
                val replacement = firstReplacement to secondReplacement
                val result = executeOpCodes(replacement)

                replacement.takeIf { result == target }
            }
        }

        val partTwo = 100 * resultReplacement.first + resultReplacement.second

        printAOCAnswers(partOne, partTwo)
    }
}
