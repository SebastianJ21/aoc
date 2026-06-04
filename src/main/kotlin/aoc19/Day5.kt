package aoc19

import AOCAnswer
import AOCSolution
import aoc19.IntCodeRunner.Companion.executeInstructions
import mapToLong
import inputLines

class Day5 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val instructions = inputLines.single().split(",").mapToLong()

        val initialState = ExecutionState.fromList(instructions)

        val partOne = executeInstructions(initialState.withInputs(1L)).outputs.last()
        val partTwo = executeInstructions(initialState.withInputs(5L)).outputs.last()

        return AOCAnswer(partOne, partTwo)
    }
}
