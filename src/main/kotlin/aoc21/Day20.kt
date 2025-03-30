@file:Suppress("MemberVisibilityCanBePrivate")

package aoc21

import AOCYear
import Position
import applyDirection
import getOrNull
import readInput
import toCharMatrix
import transposed

class Day20 {
    val northWest = -1 to -1
    val north = -1 to 0
    val northEast = -1 to 1
    val west = 0 to -1
    val origin = 0 to 0
    val east = 0 to 1
    val southWest = 1 to -1
    val south = 1 to 0
    val southEast = 1 to 1

    val mooreNeighborhood = listOf(northWest, north, northEast, west, origin, east, southWest, south, southEast)

    fun pixelToValue(pixel: Char) = when (pixel) {
        '#' -> "1"
        '.' -> "0"
        else -> error("Invalid Pixel $pixel")
    }

    fun getNextVoid(current: Char) = when (current) {
        '.' -> '#'
        '#' -> '.'
        else -> error("Invalid Pixel $current")
    }

    fun solve() {
        val rawInput = readInput("day20.txt", AOCYear.TwentyOne)
        val charMatrix = rawInput.toCharMatrix()

        val pixels = charMatrix.first()
        val inputMatrix = charMatrix.drop(2)

        val initialVoidPixel = '.'

        fun performEnhancement(repetitions: Int): List<List<Char>> {
            val voidSequence = generateSequence(initialVoidPixel to 1) { (void, index) ->
                if (index >= repetitions) {
                    null
                } else {
                    getNextVoid(void) to index + 1
                }
            }.map { (voidPixel, _) -> voidPixel }

            val resultMatrix = voidSequence.fold(inputMatrix) { matrix, currentVoid ->
                val bufferedMatrix = matrix.buffered(currentVoid)

                bufferedMatrix.mapIndexed { rowI, row ->
                    List(row.size) { colI ->
                        val pixelIndex = bufferedMatrix.getPixelIndex(rowI to colI, currentVoid)
                        val newPixel = pixels[pixelIndex]
                        newPixel
                    }
                }
            }

            return resultMatrix
        }

        val resultMapPartOne = performEnhancement(2)
        val resultMapPartTwo = performEnhancement(50)

        val partOne = resultMapPartOne.countHashes()
        val partTwo = resultMapPartTwo.countHashes()

        println("Part One: $partOne")
        println("Part Two: $partTwo")
    }

    fun List<List<Char>>.countHashes() = sumOf { row -> row.count { it == '#' } }

    fun List<List<Char>>.getPixelIndex(position: Position, voidPixel: Char): Int {
        val binaryString = mooreNeighborhood.joinToString("") { direction ->
            val pixel = getOrNull(position.applyDirection(direction)) ?: voidPixel

            pixelToValue(pixel)
        }

        return binaryString.toInt(2)
    }

    fun List<List<Char>>.buffered(bufferWith: Char): List<List<Char>> {
        val bufferRow = listOf(List(first().size) { bufferWith })

        val withBufferedRows = bufferRow + this + bufferRow

        val bufferCol = listOf(List(withBufferedRows.size) { bufferWith })

        val buffered = (bufferCol + withBufferedRows.transposed() + bufferCol).transposed()

        return buffered
    }
}
