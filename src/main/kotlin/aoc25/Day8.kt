package aoc25

import AOCAnswer
import AOCSolution
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.collections.immutable.toPersistentList
import mapToInt
import product
import readInput
import kotlin.math.pow
import kotlin.math.sqrt

class Day8 : AOCSolution {

    private data class Position(val x: Int, val y: Int, val z: Int) {
        val xDouble = x.toDouble()
        val yDouble = y.toDouble()
        val zDouble = z.toDouble()
    }

    private data class PositionPair(val a: Position, val b: Position, val distance: Double)

    private data class State(
        val sortedDescPairs: PersistentList<PositionPair>,
        val pile: PersistentSet<Position>,
        val circuits: List<PersistentSet<Position>>,
    )

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day8.txt", AOCYear.TwentyFive)

        val positions = rawInput.map { line ->
            val (x, y, z) = line.split(",").mapToInt()

            Position(x, y, z)
        }

        val sortedDescPairs = positions
            .flatMapIndexed { index, positionA ->
                positions
                    .drop(index + 1)
                    .map { positionB -> PositionPair(positionA, positionB, distance(positionA, positionB)) }
            }
            .sortedByDescending { pair -> pair.distance }
            .toPersistentList()

        val initialState = State(
            pile = positions.toPersistentHashSet(),
            circuits = emptyList(),
            sortedDescPairs = sortedDescPairs,
        )
        val stateSequence = generateSequence(initialState) { state ->
            // Use `last` as it is a O(1) for PersistentList on both `get` and `removeAt`
            val (positionA, positionB) = state.sortedDescPairs.last()
            val newPairs = state.sortedDescPairs.removeAt(state.sortedDescPairs.lastIndex)

            when {
                positionA in state.pile && positionB in state.pile -> {
                    val newPile = state.pile.removeAll(listOf(positionA, positionB))
                    val newCircuit = persistentHashSetOf(positionA, positionB)

                    State(sortedDescPairs = newPairs, pile = newPile, circuits = state.circuits.plusElement(newCircuit))
                }
                positionA in state.pile -> {
                    val newPile = state.pile.remove(positionA)
                    val newCircuits = state.circuits.map { circuit ->
                        if (positionB in circuit) circuit.add(positionA) else circuit
                    }

                    State(sortedDescPairs = newPairs, pile = newPile, circuits = newCircuits)
                }
                positionB in state.pile -> {
                    val newPile = state.pile.remove(positionB)

                    val newCircuits = state.circuits.map { circuit ->
                        if (positionA in circuit) circuit.add(positionB) else circuit
                    }

                    State(sortedDescPairs = newPairs, pile = newPile, circuits = newCircuits)
                }
                else -> {
                    val circuitAIndex = state.circuits.indexOfFirst { circuit -> positionA in circuit }
                    val circuitBIndex = state.circuits.indexOfFirst { circuit -> positionB in circuit }

                    // Already connected
                    if (circuitAIndex == circuitBIndex) {
                        state.copy(sortedDescPairs = newPairs)
                    } else {
                        val connectedCircuit = state.circuits[circuitAIndex].plus(state.circuits[circuitBIndex])

                        val newCircuits = state.circuits
                            .filter { circuit -> positionA !in circuit && positionB !in circuit }
                            .plusElement(connectedCircuit)

                        State(sortedDescPairs = newPairs, pile = state.pile, circuits = newCircuits)
                    }
                }
            }
        }

        val partOne = stateSequence
            .drop(1) // Drop initial state
            .take(1000)
            .last()
            .circuits
            .map { it.size }
            .sortedDescending()
            .take(3)
            .product()

        val partTwo = stateSequence
            .zipWithNext()
            // First transition where all circuits are connected in one
            .first { (_, nextState) ->
                nextState.circuits.size == 1 && nextState.pile.all { position -> position in nextState.circuits[0] }
            }
            .first // The state that causes the transition
            .sortedDescPairs
            .last() // The pair that will cause the completion
            .let { (positionA, positionB) -> positionA.x.toLong() * positionB.x.toLong() }

        return AOCAnswer(partOne, partTwo)
    }

    private fun distance(a: Position, b: Position): Double {
        val x = (a.xDouble - b.xDouble).pow(2.0)
        val y = (a.yDouble - b.yDouble).pow(2.0)
        val z = (a.zDouble - b.zDouble).pow(2.0)

        return sqrt(x + y + z)
    }
}
