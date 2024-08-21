@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentList
import readInput

class Day8 {

    enum class OperationType { ACC, NOP, JMP }

    fun solve() {
        val rawInput = readInput("day8.txt", AOCYear.Twenty)

        val instructions = rawInput.map { line ->
            val (operation, rawNumber) = line.split(" ")

            OperationType.valueOf(operation.uppercase()) to rawNumber.toInt()
        }.toPersistentList()

        val executionSequence = createExecutionSequence(instructions)

        val partOne = executionSequence.last().second

        val partTwo = instructions.indices.firstNotNullOf { instructionIndex ->
            val (operation, value) = instructions[instructionIndex]

            val toSwap = when (operation) {
                OperationType.NOP -> OperationType.JMP to value
                OperationType.JMP -> OperationType.NOP to value
                OperationType.ACC -> return@firstNotNullOf null
            }

            val alteredInstructions = instructions.set(instructionIndex, toSwap)

            val (index, acc) = createExecutionSequence(alteredInstructions).last()

            acc.takeIf { index > instructions.lastIndex }
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun createExecutionSequence(instructions: List<Pair<OperationType, Int>>) =
        generateSequence(Triple(0, 0, persistentHashSetOf<Int>())) { (index, acc, seen) ->
            if (index in seen || index > instructions.lastIndex) return@generateSequence null

            val (operation, value) = instructions[index]

            when (operation) {
                OperationType.ACC -> Triple(index + 1, acc + value, seen.add(index))
                OperationType.JMP -> Triple(index + value, acc, seen.add(index))
                else -> Triple(index + 1, acc, seen.add(index))
            }
        }
}
