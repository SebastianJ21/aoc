package aoc21

import AOCYear
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import readInput
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class OperationType { ON, OFF }

data class Cuboid(
    val x: IntRange,
    val y: IntRange,
    val z: IntRange,
    val id: Long = Random.nextLong(),
)

fun Cuboid.intersect(other: Cuboid) = Cuboid(x.intersect(other.x), y.intersect(other.y), z.intersect(other.z))

fun IntRange.intersects(other: IntRange) = !(last < other.first || first > other.last)

fun IntRange.intersect(other: IntRange) = max(other.first, first)..min(other.last, last)

fun Cuboid.intersects(other: Cuboid) = x.intersects(other.x) && y.intersects(other.y) && z.intersects(other.z)

fun Cuboid.size() =
    abs(x.last - x.first + 1).toLong() * abs(y.last - y.first + 1).toLong() * abs(z.last - z.first + 1).toLong()

fun List<Cuboid>.calculateUniverseCuboid(): Cuboid {
    val xMin = minOf { it.x.first } - 1
    val xMax = maxOf { it.x.last } + 1

    val yMin = minOf { it.y.first } - 1
    val yMax = maxOf { it.y.last } + 1

    val zMin = minOf { it.z.first } - 1
    val zMax = maxOf { it.z.last } + 1

    return Cuboid(xMin..xMax, yMin..yMax, zMin..zMax)
}

fun Cuboid.absoluteComplement(universe: Cuboid): List<Cuboid> {
    val x1 = universe.x.first until x.first
    val x2 = x.last + 1..universe.x.last

    val y1 = universe.y.first until y.first
    val y2 = y.last + 1..universe.y.last

    val z1 = universe.z.first until z.first
    val z2 = z.last + 1..universe.z.last

    return listOf(
        Cuboid(x1, y, z),
        Cuboid(x2, y, z),
        Cuboid(x, y1, z),
        Cuboid(x, y2, z),
        Cuboid(x, y, z1),
        Cuboid(x, y, z2),
        Cuboid(x1, y1, z),
        Cuboid(x1, y2, z),
        Cuboid(x2, y1, z),
        Cuboid(x2, y2, z),
        Cuboid(x1, y, z1),
        Cuboid(x1, y, z2),
        Cuboid(x2, y, z1),
        Cuboid(x2, y, z2),
        Cuboid(x, y1, z1),
        Cuboid(x, y1, z2),
        Cuboid(x, y2, z1),
        Cuboid(x, y2, z2),
        Cuboid(x1, y1, z1),
        Cuboid(x1, y1, z2),
        Cuboid(x1, y2, z1),
        Cuboid(x1, y2, z2),
        Cuboid(x2, y1, z1),
        Cuboid(x2, y1, z2),
        Cuboid(x2, y2, z1),
        Cuboid(x2, y2, z2),
    )
}

fun Cuboid.relativeComplement(absoluteComplement: List<Cuboid>) =
    absoluteComplement.mapNotNull { offComplement ->
        when (offComplement.intersects(this)) {
            true -> offComplement.intersect(this)
            false -> null
        }
    }

class Day22 {
    data class CuboidComposition(val cuboids: Set<Cuboid>) {
        val result = cuboids.reduce { acc, cuboid -> acc.intersect(cuboid) }
    }

    private fun List<Cuboid>.generateIntersections(): Map<Int, List<CuboidComposition>> {
        val maxPairSize = size
        val virtualThreadDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

        tailrec fun generate(
            currentDepth: Int,
            currentPairs: Map<Int, List<CuboidComposition>> = emptyMap(),
        ): Map<Int, List<CuboidComposition>> {
            if (currentDepth == maxPairSize) {
                return currentPairs
            }

            val previous = currentPairs[currentDepth] ?: map { CuboidComposition(setOf(it)) }

            val newComposition = runBlocking(virtualThreadDispatcher) {
                previous.chunked(500).map { cuboidChunk ->
                    async {
                        cuboidChunk.flatMap { composedCuboid ->
                            val childCuboids = composedCuboid.cuboids
                            mapNotNull { cuboid ->
                                if (cuboid !in childCuboids && cuboid.intersects(composedCuboid.result)) {
                                    CuboidComposition(composedCuboid.cuboids + cuboid)
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }.awaitAll().flatten().distinctBy { it.cuboids }
            }

            if (newComposition.isEmpty()) {
                return currentPairs
            }

            val newPairs = currentPairs.plus(currentDepth.inc() to newComposition)

            return generate(currentDepth + 1, newPairs)
        }

        return generate(1)
    }

    fun solve() {
        val rawInput = readInput("day22.txt", AOCYear.TwentyOne)

        val operations = rawInput.map { line ->
            val (operationStr, coordinateStrings) = line.split(" ")

            val operation = OperationType.valueOf(operationStr.uppercase())

            val cuboid = coordinateStrings
                .split(",")
                .map {
                    it.drop(2)
                        .split("..")
                        .let { (from, to) -> from.toInt()..to.toInt() }
                }
                .let { (xRange, yRange, zRange) ->
                    Cuboid(xRange, yRange, zRange)
                }

            operation to cuboid
        }

        val partOneOperations = operations.filter { (_, cuboid) ->
            val constraint = -50..50
            val (x, y, z) = cuboid

            listOf(x.first, x.last, y.first, y.last, z.first, z.last).all { it in constraint }
        }

        val partOne = countCuboidArea(partOneOperations)
        val partTwo = countCuboidArea(operations)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun countCuboidArea(operations: List<Pair<OperationType, Cuboid>>): Long {
        val cuboids = operations.map { (_, cuboid) -> cuboid }

        val universeCuboid = cuboids.calculateUniverseCuboid()

        val cuboidsToCount = operations.fold(listOf<Cuboid>()) { acc, (op, cuboid) ->
            if (op == OperationType.ON) {
                acc + cuboid
            } else {
                val spaceToKeep = cuboid.absoluteComplement(universeCuboid)

                check(spaceToKeep.sumOf { it.size() } == (universeCuboid.size() - cuboid.size())) {
                    "Expected size: ${universeCuboid.size() - cuboid.size()} got: ${spaceToKeep.sumOf { it.size() }}"
                }

                acc.flatMap { previousCuboid ->
                    if (cuboid.intersects(previousCuboid)) {
                        previousCuboid.relativeComplement(spaceToKeep)
                    } else {
                        listOf(previousCuboid)
                    }
                }
            }
        }

        // Counts all overlaps
        val totalSize = cuboidsToCount.sumOf { cuboid -> cuboid.size() }

        // Number of cuboids intersecting at an area -> composition of the intersection
        val intersections = cuboidsToCount.reversed().generateIntersections()

        // Inclusion - Exclusion principle
        val intersectionSum = intersections.entries.sumOf { (density, composition) ->
            val sum = composition.sumOf { it.result.size() }

            if (density % 2 == 0) {
                sum.unaryMinus()
            } else {
                sum
            }
        }

        return totalSize + intersectionSum
    }
}
