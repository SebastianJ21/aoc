package aoc19

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

data class ExecutionState(
    val memory: PersistentMap<Long, Long>,
    val inputs: List<Long>,
    val outputs: List<Long> = emptyList(),
    val index: Long = 0L,
    val relativeBaseOffset: Long = 0L,
) {

    companion object {
        fun fromList(listMem: List<Long>, inputs: List<Long>): ExecutionState {
            val memory = listMem.withIndex().associate { (index, value) -> index.toLong() to value }.toPersistentMap()

            return ExecutionState(memory, inputs)
        }
    }
}

class IntCodeRunner {

    companion object {

        private fun readAddress(state: ExecutionState, address: Long, mode: Int = 0): Long = when (mode) {
            0 -> state.memory[address] ?: 0L
            1 -> address
            2 -> state.memory[address + state.relativeBaseOffset] ?: 0L
            else -> error("Illegal parameter mode $mode")
        }

        private fun readWriteAddress(state: ExecutionState, index: Long, mode: Int) = when (mode) {
            0 -> index
            2 -> index + state.relativeBaseOffset
            else -> error("Illegal mode for readWriteAddress $mode")
        }

        fun executeInstructions(initialState: ExecutionState, stopOnOutput: Boolean = false): ExecutionState {
            val sequence = generateSequence(initialState) { currentState ->
                val (memory, inputs, output, index, relativeOffset) = currentState

                val instruction = memory.getValue(index).toString().padStart(5, '0')

                val opCode = instruction.takeLast(2).toInt()
                val parameterModes = instruction.take(3).map { it.digitToInt() }.reversed()

                if (output.isNotEmpty() && stopOnOutput) {
                    return@generateSequence null
                }

                when (opCode) {
                    99 -> return@generateSequence null
                    1, 2, 7, 8 -> {
                        val operandA = readAddress(currentState, memory.getValue(index + 1), parameterModes[0])
                        val operandB = readAddress(currentState, memory.getValue(index + 2), parameterModes[1])

                        val writeAddress = readWriteAddress(currentState, memory.getValue(index + 3), parameterModes[2])

                        val result = when (opCode) {
                            1 -> operandA + operandB
                            2 -> operandA * operandB
                            7 -> if (operandA < operandB) 1 else 0
                            8 -> if (operandA == operandB) 1 else 0
                            else -> error("Illegal instruction $instruction")
                        }
                        val newMemory = memory.put(writeAddress, result)

                        ExecutionState(newMemory, inputs, output, index + 4, relativeOffset)
                    }
                    3 -> {
                        val inputValue = inputs.first()

                        val writeAddress = readWriteAddress(currentState, memory.getValue(index + 1), parameterModes[0])
                        val newMemory = memory.put(writeAddress, inputValue)

                        ExecutionState(newMemory, inputs.drop(1), output, index + 2, relativeOffset)
                    }
                    4 -> {
                        val newOutputValue = readAddress(currentState, memory.getValue(index + 1), parameterModes[0])

                        ExecutionState(memory, inputs, output + newOutputValue, index + 2, relativeOffset)
                    }
                    5, 6 -> {
                        val operandA = readAddress(currentState, memory.getValue(index + 1), parameterModes[0])
                        val operandB = readAddress(currentState, memory.getValue(index + 2), parameterModes[1])

                        val movedIndex = index + 3

                        val newIndex = when (opCode) {
                            5 -> if (operandA != 0L) operandB else movedIndex
                            6 -> if (operandA == 0L) operandB else movedIndex
                            else -> error("Illegal instruction $instruction")
                        }

                        ExecutionState(memory, inputs, output, newIndex, relativeOffset)
                    }
                    9 -> {
                        val value = readAddress(currentState, memory.getValue(index + 1), parameterModes[0])

                        val newOffset = relativeOffset + value

                        ExecutionState(memory, inputs, output, index + 2, newOffset)
                    }

                    else -> error("Illegal opcode $opCode")
                }
            }

            return sequence.last()
        }
    }
}
