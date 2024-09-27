package aoc19

import AOCYear
import aoc19.IntCodeRunner.Companion.executeInstructions
import mapToLong
import printAOCAnswers
import readInput

class Day9 {

    fun solve() {
        val rawInput = readInput("day9.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()
        val initialState = ExecutionState.fromList(instructions)

        val partOne = executeInstructions(initialState.withInputs(1)).outputs.single()
        val partTwo = executeInstructions(initialState.withInputs(2)).outputs.single()

        printAOCAnswers(partOne, partTwo)
    }
}
