@file:Suppress("MemberVisibilityCanBePrivate")

package aoc21

import AOCYear
import Position
import plus
import readInput
import kotlin.math.abs
import kotlin.math.min

class Day23 {
    val up = -1 to 0
    val down = 1 to 0
    val left = 0 to -1
    val right = 0 to 1

    val directions = listOf(up, down, left, right)

    enum class Type {
        A,
        B,
        C,
        D,
    }

    fun typeToEnergyCost(type: Type) = when (type) {
        Type.A -> 1
        Type.B -> 10
        Type.C -> 100
        Type.D -> 1000
    }

    fun solve() {
        val rawInput = readInput("day23.txt", AOCYear.TwentyOne)

        val allPositions = rawInput.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, char ->
                Pair(Position(rowI, colI), char).takeIf { char == '.' || char.isLetter() }
            }
        }.toMap()

        val partOne = minimumMovement(allPositions)

        val partTwoInsert = listOf("  #D#C#B#A#", "  #D#B#A#C#")
        val insertAt = 3

        val inputPartTwo = rawInput.run {
            take(insertAt).plus(partTwoInsert).plus(takeLast(size - insertAt))
        }

        val partTwoPositions = inputPartTwo.flatMapIndexed { rowI, row ->
            row.mapIndexedNotNull { colI, char ->
                Pair(Position(rowI, colI), char).takeIf { char == '.' || char.isLetter() }
            }
        }.toMap()

        val partTwo = minimumMovement(partTwoPositions)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun minimumMovement(inputPositions: Map<Position, Char>): Long {
        val housePositions = inputPositions
            .filter { (_, value) -> value.isLetter() }
            .keys
            .sortedWith(compareBy<Position> { it.second }.thenBy { it.first })
            .toSet()

        val housesPerGroup = housePositions.size / 4

        val housePositionGroups = housePositions.windowed(housesPerGroup, housesPerGroup)

        val forbiddenToStop = housePositionGroups
            .map { group -> group.minBy { (row, _) -> row } + up }
            .toSet()

        val (aHouse, bHouse, cHouse, dHouse) = housePositionGroups

        fun typeToHouse(type: Type) = when (type) {
            Type.D -> dHouse
            Type.C -> cHouse
            Type.B -> bHouse
            Type.A -> aHouse
        }

        fun Map<Position, Type>.areSorted() = Type.entries.all { type -> typeToHouse(type).all { this[it] == type } }

        fun hasChanceOfBeatingBest(energySpent: Long, best: Long, positionToType: Map<Position, Type>): Boolean {
            if (energySpent >= best) return false

            val typeToCurrentPos = positionToType.entries.groupBy({ it.value }, { it.key })

            fun calculateMinDistance(type: Type): Int {
                val houses = typeToHouse(type)
                val positions = typeToCurrentPos.getValue(type).toMutableList()

                val distance = houses.sumOf { house ->
                    if (positionToType[house] == type) {
                        positions.remove(house)
                        return@sumOf 0
                    }

                    val pairWith = positions.removeLast()

                    if (pairWith.isTopRow()) {
                        manhattanDistance(house, pairWith)
                    } else {
                        val cols = abs(pairWith.second - house.second)
                        val rows = pairWith.first + house.first - 2
                        rows + cols
                    }
                }

                return distance * typeToEnergyCost(type)
            }

            val sum = Type.entries.sumOf { calculateMinDistance(it) }

            return energySpent + sum < best
        }

        fun isInFinalPosition(
            homeHouses: List<Position>,
            position: Position,
            positions: Map<Position, Type>,
            type: Type,
        ): Boolean {
            val index = homeHouses.indexOf(position)

            return index != -1 && homeHouses.subList(index + 1, housesPerGroup).all { positions[it] == type }
        }

        fun mapAllMoves(
            initialPosition: Position,
            type: Type,
            positionToType: Map<Position, Type>,
            totalEnergySpent: Long,
        ): List<Pair<Position, Long>> {
            val homeHouses = typeToHouse(type)

            if (isInFinalPosition(homeHouses, initialPosition, positionToType, type)) return emptyList()

            val queue = ArrayDeque(listOf(initialPosition to totalEnergySpent))
            val seen = mutableSetOf<Position>()

            return buildList {
                while (queue.isNotEmpty()) {
                    val (position, energySpent) = queue.removeFirst()

                    if (!seen.add(position)) continue

                    val canEnterTopRow = position != initialPosition &&
                        initialPosition in housePositions && position !in housePositions && position !in forbiddenToStop

                    if (canEnterTopRow || isInFinalPosition(homeHouses, position, positionToType, type)) {
                        add(position to energySpent)
                    }

                    val energyCost = typeToEnergyCost(type)

                    directions.forEach { direction ->
                        val newPosition = position + direction

                        if (newPosition in inputPositions && newPosition !in positionToType) {
                            queue.addLast(newPosition to energyCost + energySpent)
                        }
                    }
                }
            }
        }

        fun minimumMovement(initialPositions: Map<Position, Type>, energySpent: Long): Long {
            val states = ArrayDeque(listOf(initialPositions to energySpent))
            var best = Long.MAX_VALUE

            val seenStates = hashMapOf<Map<Position, Type>, Long>()

            while (states.isNotEmpty()) {
                val state = states.removeFirst()
                val (positions, score) = state

                when {
                    !hasChanceOfBeatingBest(score, best, positions) -> continue
                    positions.areSorted() -> {
                        best = min(best, score)
                        continue
                    }
                }

                val cached = seenStates[positions]

                if (cached != null && cached <= score) {
                    continue
                } else {
                    seenStates[positions] = score
                }

                positions.forEach { (position, type) ->
                    val allMoves = mapAllMoves(position, type, positions, score)
                    val baseMap = positions - position

                    allMoves.forEach { (moveTo, energySpent) ->
                        val newState = baseMap + (moveTo to type)

                        states.addLast(newState to energySpent)
                    }
                }
            }

            return best
        }

        val initialPositions = inputPositions
            .filter { (_, char) -> char.isLetter() }
            .mapValues { (_, rawType) -> Type.valueOf(rawType.toString()) }

        return minimumMovement(initialPositions, 0L)
    }

    fun manhattanDistance(a: Position, b: Position) = abs(a.first - b.first) + abs(a.second - b.second)

    fun Position.isTopRow() = first == 1
}
