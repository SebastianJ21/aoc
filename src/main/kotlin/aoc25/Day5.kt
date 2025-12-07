package aoc25

import AOCAnswer
import AOCSolution
import mapToLong
import readInput
import splitBy

class Day5 : AOCSolution {

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day5.txt", AOCYear.TwentyFive)

        val (rangesLines, idsLines) = rawInput.splitBy { it.isEmpty() }

        val ranges = rangesLines.map { line ->
            // 336566098811319-337408802399429
            val (from, to) = line.split("-").mapToLong()

            from..to
        }

        val ids = idsLines.mapToLong()

        val partOne = ids.count { id -> ranges.any { id in it } }

        val disjointRangesSequence = generateSequence(ranges to emptyList<LongRange>()) { (ranges, collected) ->
            val range = ranges.firstOrNull() ?: return@generateSequence null
            // Removes this range from other ranges
            val newRanges = ranges.drop(1).flatMap { it.minus(range) }

            newRanges to collected.plusElement(range)
        }.map { (_, collectedDisjoinRanges) -> collectedDisjoinRanges }

        val disjointRanges = disjointRangesSequence.last()

        val partTwo = disjointRanges.sumOf { it.size() }

        return AOCAnswer(partOne, partTwo)
    }

    private fun LongRange.size() = last - first + 1

    private fun LongRange.intersects(other: LongRange) = !(last < other.first || first > other.last)

    private fun LongRange.minus(other: LongRange): List<LongRange> = when {
        // No overlap
        !this.intersects(other) -> listOf(this)
        // Full overlap
        other.first <= this.first && other.last >= this.last -> emptyList()
        // Left overlap
        other.first <= this.first && other.last < this.last -> listOf(other.last.inc()..this.last)
        // Right overlap
        other.first > this.first && other.last >= this.last -> listOf(this.first until other.first)
        // Inner overlap
        else -> listOf(this.first until other.first, other.last.inc()..this.last)
    }
}
