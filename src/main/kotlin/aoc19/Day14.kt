@file:Suppress("MemberVisibilityCanBePrivate")

package aoc19

import AOCAnswer
import AOCSolution
import AOCYear
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import readInput
import kotlin.math.ceil

class Day14 : AOCSolution {

    data class Chemical(val index: Int, val amount: Int)

    fun String.parseToChemical() = split(' ').let { (amount, item) -> Pair(item, amount.toInt()) }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day14.txt", AOCYear.Nineteen)

        val pairs = rawInput.map { line ->
            val (inputPart, outputPart) = line.split(" => ")

            outputPart.parseToChemical() to inputPart.split(", ").map { it.parseToChemical() }
        }

        val chemicalNameToIndex = pairs
            .flatMap { (output, inputs) -> inputs.map { (name) -> name }.plusElement(output.first) }
            .distinct()
            .mapIndexed { index, name -> name to index }
            .toMap()

        val indexToPurchaseOptions = pairs.associate { (output, inputs) ->
            val (outputName, outputAmount) = output

            val outputIndex = chemicalNameToIndex.getValue(outputName)

            val inputChemicals = inputs.map { (name, amount) ->
                Chemical(chemicalNameToIndex.getValue(name), amount)
            }

            val outputChemical = Chemical(outputIndex, outputAmount)

            outputChemical.index to Pair(outputChemical, inputChemicals)
        }

        val oreIndex = chemicalNameToIndex.getValue("ORE")
        val fuelIndex = chemicalNameToIndex.getValue("FUEL")

        val initialState = List(chemicalNameToIndex.size) { 0L }.toPersistentList()

        val finalState = purchaseFuel(initialState, indexToPurchaseOptions, oreIndex, fuelIndex, 1)
        val oreCostForOneFuel = finalState[oreIndex]

        val oreTarget = 1000000000000L
        // The initial oreCostForOneFuel is the upper limit for the cost as it starts with no resources
        val fuelEstimate = oreTarget / oreCostForOneFuel

        val oreTargetDouble = oreTarget.toDouble()

        val findMaxFuelSequence = generateSequence(fuelEstimate) { currentFuel ->
            val result = purchaseFuel(initialState, indexToPurchaseOptions, oreIndex, fuelIndex, currentFuel)
            val oreResult = result[oreIndex]

            val factor = oreTargetDouble / oreResult
            val newFuel = (currentFuel * factor).toLong()

            newFuel.takeIf { it != currentFuel }
        }

        val partOne = oreCostForOneFuel
        val partTwo = findMaxFuelSequence.last()

        return AOCAnswer(partOne, partTwo)
    }

    fun purchaseFuel(
        initialState: PersistentList<Long>,
        purchaseOptions: Map<Int, Pair<Chemical, List<Chemical>>>,
        oreIndex: Int,
        fuelIndex: Int,
        fuelAmount: Long,
    ): PersistentList<Long> {
        fun executeSearch(
            chemicals: List<Chemical>,
            state: PersistentList<Long>,
            multiplier: Long,
        ): PersistentList<Long> = chemicals.fold(state) { currentState, target ->
            val targetAmount = target.amount * multiplier
            val currentAmount = currentState[target.index]

            val neededTargetAmount = targetAmount - currentAmount

            val (targetOutput, targetInputs) = purchaseOptions.getValue(target.index)
            val firstInput = targetInputs.first()

            val resultState = when {
                neededTargetAmount <= 0 -> currentState
                firstInput.index == oreIndex -> {
                    val purchaseAmount = ceil(neededTargetAmount / targetOutput.amount.toDouble()).toLong()

                    val unitOreCost = firstInput.amount
                    val oreCost = purchaseAmount * unitOreCost
                    val purchasedAmount = purchaseAmount * targetOutput.amount

                    currentState.set(oreIndex, currentState[oreIndex] + oreCost)
                        .set(target.index, currentAmount + purchasedAmount)
                }
                else -> {
                    val newMultiplier = ceil(neededTargetAmount / targetOutput.amount.toDouble()).toLong()
                    val produced = targetOutput.amount * newMultiplier

                    executeSearch(targetInputs, currentState, newMultiplier).set(target.index, currentAmount + produced)
                }
            }

            resultState.set(target.index, resultState[target.index] - targetAmount)
        }

        val (_, fuelInputs) = purchaseOptions.getValue(fuelIndex)

        return executeSearch(fuelInputs, initialState, fuelAmount)
    }
}
