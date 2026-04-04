package aoc25

import AOCAnswer
import AOCSolution
import Position
import applyDirection
import at
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import positionsOf
import readInput

class Day7 : AOCSolution {

    private val up = -1 at 0
    private val down = 1 at 0
    private val left = 0 at -1
    private val right = 0 at 1

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day7.txt", AOCYear.TwentyFive)
        val lines = rawInput.map { it.toList() }

        val splitterPositions = lines.positionsOf { it == '^' }.toSet()
        val start = lines.positionsOf { it == 'S' }.first()
        val rows = lines.size

        val initialActivePosition = listOf(start)
        val splitSequence = generateSequence(initialActivePosition) { activePositions ->
            val (splitPositions, movedPositions) = activePositions
                .map { it.applyDirection(down) }
                .filter { (rowI) -> rowI < rows }
                .partition { it in splitterPositions }

            val newSplitLeft = splitPositions.map { it.applyDirection(left) }
            val newSplitRight = splitPositions.map { it.applyDirection(right) }
            val newSplitPositions = newSplitLeft.plus(newSplitRight).distinct()

            newSplitPositions + movedPositions
        }

        val hitSplitters = splitSequence
            .takeWhile { it.isNotEmpty() }
            .flatten()
            .map { it.applyDirection(down) }
            .filter { it in splitterPositions }
            .toSet()

        val partOne = hitSplitters.count()

        val rowSize = lines.first().size
        // Create an imaginary last row of splitters which we will use to capture all beams
        val extraLastRow = List(rowSize) { rows at it }

        val splitterToConnected = hitSplitters.plus(extraLastRow).associateWith { startPosition ->
            // 'Shoot' a beam from the splitter upwards and collect all splitters that lead to hitting it
            generateSequence(startPosition.applyDirection(up)) { it.applyDirection(up) }
                .takeWhile { position -> position !in hitSplitters && position.first > 0 }
                .flatMap { position ->
                    listOf(position.applyDirection(left), position.applyDirection(right))
                        .filter { it in hitSplitters }
                }
                .toList()
        }

        val firstSplitter = hitSplitters.minBy { (rowI) -> rowI }

        // Reachability = In how many ways can the splitter be reached from the origin
        fun buildSplitterToReachability(
            position: Position,
            currentMap: PersistentMap<Position, Long>,
        ): PersistentMap<Position, Long> {
            if (position in currentMap) return currentMap
            // The only splitter reachable from origin position
            if (position == firstSplitter) return currentMap.put(position, 1)

            val connections = splitterToConnected.getValue(position)

            val newMap = connections.fold(currentMap) { accMap, it -> buildSplitterToReachability(it, accMap) }
            val positionReachability = connections.sumOf { newMap.getValue(it) }

            return newMap.put(position, positionReachability)
        }

        val splitterToReachability = extraLastRow.fold(persistentHashMapOf<Position, Long>()) { accMap, position ->
            buildSplitterToReachability(position, accMap)
        }

        // If any beam terminated, it must hit extraLastRow
        val partTwo = extraLastRow.sumOf { splitterToReachability.getValue(it) }

        return AOCAnswer(partOne, partTwo)
    }
}
