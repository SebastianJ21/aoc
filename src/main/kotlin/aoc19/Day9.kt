package aoc19

import AOCYear
import mapToLong
import printAOCAnswers
import readInput

class Day9 {

    fun solve() {
        val rawInput = readInput("day9.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()

        val partOneInitialState = ExecutionState.fromList(instructions, listOf(1))
        val partOne = IntCodeRunner.executeInstructions(partOneInitialState).outputs.single()

        val partTwoInitialState = ExecutionState.fromList(instructions, listOf(2))
        val partTwo = IntCodeRunner.executeInstructions(partTwoInitialState).outputs.single()

        printAOCAnswers(partOne, partTwo)
    }
}
