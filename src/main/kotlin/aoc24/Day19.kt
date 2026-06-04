package aoc24

import AOCAnswer
import AOCSolution
import inputLines
import splitBy

class Day19 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines()

        val (partsInput, targetsInput) = inputLines.splitBy { it.isEmpty() }

        val parts = partsInput.single().split(", ")

        val partOne = targetsInput.count { target -> canCompose(target, parts) }
        val partTwo = targetsInput.sumOf { target -> combinations(target, parts) }

        return AOCAnswer(partOne, partTwo)
    }

    private fun canCompose(target: String, parts: List<String>): Boolean {
        if (target.isEmpty()) return true

        return parts.any { target.startsWith(it) && canCompose(target.removePrefix(it), parts) }
    }

    private fun combinations(target: String, parts: List<String>): Long {
        val cache = hashMapOf<String, Long>()

        fun count(remaining: String): Long {
            if (remaining.isEmpty()) return 1

            cache[remaining]?.let { return it }

            val result = parts
                .filter { remaining.startsWith(it) }
                .sumOf { count(remaining.removePrefix(it)) }

            cache[remaining] = result
            return result
        }

        return count(target)
    }
}
