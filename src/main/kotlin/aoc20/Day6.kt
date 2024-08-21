package aoc20

import AOCYear
import readInput
import splitBy

class Day6 {

    fun solve() {
        val rawInput = readInput("day6.txt", AOCYear.Twenty)

        val answersPerGroup = rawInput.splitBy({ isEmpty() }, { it.toList() })

        val partOne = answersPerGroup.sumOf { groupAnswers -> groupAnswers.flatten().toSet().size }

        val partTwo = answersPerGroup.sumOf { groupAnswers ->
            groupAnswers.flatten()
                .groupingBy { it }
                .eachCount()
                .count { (_, answerCount) -> answerCount == groupAnswers.size }
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
