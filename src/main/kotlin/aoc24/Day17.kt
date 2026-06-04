package aoc24

import AOCAnswer
import AOCSolution
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import mapToLong
import inputLines
import splitBy

class Day17 : AOCSolution {

    private data class State(
        val registerA: Long,
        val registerB: Long,
        val registerC: Long,
        val instructionPointer: Int,
        val output: PersistentList<Int>,
    )

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val (registerInput, programInput) = inputLines.splitBy { it.isEmpty() }

        // Register A: 63687530
        // Register B: 0
        // Register C: 0
        val registers = registerInput.map { it.split(": ").last().toLong() }

        // Program: 2,4,1,3,7,5,0,3,1,5,4,1,5,5,3,0
        val program = programInput.single().split(": ").last().split(",").mapToLong()

        val initialState = State(
            registerA = registers[0],
            registerB = registers[1],
            registerC = registers[2],
            instructionPointer = 0,
            output = persistentListOf(),
        )

        val finalState = initialState.executeProgram(program).output

        val partOne = finalState.joinToString(",")
        val partTwo = findQuine(program = program, initialState = initialState)

        return AOCAnswer(partOne, partTwo)
    }

    private fun State.executeProgram(program: List<Long>): State {
        val sequence = generateSequence(this) { state ->
            val opCode = program.getOrNull(state.instructionPointer) ?: return@generateSequence null
            val operand = program.getOrNull(state.instructionPointer + 1) ?: return@generateSequence null

            state.execute(opCode, operand)
        }

        return sequence.last()
    }

    private fun State.execute(opCode: Long, operand: Long): State = when (opCode) {
        // The adv instruction (opcode 0) performs division.
        // The numerator is the value in the A register.
        // The denominator is found by raising 2 to the power of the instruction's combo operand.
        // The result of the division operation is truncated to an integer and then written to the A register.
        0L -> {
            val resolvedOperand = resolveComboOperand(value = operand, state = this)
            require(resolvedOperand < Int.MAX_VALUE)

            val denominator = 1L shl resolvedOperand.toInt()

            copy(registerA = registerA / denominator, instructionPointer = instructionPointer + 2)
        }
        // The bxl instruction (opcode 1) calculates the bitwise XOR of register B and the instruction's literal operand,
        // then stores the result in register B.
        1L -> copy(registerB = registerB xor operand, instructionPointer = instructionPointer + 2)
        // The bst instruction (opcode 2) calculates the value of its combo operand modulo 8 (thereby keeping only its lowest 3 bits),
        // then writes that value to the B register.
        2L -> {
            val resolvedOperand = resolveComboOperand(operand, this)

            // % 8
            copy(registerB = resolvedOperand and 7, instructionPointer = instructionPointer + 2)
        }
        // The jnz instruction (opcode 3) does nothing if the A register is 0.
        // However, if the A register is not zero, it jumps by setting the instruction pointer to the value of its literal operand;
        // if this instruction jumps, the instruction pointer is not increased by 2 after this instruction.
        3L -> {
            if (registerA == 0L) {
                copy(instructionPointer = instructionPointer + 2)
            } else {
                require(operand < Int.MAX_VALUE)

                copy(instructionPointer = operand.toInt())
            }
        }
        // The bxc instruction (opcode 4) calculates the bitwise XOR of register B and register C,
        // then stores the result in register B. (For legacy reasons, this instruction reads an operand but ignores it.)
        4L -> copy(registerB = registerB xor registerC, instructionPointer = instructionPointer + 2)

        // The out instruction (opcode 5) calculates the value of its combo operand modulo 8,
        // then outputs that value. (If a program outputs multiple values, they are separated by commas.)
        5L -> {
            val resolvedOperand = resolveComboOperand(operand, this)

            copy(
                // combo operand modulo 8 (thereby keeping only its lowest 3 bits)
                output = output.add((resolvedOperand and 7L).toInt()),
                instructionPointer = instructionPointer + 2,
            )
        }
        // The bdv instruction (opcode 6) works exactly like the adv instruction except that the result is stored in the B register.
        // (The numerator is still read from the A register.)
        6L -> {
            val resolvedOperand = resolveComboOperand(value = operand, state = this)
            require(resolvedOperand < Int.MAX_VALUE)

            val denominator = 1 shl resolvedOperand.toInt()

            copy(registerB = registerA / denominator, instructionPointer = instructionPointer + 2)
        }
        // The cdv instruction (opcode 7) works exactly like the adv instruction except that the result is stored in the C register.
        // (The numerator is still read from the A register.)
        7L -> {
            val resolvedOperand = resolveComboOperand(value = operand, state = this)
            require(resolvedOperand < Int.MAX_VALUE)

            val denominator = 1 shl resolvedOperand.toInt()

            copy(registerC = registerA / denominator, instructionPointer = instructionPointer + 2)
        }
        else -> error("Unknown opCode: $opCode")
    }

    private fun resolveComboOperand(value: Long, state: State): Long = when (value) {
        0L -> 0
        1L -> 1
        2L -> 2
        3L -> 3
        4L -> state.registerA
        5L -> state.registerB
        6L -> state.registerC
        7L -> error("Combo operand 7 is reserved and will not appear in valid programs")
        else -> error("Unknown combo operand: $value")
    }

    private fun findQuine(program: List<Long>, initialState: State): Long {
        /*
        Each element in current represents a number in range [0, 8) (a 3-bit number).
         */
        fun search(current: PersistentList<Int>, index: Int): PersistentList<Int>? {
            if (index == program.size) return current

            val programIndex = program.lastIndex - index
            val target = program[programIndex].toInt()

            return (0..7).firstNotNullOfOrNull { value ->
                val registerA = current.set(index, value)
                    .joinToString("") { it.toString(2).padStart(3, '0') }
                    .toLong(2)

                val output = initialState.copy(registerA = registerA).executeProgram(program).output

                if (output.size != program.size) return@firstNotNullOfOrNull null
                if (output[programIndex] != target) return@firstNotNullOfOrNull null

                val picked = search(
                    current = current.set(index, value),
                    index = index + 1,
                )

                picked
            }
        }

        val initial = List(program.size) { 0 }.toPersistentList()

        val result = search(current = initial, index = 0) ?: error("No answer found")

        return result.joinToString("") { it.toString(2).padStart(3, '0') }.toLong(2)
    }
}
