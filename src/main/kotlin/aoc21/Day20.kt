package aoc21

import AOCAnswer
import AOCSolution
import AOCYear
import Position
import applyDirection
import at
import getOrNull
import readInput
import toCharMatrix
import transposed

class Day20 : AOCSolution {

    private val northWest = -1 at -1
    private val north = -1 at 0
    private val northEast = -1 at 1
    private val west = 0 at -1
    private val origin = 0 at 0
    private val east = 0 at 1
    private val southWest = 1 at -1
    private val south = 1 at 0
    private val southEast = 1 at 1

    private val mooreNeighborhood = listOf(northWest, north, northEast, west, origin, east, southWest, south, southEast)

    private fun pixelToValue(pixel: Char) = when (pixel) {
        '#' -> "1"
        '.' -> "0"
        else -> error("Invalid Pixel $pixel")
    }

    private fun getNextVoid(current: Char) = when (current) {
        '.' -> '#'
        '#' -> '.'
        else -> error("Invalid Pixel $current")
    }

    override fun solve(): AOCAnswer {
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
                        val pixelIndex = bufferedMatrix.getPixelIndex(rowI at colI, currentVoid)
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

        return AOCAnswer(partOne, partTwo)
    }

    private fun List<List<Char>>.countHashes() = sumOf { row -> row.count { it == '#' } }

    private fun List<List<Char>>.getPixelIndex(position: Position, voidPixel: Char): Int {
        val binaryString = mooreNeighborhood.joinToString("") { direction ->
            val pixel = getOrNull(position.applyDirection(direction)) ?: voidPixel

            pixelToValue(pixel)
        }

        return binaryString.toInt(2)
    }

    private fun List<List<Char>>.buffered(bufferWith: Char): List<List<Char>> {
        val bufferRow = listOf(List(first().size) { bufferWith })

        val withBufferedRows = bufferRow + this + bufferRow

        val bufferCol = listOf(List(withBufferedRows.size) { bufferWith })

        val buffered = (bufferCol + withBufferedRows.transposed() + bufferCol).transposed()

        return buffered
    }
}
