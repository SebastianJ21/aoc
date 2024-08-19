@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import invertListMap
import readInput

class Day24 {

    val directionToVector = mapOf(
        "w" to Triple(-1, 0, 1),
        "e" to Triple(1, 0, -1),
        "se" to Triple(0, 1, -1),
        "ne" to Triple(1, -1, 0),
        "sw" to Triple(-1, 1, 0),
        "nw" to Triple(0, -1, 1),
    )

    fun solve() {
        val rawInput = readInput("day24.txt", AOCYear.Twenty)

        val rawDirections = rawInput.map { line ->
            line.fold(listOf<String>() to "") { (collected, carry), char ->
                val directionString = carry + char

                val match = directionToVector.keys.firstOrNull { it == directionString }

                if (match != null) {
                    collected + match to ""
                } else {
                    collected to directionString
                }
            }.first
        }

        val tilePositions = rawDirections.map { tileDirections ->
            tileDirections.map { directionToVector.getValue(it) }.fold(Triple(0, 0, 0)) { position, direction ->
                position.applyDirection(direction)
            }
        }

        val tileGroups = tilePositions.groupingBy { it }.eachCount()

        val (whiteTiles, blackTiles) = tileGroups
            .entries
            .partition { (_, groupCount) -> groupCount % 2 == 0 }
            .toList()
            .map { group -> group.map { (position) -> position } }

        val partOne = tilePositions
            .groupingBy { it }
            .eachCount()
            .count { (_, groupCount) -> groupCount % 2 != 0 }

        val (_, finalBlackTiles) = (1..100).fold(whiteTiles to blackTiles.toSet()) { (white, black), _ ->
            flipTiles(white, black)
        }

        val partTwo = finalBlackTiles.size

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun flipTiles(
        whiteTiles: List<Triple<Int, Int, Int>>,
        blackTiles: Set<Triple<Int, Int, Int>>,
    ): Pair<List<Triple<Int, Int, Int>>, Set<Triple<Int, Int, Int>>> {
        val blackTileToNeighbors = blackTiles.associateWith { tile ->
            directionToVector.values.map { tile.applyDirection(it) }
        }

        val newBlack = invertListMap(blackTileToNeighbors).filter { (_, neighbors) -> neighbors.size == 2 }.keys

        val (flippingToWhite, stayingBlack) = blackTiles.partition { tile ->
            val count = blackTileToNeighbors.getValue(tile).count { it in blackTiles }

            count == 0 || count > 2
        }

        val (flippingToBlack, stayingWhite) = whiteTiles.partition { tile ->
            val count = directionToVector.values.count { direction ->
                tile.applyDirection(direction) in blackTiles
            }

            count == 2
        }

        return flippingToWhite + stayingWhite to (flippingToBlack + stayingBlack).toSet() + newBlack
    }

    fun Triple<Int, Int, Int>.applyDirection(direction: Triple<Int, Int, Int>) = direction.let { (a, b, c) ->
        Triple(first + a, second + b, third + c)
    }
}
