package aoc23

import AOCYear
import mapToLong
import readInput

class Day5 {
    data class MapEntry(val sourceStart: Long, val destinationStart: Long, val rangeSize: Long) {
        val sourceRange = sourceStart..sourceStart + rangeSize
        val destRange = destinationStart..destinationStart + rangeSize

        fun destinationToSource(dest: Long): Long {
            val rangeUsed = dest - destinationStart
            return sourceStart + rangeUsed
        }

        fun sourceToDestination(source: Long): Long {
            val rangeUsed = source - sourceStart
            return destinationStart + rangeUsed
        }
    }

    fun solve() {
        val rawInput = readInput("day5.txt", AOCYear.TwentyThree)

        val seeds = rawInput.first().split(" ").drop(1).mapToLong()

        val fillerLinesBetweenMaps = 2

        val plantMaps = generateSequence(rawInput.drop(1 + fillerLinesBetweenMaps)) { inputLines ->
            val valuesLines = inputLines.takeWhile { it.isNotEmpty() }
            // Drop value lines size + empty line and next map 'headline'
            inputLines.drop(valuesLines.size + fillerLinesBetweenMaps).ifEmpty { null }
        }.map { it.getMap() }.toList()

        val partOne = seeds.minOf { seedVal ->
            plantMaps.fold(seedVal) { currentValue, map ->
                map.find { currentValue in it.sourceRange }
                    ?.sourceToDestination(currentValue) ?: currentValue
            }
        }

        val seedsAsMap = seeds
            .windowed(2, 2)
            .map { (seedStart, range) -> MapEntry(seedStart, seedStart, range) }

        val reversed = plantMaps.reversed()

        val gapFirst = reversed
            .first()
            .minOf { it.destinationStart }
            .takeIf { it != 0L }
            ?.let { MapEntry(0, 0, it) }

        // The First hit in this list is the result
        val destinationMap = reversed
            .first()
            .sortedBy { it.sourceStart }
            .windowed(2, 2)
            .flatMap { (first, second) ->
                val firstEnd = first.sourceStart + first.rangeSize
                val gap = second.sourceStart - firstEnd

                if (gap > 0) {
                    val newStart = firstEnd + 1
                    listOf(first, MapEntry(newStart, newStart + gap, gap), second)
                } else {
                    listOf(first, second)
                }
            }
            // Ignore case where the answer is after all destination values
            .plus(listOfNotNull(gapFirst))
            .sortedBy { it.destinationStart }
            .toList()

        val partTwo = destinationMap.firstNotNullOf { destMapEntry ->
            destMapEntry.destRange.firstNotNullOfOrNull { destValue ->
                val foundSeedSource = reversed.fold(destValue) { current, mapEntries ->
                    val matchingNext = mapEntries.find { current in it.destRange }

                    matchingNext?.destinationToSource(current) ?: current
                }
                if (seedsAsMap.any { foundSeedSource in it.sourceRange }) destValue else null
            }
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun List<String>.getMap() = this
        .takeWhile { it.isNotEmpty() }
        .map { rawEntry ->
            rawEntry.split(" ")
                .mapToLong()
                .apply { check(size == 3) { "Expected map size to be 3. Got $this" } }
                .let { (destinationStart, sourceStart, rangeSize) ->
                    MapEntry(sourceStart, destinationStart, rangeSize)
                }
        }
}
