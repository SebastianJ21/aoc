package aoc24

import AOCAnswer
import AOCSolution
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import mapToLong
import readInput

class Day7 : AOCSolution {

    private val baseOperators = listOf<(Long, Long) -> Long>(Long::plus, Long::times)
    private val concatenationOperator: (Long, Long) -> Long = { a, b ->
        // Count b digits
        val multiplier = generateSequence(10L) { it * 10 }.first { it > b }

        a * multiplier + b
    }
    private val allOperators = (baseOperators + concatenationOperator)

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day7.txt", AOCYear.TwentyFour)

        val instructions = rawInput.map { line ->
            // Line example: "12615: 804 5 2 1 5 7 7 7 1 3 3 5"
            val (expectedResult, operands) = line.split(": ")

            expectedResult.toLong() to operands.split(" ").mapToLong()
        }

        val (reachableWithBase, unreachableWithBase) = instructions.partition { (result, operands) ->
            canReachTarget(operands, baseOperators, result)
        }

        val reachableWithBaseResultsSum = reachableWithBase.sumOf { (result) -> result }

        val reachableWithAll = unreachableWithBase.filter { (result, operands) ->
            canReachTarget(operands, allOperators, result)
        }

        val partOne = reachableWithBaseResultsSum
        val partTwo = reachableWithAll.sumOf { (result) -> result } + reachableWithBaseResultsSum

        return AOCAnswer(partOne, partTwo)
    }

    private fun canReachTarget(operands: List<Long>, operators: List<(Long, Long) -> Long>, target: Long): Boolean {
        fun canReachTargetInternal(operandLeft: PersistentList<Long>, current: Long): Boolean {
            when {
                current > target -> return false
                operandLeft.isEmpty() -> return current == target
            }

            return operators.any { operator ->
                val newCurrent = operator(current, operandLeft.first())

                canReachTargetInternal(operandLeft.removeAt(0), newCurrent)
            }
        }

        return canReachTargetInternal(operands.toPersistentList().removeAt(0), operands.first())
    }
}
