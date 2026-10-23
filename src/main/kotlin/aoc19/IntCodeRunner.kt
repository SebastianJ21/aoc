package aoc19

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap

data class ExecutionState(
    val memory: PersistentMap<Long, Long>,
    val inputs: List<Long> = emptyList(),
    val outputs: List<Long> = emptyList(),
    val index: Long = 0L,
    val relativeBaseOffset: Long = 0L,
) {

    companion object {
        fun fromList(listMem: List<Long>, inputs: List<Long> = emptyList()): ExecutionState {
            val memory = listMem.withIndex()
                .associate { (index, value) -> index.toLong() to value }
                .toPersistentHashMap()

            return ExecutionState(memory, inputs)
        }
    }
}

fun ExecutionState.withInputs(vararg inputs: Long) = copy(inputs = inputs.toList())

fun ExecutionState.withClearedOutputs() = copy(outputs = emptyList())

object IntCodeRunner {
    fun executeInstructions(initialState: ExecutionState, stopOnOutputSize: Int? = null): ExecutionState {
        val sequence = generateSequence(initialState) { currentState ->
            val (memory, inputs, output, index, relativeOffset) = currentState

            val instruction = (memory.getValue(index) % 100_000).toInt()

            // Last 2 digits of the instruction
            val opCode = (instruction % 100)

            // read each value right-to-left from the opcode:
            val stripped = (instruction - opCode) / 100

            val paramMode1 = (stripped) % 10
            val paramMode2 = (stripped / 10) % 10
            val paramMode3 = (stripped / 100) % 10

            if (stopOnOutputSize != null && stopOnOutputSize <= output.size) {
                return@generateSequence null
            }

            when (opCode) {
                99 -> return@generateSequence null
                1, 2, 7, 8 -> {
                    val operandA = readAddress(currentState, memory.getValue(index + 1), paramMode1)
                    val operandB = readAddress(currentState, memory.getValue(index + 2), paramMode2)

                    val writeAddress = readWriteAddress(currentState, memory.getValue(index + 3), paramMode3)

                    val result = when (opCode) {
                        1 -> operandA + operandB
                        2 -> operandA * operandB
                        7 -> if (operandA < operandB) 1 else 0
                        8 -> if (operandA == operandB) 1 else 0
                        else -> error("Illegal instruction $instruction")
                    }
                    val newMemory = memory.putting(writeAddress, result)

                    ExecutionState(newMemory, inputs, output, index + 4, relativeOffset)
                }
                3 -> {
                    val inputValue = inputs.first()

                    val writeAddress = readWriteAddress(currentState, memory.getValue(index + 1), paramMode1)
                    val newMemory = memory.putting(writeAddress, inputValue)

                    ExecutionState(newMemory, inputs.drop(1), output, index + 2, relativeOffset)
                }
                4 -> {
                    val newOutputValue = readAddress(currentState, memory.getValue(index + 1), paramMode1)

                    ExecutionState(memory, inputs, output + newOutputValue, index + 2, relativeOffset)
                }
                5, 6 -> {
                    val operandA = readAddress(currentState, memory.getValue(index + 1), paramMode1)
                    val operandB = readAddress(currentState, memory.getValue(index + 2), paramMode2)

                    val movedIndex = index + 3

                    val newIndex = when (opCode) {
                        5 -> if (operandA != 0L) operandB else movedIndex
                        6 -> if (operandA == 0L) operandB else movedIndex
                        else -> error("Illegal instruction $instruction")
                    }

                    ExecutionState(memory, inputs, output, newIndex, relativeOffset)
                }
                9 -> {
                    val value = readAddress(currentState, memory.getValue(index + 1), paramMode1)

                    val newOffset = relativeOffset + value

                    ExecutionState(memory, inputs, output, index + 2, newOffset)
                }

                else -> error("Illegal opcode $opCode")
            }
        }

        return sequence.last()
    }

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
}
