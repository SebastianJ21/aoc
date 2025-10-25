@file:Suppress("MemberVisibilityCanBePrivate")

package aoc21

import AOCYear
import alsoPrintLn
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import readInput
import splitBy
import java.util.concurrent.Executors

@Suppress("ktlint:standard:enum-entry-name-case")
class Day24 {

    enum class Instruction {
        inp,
        mul,
        add,
        mod,
        div,
        eql,
    }
    enum class Register { x, y, z, w }

    fun registerToIndex(register: Register) = when (register) {
        Register.x -> 0
        Register.y -> 1
        Register.z -> 2
        Register.w -> 3
    }

    sealed class Tree {
        abstract val resultRegister: List<Long>

        data class Root(override val resultRegister: List<Long>) : Tree()

        data class Node(
            override val resultRegister: List<Long>,
            val predecessor: Tree,
            val input: Long,
        ) : Tree()
    }

    fun solve() {
        val rawInput = readInput("day24.txt", AOCYear.TwentyOne)
        val initialRegisters = Register.entries.map { 0L }

        val rootNode = Tree.Root(initialRegisters.dropLast(1))

        val splitByInputCommand = rawInput.drop(1).splitBy { it.contains("inp") }

        val individualExecutions = splitByInputCommand.map { extractExecutionFlow(it) }

        fun findValidInputSequence(smallest: Boolean): List<Tree.Node> {
            val inputNumbers = if (smallest) 1L..9L else 9L downTo 1L

            fun Tree.getNextStates(
                seenRegisters: MutableSet<List<Long>>,
                execution: List<(MutableList<Long>) -> Unit>,
                isLast: Boolean,
            ): List<Tree.Node> {
                val templateRegister = inputNumbers.map { inputNumber ->
                    resultRegister + inputNumber
                }

                return templateRegister.mapNotNull { registerToUse ->
                    val register = registerToUse.toMutableList()

                    execution.forEach { it(register) }

                    val resultRegister = register.dropLast(1)

                    if (seenRegisters.add(resultRegister) && (!isLast || resultRegister.last() == 0L)) {
                        Tree.Node(resultRegister, this, registerToUse.last())
                    } else {
                        null
                    }
                }
            }

            val (firstExecution, executions) = individualExecutions.first() to individualExecutions.drop(1)

            val initialNodes = rootNode.getNextStates(hashSetOf(), firstExecution, false)

            val lastIndex = executions.lastIndex
            return executions.foldIndexed(initialNodes) { index, parentNodes, execution ->
                val seenRegisters = hashSetOf<List<Long>>()

                parentNodes.flatMap { node ->
                    node.getNextStates(seenRegisters, execution, index == lastIndex)
                }
            }
        }

        val (maxNode, minNode) = runBlocking(Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()) {
            val maxNode = async { findValidInputSequence(false).maxBy { it.input } }
            val minNode = async { findValidInputSequence(true).minBy { it.input } }

            maxNode.await() to minNode.await()
        }

        fun Tree.Node.extractAnswer() = generateSequence(this) { (_, parentNode) ->
            if (parentNode is Tree.Node) parentNode else null
        }.joinToString("") { it.input.toString() }.reversed()

        maxNode.extractAnswer().alsoPrintLn { }
        minNode.extractAnswer().alsoPrintLn { }
    }

    fun extractExecutionFlow(input: List<String>): List<(MutableList<Long>) -> Unit> {
        return input.map { rawOperation ->
            val instructionParts = rawOperation.split(" ")

            check(instructionParts.size == 3) {
                "Instruction $rawOperation does not contain 3 components. It most likely is an inp command"
            }

            val (instruction, arg1Index, resolveArg2) = instructionParts.let { (rawInstruction, rawArg1, rawArg2) ->
                val arg1RegisterIndex = registerToIndex(Register.valueOf(rawArg1))

                val arg2Long = rawArg2.toLongOrNull()
                val arg2RegisterIndex = if (arg2Long == null) registerToIndex(Register.valueOf(rawArg2)) else null

                val resolveArg2Func = { registers: MutableList<Long> ->
                    arg2Long ?: registers[arg2RegisterIndex!!]
                }

                Triple(Instruction.valueOf(rawInstruction), arg1RegisterIndex, resolveArg2Func)
            }
            val valueFunction = instructionToFunction(instruction)

            val function = { registers: MutableList<Long> ->
                val arg1 = registers[arg1Index]
                val arg2 = resolveArg2(registers)

                registers[arg1Index] = valueFunction(arg1, arg2)
            }

            function
        }
    }

    fun instructionToFunction(instruction: Instruction): (Long, Long) -> Long = when (instruction) {
        Instruction.inp -> { _: Long, b: Long -> b }
        Instruction.add -> Long::plus
        Instruction.div -> Long::div
        Instruction.mul -> Long::times
        Instruction.mod -> Long::mod
        Instruction.eql -> { a: Long, b: Long -> if (a == b) 1 else 0 }
    }
}
