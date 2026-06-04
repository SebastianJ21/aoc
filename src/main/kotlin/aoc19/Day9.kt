package aoc19

import AOCAnswer
import AOCSolution
import aoc19.IntCodeRunner.Companion.executeInstructions
import mapToLong
import inputLines

class Day9 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val instructions = inputLines.single().split(",").mapToLong()
        val initialState = ExecutionState.fromList(instructions)

        val partOne = executeInstructions(initialState.withInputs(1)).outputs.single()
        val partTwo = executeInstructions(initialState.withInputs(2)).outputs.single()

        return AOCAnswer(partOne, partTwo)
    }
}
