package aoc21

import AOCYear
import contains
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import mapToInt
import readInput
import splitBy
import kotlin.math.abs

typealias Position = Triple<Int, Int, Int>

class Day19 {

    data class BeaconPairing(val first: Position, val second: Position, val scannerIndex: Int) {

        val absoluteAxisDistance = first.let { (a, b, c) ->
            listOf(abs(a - second.first), abs(b - second.second), abs(c - second.third)).sorted()
        }.let { (a, b, c) -> Triple(a, b, c) }
    }

    data class ScannerResolver(val fromIndex: Int, val toIndex: Int, val resolver: (Position) -> Position)

    fun solve() {
        val rawInput = readInput("day19.txt", AOCYear.TwentyOne)

        val inputScanners = rawInput
            .drop(1)
            .filter { it.isNotEmpty() }
            .splitBy { contains("scanner") }
            .map { beacons ->
                beacons.map { beacon ->
                    val (a, b, c) = beacon.split(",").mapToInt()
                    Position(a, b, c)
                }
            }

        val scannerBeaconPairings = inputScanners.mapIndexed { scannerIndex, beacons ->
            beacons.flatMapIndexed { index, beacon ->
                val otherBeacons = beacons.drop(index + 1)

                otherBeacons.map { otherBeacon -> BeaconPairing(beacon, otherBeacon, scannerIndex) }
            }
        }

        val scannerFlow = scannerBeaconPairings.flatMapIndexed { index, beaconPairings ->
            val otherBeaconPairings = scannerBeaconPairings.drop(index + 1)

            otherBeaconPairings.mapNotNull { otherBeacons ->
                val absAxisDistToBeacon = otherBeacons.associateBy { it.absoluteAxisDistance }

                val beaconPairing = beaconPairings
                    .filter { it.absoluteAxisDistance in absAxisDistToBeacon }
                    .associateWith { absAxisDistToBeacon.getValue(it.absoluteAxisDistance) }

                val positionPairing = beaconPairing.findPositionPairings()

                if (positionPairing.size >= 12) {
                    val from = beaconPairing.keys.first { (first, second) ->
                        first in positionPairing && second in positionPairing
                    }

                    val (toFirst, toSecond) = listOf(from.first, from.second).map { positionPairing.getValue(it) }

                    val toScannerIndex = beaconPairing.values.first().scannerIndex

                    from to BeaconPairing(toFirst, toSecond, toScannerIndex)
                } else {
                    null
                }
            }
        }

        val beaconFlow = scannerBeaconPairings.flatMapIndexed { index, beaconPairings ->
            val otherBeaconPairings = scannerBeaconPairings.drop(index + 1)

            otherBeaconPairings.mapNotNull { otherBeacons ->
                val absAxisDistToBeacon = otherBeacons.associateBy { it.absoluteAxisDistance }

                val beaconPairing = beaconPairings
                    .filter { it.absoluteAxisDistance in absAxisDistToBeacon }
                    .associateWith { absAxisDistToBeacon.getValue(it.absoluteAxisDistance) }

                if (beaconPairing.size >= 12) {
                    beaconPairing.findPositionPairings()
                } else {
                    null
                }
            }
        }

        val groups = buildList<PersistentSet<Position>> {
            beaconFlow.forEach { mappings ->
                mappings.forEach { (from, to) ->
                    val existingGroupIndex = indexOfFirst { from in it || to in it }

                    if (existingGroupIndex != -1) {
                        val group = get(existingGroupIndex).add(from).add(to)

                        set(existingGroupIndex, group)
                    } else {
                        add(persistentHashSetOf(from, to))
                    }
                }
            }
        }

        val individualBeacons = inputScanners.flatten().count { beacon -> groups.none { beacon in it } }

        val partOne = individualBeacons + groups.size

        val bidirectionalScanners = scannerFlow + scannerFlow.map { (from, to) -> to to from }

        val resolvers = buildList<ScannerResolver> {
            val baseScanners = scannerFlow.filter { (from, _) -> from.scannerIndex == 0 }
            val scanners = bidirectionalScanners - baseScanners.toSet()

            // Resolvers for Scanner 0
            val baseResolvers = baseScanners.map { (to, from) ->
                ScannerResolver(from.scannerIndex, to.scannerIndex, makeResolver(from, to))
            }

            addAll(baseResolvers)

            while (true) {
                val newResolvers = scanners.filter { (_, to) ->
                    any { it.fromIndex == to.scannerIndex } && none { it.toIndex == to.scannerIndex }
                }.map { (from, to) -> ScannerResolver(from.scannerIndex, to.scannerIndex, makeResolver(from, to)) }

                if (newResolvers.isEmpty()) break

                addAll(newResolvers)
            }
        }

        // (From, To, Distance)
        val relativeScannerDistances = buildList {
            bidirectionalScanners.forEach { (from, to) ->
                val fromScanner = from.scannerIndex
                val toScanner = to.scannerIndex

                val (_, _, resolver) = resolvers.find { (fromIndex, toIndex, _) ->
                    fromIndex == fromScanner && toIndex == toScanner
                } ?: return@forEach

                val resolved = resolver(from.first)
                val distance = minus(to.first, resolved)

                add(Triple(fromScanner, toScanner, distance))
            }
        }

        // Scanner index -> Distance from (0, 0, 0)
        val absoluteScannerDistances = buildMap {
            // Initial absolute position
            put(0, Position(0, 0, 0))

            // Relative distance from (0, 0, 0) == Absolute distance
            relativeScannerDistances.forEach { (from, to, distance) ->
                if (to == 0) {
                    put(from, distance)
                }
            }

            fun getResolverFrom(from: Int) = resolvers.first { it.fromIndex == from }

            while (true) {
                // To convert a relative distance, we need to know the absolute distance of its destination
                val relativeDistance = relativeScannerDistances.firstOrNull { (from, to) ->
                    from !in this && to in this
                }

                if (relativeDistance == null) break

                val (from, to, relativePosition) = relativeDistance

                // Resolve / align to the direction & rotation of the absolute position
                val alignedToAbsolutePosition = generateSequence(to to relativePosition) { (to, position) ->
                    if (to == 0) {
                        null
                    } else {
                        val (_, toIndex, resolver) = getResolverFrom(to)
                        toIndex to resolver(position)
                    }
                }.last().second

                val targetAbsolutePosition = getValue(to)

                val absolutePosition = plus(alignedToAbsolutePosition, targetAbsolutePosition)

                put(from, absolutePosition)
            }
        }

        // Find max manhattan distance between two scanners
        val partTwo = absoluteScannerDistances.values.flatMapIndexed { index, position ->
            val otherPositions = absoluteScannerDistances.values.drop(index + 1)

            otherPositions.map { otherPosition -> distance(position, otherPosition) }
        }.max()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun minus(a: Position, b: Position) = Triple(a.first - b.first, a.second - b.second, a.third - b.third)

    private fun plus(a: Position, b: Position) = Triple(a.first + b.first, a.second + b.second, a.third + b.third)

    private fun distance(a: Position, b: Position) = a.let { (a1, a2, a3) ->
        abs(a1 - b.first) + abs(a2 - b.second) + abs(a3 - b.third)
    }

    private fun makeResolver(from: BeaconPairing, to: BeaconPairing): (Position) -> Position {
        val toPositions = to.first.toList().zip(to.second.toList())
        val fromPositions = from.first.toList().zip(from.second.toList())

        val positionsResolver = toPositions.map { (positionA, positionB) ->
            val distance = abs(positionB - positionA)

            // Find the equivalent pairing index
            val equivalentIndex = fromPositions.indexOfFirst { (a, b) -> abs(b - a) == distance }

            val (eqA, eqB) = fromPositions[equivalentIndex]

            val isApproaching = abs(positionB - positionA.inc()) > distance
            val isToApproaching = abs(eqB - eqA.inc()) > abs(eqB - eqA)

            val func = if (isApproaching == isToApproaching) {
                { position: Int -> position }
            } else {
                { position: Int -> position.unaryMinus() }
            }

            equivalentIndex to func
        }

        val resolver = { position: Position ->
            val positionList = position.toList()
            val (a, b, c) = positionsResolver.map { (index, signFunction) ->
                signFunction(positionList[index])
            }

            Position(a, b, c)
        }

        return resolver
    }

    private fun Map<BeaconPairing, BeaconPairing>.findPositionPairings(): Map<Position, Position> {
        val beaconPositionPairs = map { (pairingA, pairingB) ->
            Pair(pairingA.first, pairingA.second) to Pair(pairingB.first, pairingB.second)
        }.toMap()

        fun findPair(
            toFind: Position,
            currentFromPair: Pair<Position, Position>,
            currentToPair: Pair<Position, Position>,
        ): Pair<Position, Position>? {
            val differentPairingB = beaconPositionPairs.entries.firstOrNull { (from, _) ->
                toFind in from && from != currentFromPair
            }?.value

            if (differentPairingB == null) return null

            val matchingPosition = currentToPair.toList().single { it in differentPairingB }

            return toFind to matchingPosition
        }

        return buildMap {
            beaconPositionPairs.forEach { (fromBeacons, toBeacons) ->
                val (fromFirst, fromSecond) = fromBeacons

                if (fromFirst !in this || fromSecond !in this) {
                    val (from, to) = findPair(fromFirst, fromBeacons, toBeacons)
                        ?: findPair(fromSecond, fromBeacons, toBeacons)
                        ?: return@forEach

                    put(from, to)
                    put(fromBeacons.toList().single { it != from }, toBeacons.toList().single { it != to })
                }
            }
        }
    }
}
