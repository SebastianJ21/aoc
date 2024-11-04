@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import aoc19.IntCodeRunner.Companion.executeInstructions
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import mapToLong
import readInput

class Day7 : AOCSolution {

    override fun solve(): AOCAnswer  {
        val rawInput = readInput("day7.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()

        val initialMemory = instructions.withIndex().associate { (index, value) ->
            index.toLong() to value
        }.toPersistentMap()

        val phaseSettingRange = 0..4L
        val partOne = maxAmpSignal(initialMemory, phaseSettingRange.toList(), 0)

        val loopPhaseSettings = 5..9L
        val partTwo = maxAmpLoopSignal(initialMemory, loopPhaseSettings.toList())

        return AOCAnswer(partOne, partTwo)
    }

    fun maxAmpLoopSignal(initialMemory: PersistentMap<Long, Long>, phaseSettings: List<Long>): Long {
        val lastAmpIndex = phaseSettings.last()
        val firstAmpIndex = phaseSettings.first()

        fun executeAmpLoop(configurationSetting: List<Long>): Map<Long, ExecutionState> {
            val initialAmps = phaseSettings.zip(configurationSetting) { ampIndex, phaseSetting ->
                ampIndex to ExecutionState(initialMemory, listOf(phaseSetting))
            }.toMap()

            val initialState = Triple(0L, firstAmpIndex, initialAmps)

            val sequence = generateSequence(initialState) { (inputSignal, ampIndex, ampStates) ->
                val ampState = ampStates.getValue(ampIndex).run {
                    // Add inputSignal from previous amp and clear output
                    copy(inputs = inputs.plus(inputSignal), outputs = emptyList())
                }

                val newState = executeInstructions(ampState, 1)

                if (newState.outputs.isEmpty()) return@generateSequence null

                val newInputSignal = newState.outputs.last()
                val newAmpIndex = if (ampIndex == lastAmpIndex) firstAmpIndex else ampIndex + 1

                Triple(newInputSignal, newAmpIndex, ampStates.plus(ampIndex to newState))
            }

            val (_, _, finalAmpStates) = sequence.last()

            return finalAmpStates
        }

        fun findMaxAmpLoopSignal(unusedSettings: List<Long>, usedSettings: List<Long>): Long =
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

    fun maxAmpSignal(initialMemory: PersistentMap<Long, Long>, phaseSettings: List<Long>, input: Long): Long {
        val possibleOutputs = phaseSettings.associateWith { phaseSetting ->
            val state = ExecutionState(initialMemory, listOf(phaseSetting, input))

            executeInstructions(state).outputs.single()
        }

        return possibleOutputs.maxOf { (usedSetting, output) ->
            val newPhaseSettings = phaseSettings.minus(usedSetting)

            if (newPhaseSettings.isEmpty()) {
                output
            } else {
                maxAmpSignal(initialMemory, newPhaseSettings, output)
            }
        }
    }
}
