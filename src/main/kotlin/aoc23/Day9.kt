package aoc23

import AOCYear
import mapToInt
import readInput

class Day9 {

    fun List<Int>.nextSequence() = zipWithNext().map { (a, b) -> b - a }

    fun solve() {
        val rawInput = readInput("day9.txt", AOCYear.TwentyThree)

        val allSequences = rawInput.map { line ->
            val baseSequence = line.split(" ").mapToInt()

            val newSequence = generateSequence(baseSequence) { currentSeq ->
                currentSeq
                    .nextSequence()
                    .takeUnless { newSeq -> newSeq.all { it == 0 } }
            }

            newSequence.toList()
        }

        val partOne = allSequences.sumOf { sequences ->
            sequences.foldRight(0L) { seq, acc -> acc + seq.last() }
        }

        val partTwo = allSequences.sumOf { sequence ->
            sequence.foldRight(0L) { seq, acc -> seq.first() - acc }
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
