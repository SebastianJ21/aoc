@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import product
import readInput
import splitBy
import toCharMatrix
import transposed

private typealias Tile = List<List<Char>>

class Day20 {

    fun solve() {
        val rawInput = readInput("day20.txt", AOCYear.Twenty)

        val idToMatrix = rawInput.splitBy { it.isEmpty() }.associate { lines ->
            val id = lines.first().dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }.toInt()

            val matrix = lines.drop(1).toCharMatrix()

            id to matrix
        }

        val idToBorders = idToMatrix.mapValues { (_, matrix) -> matrix.getAllBorderCombinations() }

        val idToNeighbors = idToBorders.entries.associate { (id, borders) ->

            val matches = idToBorders.filter { (otherId, otherBorders) ->
                id != otherId && borders.any { it in otherBorders }
            }

            id to matches.keys
        }

        val partOne = idToNeighbors.filter { (_, neighbors) -> neighbors.size == 2 }.keys.map { it.toLong() }.product()

        val matchedTiles = findConfiguration(idToNeighbors, idToMatrix)

        val tilesWithoutBorders = matchedTiles.map { tileRow ->
            tileRow.map { matrix ->
                matrix.drop(1).dropLast(1).map { matrixRow ->
                    matrixRow.mapIndexedNotNull { index, value ->
                        if (index == 0 || index == matrixRow.lastIndex) null else value
                    }
                }
            }
        }

        val rowJoined = tilesWithoutBorders.map { row -> joinMatrices(row) }

        val finalMatrix = joinMatrices(rowJoined.map { it.transposed() }).transposed()

        val pattern = listOf("                  # ", "#    ##    ##    ###", " #  #  #  #  #  #   ")

        val matchCount = getAllConfigurations(finalMatrix).maxOf {
            countMatches(it, pattern)
        }

        val hashesPerMatch = pattern.sumOf { row -> row.count { it == '#' } }

        val totalHashCount = finalMatrix.sumOf { row -> row.count { it == '#' } }

        val partTwo = totalHashCount - (matchCount * hashesPerMatch)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun countMatches(tile: Tile, pattern: List<String>): Int {
        val regexPattern = pattern.map {
            Regex(it.replace(' ', '.'))
        }

        val firstRegex = regexPattern.first()
        val restRegex = regexPattern.drop(1)

        val stringMatrix = tile.map { it.joinToString("") }

        val counts = stringMatrix.windowed(pattern.size, 1) { rows ->
            val firstRow = rows.first()
            val restRows = rows.drop(1).zip(restRegex)

            val matchSequence = generateSequence(firstRegex.find(firstRow)) { previousMatch ->
                firstRegex.find(firstRow, previousMatch.range.first + 1)
            }

            matchSequence.count { match ->
                val matchRange = match.range

                restRows.all { (string, regex) -> regex.containsMatchIn(string.substring(matchRange)) }
            }
        }

        return counts.sum()
    }

    fun <T> joinMatrices(matrices: List<List<List<T>>>): List<List<T>> {
        if (matrices.isEmpty()) return emptyList()

        return matrices.drop(1).fold(matrices.first()) { acc, matrix ->
            acc.zip(matrix) { row1, row2 -> row1 + row2 }
        }
    }

    fun getFirstRow(
        id: Int,
        idToNeighborIds: Map<Int, Set<Int>>,
        idToConfigs: Map<Int, Set<Tile>>,
    ): List<Pair<Int, Tile>> {
        val neighborIds = idToNeighborIds.getValue(id)

        val allNeighborBorders = neighborIds.flatMap { neighborId ->

            idToConfigs.getValue(neighborId).flatMap { it.getBorders() }
        }.toSet()

        val configurations = idToConfigs.getValue(id)

        val baseConfig = configurations.first { configuration ->
            val borders = configuration.getBorders()

            borders.first() !in allNeighborBorders && borders[2] !in allNeighborBorders
        }

        return getRowSequence(id to baseConfig, idToNeighborIds, idToConfigs).toList()
    }

    fun getRowSequence(
        cornerPair: Pair<Int, Tile>,
        idToNeighbors: Map<Int, Set<Int>>,
        idToConfigurations: Map<Int, Set<Tile>>,
    ) = generateSequence(cornerPair) { (leftId, leftConfig) ->

        val neighbors = idToNeighbors.getValue(leftId)

        val leftBorderToFind = leftConfig.lastColumn()

        val nextLeft = neighbors.firstNotNullOfOrNull { neighborId ->
            val configs = idToConfigurations.getValue(neighborId)

            configs.singleOrNull { config ->
                config.firstColumn() == leftBorderToFind
            }?.let { neighborId to it }
        }

        nextLeft
    }

    fun findConfiguration(idToNeighbors: Map<Int, Set<Int>>, idToBaseMatrix: Map<Int, Tile>): List<List<Tile>> {
        val idToConfigurations = idToBaseMatrix.mapValues { (_, matrix) -> getAllConfigurations(matrix) }

        // Corner matrix
        val initialId = idToNeighbors.entries.first { (_, neighborIds) -> neighborIds.size == 2 }.key

        val firstRow = getFirstRow(initialId, idToNeighbors, idToConfigurations)

        fun getNextRowOrNull(previousRows: List<List<Pair<Int, Tile>>>): List<Pair<Int, Tile>>? {
            val previousRow = previousRows.last()

            val upCornerId = idToNeighbors.getValue(previousRow.first().first).singleOrNull { id ->
                val previousCornerLeft = previousRow[1].first
                val previousCornerUp = previousRows.getOrNull(previousRows.lastIndex - 1)?.first()?.first

                id != previousCornerLeft && id != previousCornerUp
            }

            if (upCornerId == null) {
                return null
            }

            val upCornerNeighbors = idToNeighbors.getValue(upCornerId).flatMap { id ->
                idToConfigurations.getValue(id).flatMap { it.getBorders() }
            }.toSet()

            val upCornerConfig = idToConfigurations.getValue(upCornerId).single { config ->
                config.first() == previousRow.first().second.last() && config.firstColumn() !in upCornerNeighbors
            }

            val rowSeq = getRowSequence(upCornerId to upCornerConfig, idToNeighbors, idToConfigurations)

            return rowSeq.toList()
        }

        val allRows = generateSequence(listOf(firstRow)) { previousRows ->
            getNextRowOrNull(previousRows)?.let { previousRows.plusElement(it) }
        }.last()

        val finalMatrix = allRows.map { row -> row.map { it.second } }

        return finalMatrix
    }

    fun getAllConfigurations(tile: Tile): Set<Tile> {
        fun Tile.getRotationCombination(): List<Tile> {
            fun Tile.rotate() = transposed().map { it.reversed() }

            return (1..3).runningFold(this) { acc, _ -> acc.rotate() }
        }

        fun Tile.getFlipCombination(): List<Tile> {
            val rowFlip = { tile: Tile -> tile.map { it.reversed() } }
            val colFlip = { tile: Tile -> tile.transposed().map { it.reversed() }.transposed() }

            return listOf(this, rowFlip(this), colFlip(this), rowFlip(colFlip(this)))
        }

        val allCombinations = tile.getRotationCombination().flatMap { rotatedMatrix ->
            rotatedMatrix.getFlipCombination()
        }

        return allCombinations.toSet()
    }

    fun Tile.firstColumn() = map { it.first() }
    fun Tile.lastColumn() = map { it.last() }

    fun Tile.getBorders() = listOf(first(), last(), firstColumn(), lastColumn())

    fun Tile.getAllBorderCombinations(): Set<List<Char>> {
        val firstColumn = firstColumn()
        val lastColumn = lastColumn()

        return setOf(
            first(),
            first().reversed(),
            last(),
            last().reversed(),
            firstColumn,
            firstColumn.reversed(),
            lastColumn,
            lastColumn.reversed(),
        )
    }
}
