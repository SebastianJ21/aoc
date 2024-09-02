@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCYear
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import mapToInt
import printAOCAnswers
import readInput

class Day7 {

    data class ExecutionState(
        val program: PersistentList<Int>,
        val index: Int,
        val inputs: List<Int>,
        val outputs: List<Int> = emptyList(),
    )

    fun solve() {
        val rawInput = readInput("day7.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToInt().toPersistentList()

        val phaseSettingRange = 0..4
        val partOne = maxAmpSignal(instructions, phaseSettingRange.toList(), 0)

        val loopPhaseSettings = 5..9
        val partTwo = maxAmpLoopSignal(instructions, loopPhaseSettings.toList())

        printAOCAnswers(partOne, partTwo)
    }

    fun maxAmpLoopSignal(instructions: PersistentList<Int>, phaseSettings: List<Int>): Int {
        val lastAmpIndex = phaseSettings.last()
        val firstAmpIndex = phaseSettings.first()

        fun executeAmpLoop(configurationSetting: List<Int>): Map<Int, ExecutionState> {
            val initialAmps = phaseSettings.zip(configurationSetting) { ampIndex, phaseSetting ->
                ampIndex to ExecutionState(instructions, 0, listOf(phaseSetting))
            }.toMap()

            val initialState = Triple(0, firstAmpIndex, initialAmps)

            val sequence = generateSequence(initialState) { (inputSignal, ampIndex, ampStates) ->
                val ampState = ampStates.getValue(ampIndex).run {
                    // Add inputSignal from previous amp and clear output
                    copy(inputs = inputs.plus(inputSignal), outputs = emptyList())
                }

                val newState = executeInstructions(ampState, true)

                if (newState.outputs.isEmpty()) return@generateSequence null

                val newInputSignal = newState.outputs.last()
                val newAmpIndex = if (ampIndex == lastAmpIndex) firstAmpIndex else ampIndex + 1

                Triple(newInputSignal, newAmpIndex, ampStates.plus(ampIndex to newState))
            }

            val (_, _, finalAmpStates) = sequence.last()

            return finalAmpStates
        }

        fun findMaxAmpLoopSignal(unusedSettings: List<Int>, usedSettings: List<Int>): Int =
            unusedSettings.maxOf { settingToUse ->
                val newUnusedSettings = unusedSettings - settingToUse
                val newUsedSettings = usedSettings + settingToUse

                if (newUnusedSettings.isEmpty()) {
                    val ampLoopResult = executeAmpLoop(newUsedSettings)

                    ampLoopResult.getValue(lastAmpIndex).outputs.single()
                } else {
                    findMaxAmpLoopSignal(newUnusedSettings, newUsedSettings)
                }
            }

        return findMaxAmpLoopSignal(phaseSettings, emptyList())
    }

    fun maxAmpSignal(instructions: PersistentList<Int>, phaseSettings: List<Int>, input: Int): Int {
        val possibleOutputs = phaseSettings.associateWith { phaseSetting ->
            val state = ExecutionState(instructions, 0, listOf(phaseSetting, input))

            executeInstructions(state, false).outputs.single()
        }

        return possibleOutputs.maxOf { (usedSetting, output) ->
            val newPhaseSettings = phaseSettings.minus(usedSetting)

            if (newPhaseSettings.isEmpty()) {
                output
            } else {
                maxAmpSignal(instructions, newPhaseSettings, output)
            }
        }
    }

    fun executeInstructions(initialState: ExecutionState, stopOnOutput: Boolean): ExecutionState {
        fun readAddress(state: PersistentList<Int>, address: Int, isImmediateMode: Boolean) =
            if (isImmediateMode) address else state[address]

        val sequence = generateSequence(initialState) { (instructions, index, inputs, output) ->
            val instruction = instructions[index].toString().padStart(5, '0')

            val opCode = instruction.takeLast(2).toInt()
            val parameterModes = instruction.take(3).map { it.digitToInt() }.reversed()

            if (output.isNotEmpty() && stopOnOutput) {
                return@generateSequence null
            }

            when (opCode) {
                99 -> return@generateSequence null
                1, 2, 7, 8 -> {
                    val instructionFunction: (Int, Int) -> Int = when (opCode) {
                        1 -> Int::plus
                        2 -> Int::times
                        7 -> { a: Int, b: Int -> if (a < b) 1 else 0 }
                        8 -> { a: Int, b: Int -> if (a == b) 1 else 0 }
                        else -> error("Illegal instruction $instruction")
                    }

                    val operandA = readAddress(instructions, instructions[index + 1], parameterModes[0] == 1)
                    val operandB = readAddress(instructions, instructions[index + 2], parameterModes[1] == 1)
                    val writeAddress = instructions[index + 3]

                    val result = instructionFunction(operandA, operandB)
                    val newOpCodes = instructions.set(writeAddress, result)

                    ExecutionState(newOpCodes, index + 4, inputs, output)
                }
                3 -> {
                    val writeAddress = instructions[index + 1]
                    val inputValue = inputs.first()

                    val newOpCodes = instructions.set(writeAddress, inputValue)

                    ExecutionState(newOpCodes, index + 2, inputs.drop(1), output)
                }
                4 -> {
                    val newOutputValue = readAddress(instructions, instructions[index + 1], parameterModes.first() == 1)

                    ExecutionState(instructions, index + 2, inputs, output + newOutputValue)
                }
                5, 6 -> {
                    val func = when (opCode) {
                        5 -> { a: Int, b: Int -> a != b }
                        6 -> Int::equals
                        else -> error("Illegal instruction $instruction")
                    }

                    val operandA = readAddress(instructions, instructions[index + 1], parameterModes[0] == 1)
                    val operandB = readAddress(instructions, instructions[index + 2], parameterModes[1] == 1)

                    val newIndex = if (func(operandA, 0)) operandB else index + 3

                    ExecutionState(instructions, newIndex, inputs, output)
                }

                else -> error("Illegal opcode $opCode")
            }
        }

        return sequence.last()
    }
}
