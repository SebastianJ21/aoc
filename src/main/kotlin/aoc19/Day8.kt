package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import readInput

class Day8 : AOCSolution {

    private val imageDimensions = 25 to 6

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day8.txt", AOCYear.Nineteen)

        val (width, height) = imageDimensions

        val pixelLayers = rawInput.single().map { it.digitToInt() }.chunked(width).chunked(height)

        val layerPixelCount = pixelLayers.map { layer -> layer.flatten().groupingBy { it }.eachCount() }
        val minLayer = layerPixelCount.minBy { it[0] ?: 0 }

        val partOne = minLayer.getValue(1) * minLayer.getValue(2)

        val finalLayer = pixelLayers.first().mapIndexed { rowI, row ->
            List(row.size) { colI ->
                pixelLayers.firstNotNullOf { layer ->
                    layer[rowI][colI].takeIf { it != 2 }
                }
            }
        }

        val partTwo = "\n" + finalLayer.joinToString("\n") { row ->
            row.joinToString("") { if (it == 1) "#" else " " }
        }

        return AOCAnswer(partOne, partTwo)
    }
}
