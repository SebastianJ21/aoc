package aoc19

import AOCYear
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import mapToInt
import printAOCAnswers
import readInput

class Day5 {

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.Nineteen)

        val inputOpCodes = rawInput.single().split(",").mapToInt().toPersistentList()

        val partOne = executeOpCodes(inputOpCodes, 1).second.last()
        val partTwo = executeOpCodes(inputOpCodes, 5).second.last()

        printAOCAnswers(partOne, partTwo)
    }

    fun executeOpCodes(inputOpCodes: PersistentList<Int>, inputValue: Int): Pair<PersistentList<Int>, List<Int>> {
        fun readAddress(state: PersistentList<Int>, address: Int, isImmediateMode: Boolean) =
            if (isImmediateMode) address else state[address]

        val initial = Triple(inputOpCodes, 0, listOf<Int>())

        val sequence = generateSequence(initial) { (opCodes, index, output) ->
            val instruction = opCodes[index].toString().padStart(5, '0')

            val opCode = instruction.takeLast(2).toInt()
            val parameterModes = instruction.take(3).map { it.digitToInt() }.reversed()

            when (opCode) {
                99 -> return@generateSequence null
                1, 2, 7, 8 -> {
                    val func: (Int, Int) -> Int = when (opCode) {
                        1 -> Int::plus
                        2 -> Int::times
                        7 -> { a: Int, b: Int -> if (a < b) 1 else 0 }
                        8 -> { a: Int, b: Int -> if (a == b) 1 else 0 }
                        else -> error("Illegal instruction $instruction")
                    }

                    val operandA = readAddress(opCodes, opCodes[index + 1], parameterModes[0] == 1)
                    val operandB = readAddress(opCodes, opCodes[index + 2], parameterModes[1] == 1)
                    val writeAddress = opCodes[index + 3]

                    val result = func(operandA, operandB)
                    val newOpCodes = opCodes.set(writeAddress, result)

                    Triple(newOpCodes, index + 4, output)
                }
                3 -> {
                    val writeAddress = opCodes[index + 1]
                    val newOpCodes = opCodes.set(writeAddress, inputValue)

                    Triple(newOpCodes, index + 2, output)
                }
                4 -> {
                    val newOutputValue = readAddress(opCodes, opCodes[index + 1], parameterModes.first() == 1)

                    Triple(opCodes, index + 2, output + newOutputValue)
                }
                5, 6 -> {
                    val func = when (opCode) {
                        5 -> { a: Int, b: Int -> a != b }
                        6 -> Int::equals
                        else -> error("Illegal instruction $instruction")
                    }

                    val operandA = readAddress(opCodes, opCodes[index + 1], parameterModes[0] == 1)
                    val operandB = readAddress(opCodes, opCodes[index + 2], parameterModes[1] == 1)

                    val newIndex = if (func(operandA, 0)) operandB else index + 3

                    Triple(opCodes, newIndex, output)
                }

                else -> error("Illegal opcode $opCode")
            }
        }

        val (finalOpCodes, _, output) = sequence.last()

        return finalOpCodes to output
    }
}
