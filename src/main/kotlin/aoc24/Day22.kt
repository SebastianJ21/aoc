package aoc24

import AOCAnswer
import AOCSolution
import inputLines
import mapToLong

class Day22 : AOCSolution {

    override fun solve(): AOCAnswer {
        val inputLines = inputLines().mapToLong()

        val secretNumberSequences = inputLines.map { (1..2000).runningFold(it) { acc, _ -> nextNumber(acc) } }

        val partOne = secretNumberSequences.sumOf { secretNumbers -> secretNumbers.last() }

        // 18 is the max number we can encounter, as we are only working with
        // 1 digits numbers (-9..9) offset by 9 -> (0..18)
        val size = index4d(18, 18, 18, 18) + 1

        val data = IntArray(size)

        secretNumberSequences.forEach { numberSequence ->
            val seen = BooleanArray(size)

            val lastNumberSequence = numberSequence.map { it.last().toInt() }

            lastNumberSequence
                // Subtract & offset to a positive number (e.g., a=9, b=0 -> 0 - 9 + 9 = -9 + 9 -> 0)
                .zipWithNext { a, b -> b - a + 9 }
                .windowed(4) { (a, b, c, d) -> index4d(a, b, c, d) }
                .zip(lastNumberSequence.subList(4, lastNumberSequence.size))
                .forEach { (key, value) ->
                    if (seen[key]) return@forEach

                    seen[key] = true
                    data[key] += value
                }
        }

        val partTwo = data.max()

        return AOCAnswer(partOne, partTwo)
    }

    private fun nextNumber(value: Long): Long {
        val first = value xor (value * 64) % 16777216
        val second = first xor (first / 32) % 16777216
        val third = second xor (second * 2048) % 16777216

        return third
    }

    private fun index4d(a: Int, b: Int, c: Int, d: Int) = (a * 19 * 19 * 19) + (b * 19 * 19) + (c * 19) + d

    private fun Long.last() = this - (this / 10 * 10)
}
