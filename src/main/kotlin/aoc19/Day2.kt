package aoc19

import AOCAnswer
import AOCSolution
import aoc19.IntCodeRunner.executeInstructions
import inputLines
import kotlinx.collections.immutable.toPersistentMap
import mapToLong

class Day2 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val instructions = inputLines.single().split(",").mapToLong()

        val initialMemory = instructions.withIndex().associate { (index, value) ->
            index.toLong() to value
        }.toPersistentMap()

        val partOneProgram = ExecutionState(initialMemory.putting(1, 12).putting(2, 2))
        val partOne = executeInstructions(partOneProgram).memory.getValue(0)

        val target = 19690720L
        val replacementRange = 0..99L

        val resultReplacement = replacementRange.firstNotNullOf { firstReplacement ->
            replacementRange.firstNotNullOfOrNull { secondReplacement ->
                val program = ExecutionState(initialMemory.putting(1, firstReplacement).putting(2, secondReplacement))

                val result = executeInstructions(program).memory.getValue(0)

                if (result == target) firstReplacement to secondReplacement else null
            }
        }

        val partTwo = 100 * resultReplacement.first + resultReplacement.second

        return AOCAnswer(partOne, partTwo)
    }
}
