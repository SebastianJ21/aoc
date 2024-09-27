package aoc19

import AOCYear
import aoc19.IntCodeRunner.Companion.executeInstructions
import mapToLong
import printAOCAnswers
import readInput

class Day5 {

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.Nineteen)

        val instructions = rawInput.single().split(",").mapToLong()

        val initialState = ExecutionState.fromList(instructions)

        val partOne = executeInstructions(initialState.withInputs(1L)).outputs.last()
        val partTwo = executeInstructions(initialState.withInputs(5L)).outputs.last()

        printAOCAnswers(partOne, partTwo)
    }
}
