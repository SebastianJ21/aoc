package aoc19

import AOCYear
import mapToLong
import printAOCAnswers
import readInput

class Day5 {

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()

        val partOneInitialState = ExecutionState.fromList(instructions, listOf(1))
        val partTwoInitialState = ExecutionState.fromList(instructions, listOf(5))

        val partOne = IntCodeRunner.executeInstructions(partOneInitialState).outputs.last()
        val partTwo = IntCodeRunner.executeInstructions(partTwoInitialState).outputs.last()

        printAOCAnswers(partOne, partTwo)
    }
}
