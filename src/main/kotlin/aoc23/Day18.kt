package aoc23

import AOCYear
import readInput
import java.util.HexFormat
import kotlin.math.abs
import kotlin.math.absoluteValue

typealias PositionL = Pair<Long, Long>

class Day18 {
    fun solve() {
        val rawInput = readInput("day18.txt", AOCYear.TwentyThree)

        fun extractPoints(
            lineExtractor: (dir: String, value: String, hexCode: String) -> Pair<Long, String>,
        ): List<PositionL> {
            val (points, _) =
                rawInput.fold(listOf<PositionL>() to (0L to 0L)) { (points, nowPosition), line ->
                    val (baseDir, rawValue, hex) = line.split(" ")
                    val (value, dir) = lineExtractor(baseDir, rawValue, hex)

                    val (rowI, colI) = nowPosition

                    val (newRowI, newColI) =
                        when (dir) {
                            "R" -> rowI to colI + value
                            "L" -> rowI to colI - value
                            "U" -> rowI - value to colI
                            "D" -> rowI + value to colI
                            else -> error("")
                        }

                    val newPair = newRowI to newColI

                    (points + newPair) to newPair
                }
            return points
        }

        // Shoelace with longs
        fun polygonArea(points: List<PositionL>): Long =
            points
                .zipWithNext()
                .plus(points.last() to points.first())
                .sumOf { (first, second) ->
                    val (x0, y0) = first
                    val (x1, y1) = second

                    (x0 * y1) - (x1 * y0)
                }.absoluteValue.div(2)

        fun areaWithBoundaryPoints(points: List<PositionL>): Long {
            val area = polygonArea(points)

            val boundaryPoints =
                (listOf(0L to 0L) + points)
                    .zipWithNext()
                    .sumOf { (a, b) -> abs(b.first - a.first) + abs(b.second - a.second) }

            val interior = area - boundaryPoints.div(2) + 1

            return interior + boundaryPoints
        }

        val partOnePoints = extractPoints { dir, value, _ -> value.toLong() to dir }
        val partTwoPoints =
            extractPoints { _, _, hex ->
                val (hexValue, hexDir) = hex.drop(2).dropLast(1).run { take(5) to takeLast(1) }

                HexFormat.fromHexDigits(hexValue).toLong() to
                    when (hexDir) {
                        "0" -> "R"
                        "1" -> "D"
                        "2" -> "L"
                        "3" -> "U"
                        else -> error("")
                    }
            }

        val partOne = areaWithBoundaryPoints(partOnePoints)
        val partTwo = areaWithBoundaryPoints(partTwoPoints)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
