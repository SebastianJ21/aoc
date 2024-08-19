@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import mapToLong
import readInput

class Day25 {

    fun solve() {
        val rawInput = readInput("day25.txt", AOCYear.Twenty)

        val (cardKey, doorKey) = rawInput.mapToLong()

        val loopSizeSequence = getLoopSizeSequence(7L).withIndex()

        val cardLoopSize = loopSizeSequence.indexOfFirst { it.value == cardKey }
        val doorLoopSize = loopSizeSequence.indexOfFirst { it.value == doorKey }

        val (key, loopSize) =
            listOf(cardKey to doorLoopSize, doorKey to cardLoopSize).minBy { (_, loopSize) -> loopSize }

        val encryptionKey = getLoopSizeSequence(key).withIndex().first { it.index == loopSize }.value

        println("Part one: $encryptionKey")
    }

    fun getLoopSizeSequence(subjectNumber: Long) = generateSequence(1L) { value ->
        (value * subjectNumber) % 20201227L
    }
}
