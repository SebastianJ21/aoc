@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import product
import readInput

class Day19 {

    data class RobotCost(
        val ore: Int = 0,
        val clay: Int = 0,
        val obsidian: Int = 0,
    )

    data class Blueprint(
        val id: Int,
        val oreRobotCost: RobotCost,
        val clayRobotCost: RobotCost,
        val obsidianRobotCost: RobotCost,
        val geodeRobotCost: RobotCost,
    )

    data class SimulationState(
        val minute: Int,
        val ore: Int,
        val clay: Int,
        val obsidian: Int,
        val geodes: Int,
        val oreRobots: Int,
        val clayRobots: Int,
        val obsidianRobots: Int,
        val geodeRobots: Int,
    )

    fun RobotCost.canBePurchased(state: SimulationState) =
        state.ore >= ore && state.clay >= clay && state.obsidian >= obsidian

    fun solve() {
        val rawInput = readInput("day19.txt")
        val blueprints = extractBlueprints(rawInput)

        val partOne = blueprints.sumOf { calculateBlueprintValue(it, 25) * it.id }
        val partTwo = blueprints.take(3).map { calculateBlueprintValue(it, 33) }.product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun extractBlueprints(rawInput: List<String>): List<Blueprint> {
        val normalizedInput = rawInput.map { it.replace(".", "").replace(":", "") }
        val blueprintValues = normalizedInput.map { line -> line.split(" ").mapNotNull { it.toIntOrNull() } }

        return blueprintValues.map {
            check(it.size == 7)

            Blueprint(
                id = it[0],
                oreRobotCost = RobotCost(ore = it[1]),
                clayRobotCost = RobotCost(ore = it[2]),
                obsidianRobotCost = RobotCost(ore = it[3], clay = it[4]),
                geodeRobotCost = RobotCost(ore = it[5], obsidian = it[6]),
            )
        }
    }

    // TODO: Too much mutability
    private fun calculateBlueprintValue(blueprint: Blueprint, timeLimit: Int): Int {
        val cache = hashSetOf<Int>()

        val obsidianMaxSpend = blueprint.run {
            maxOf(geodeRobotCost.obsidian, obsidianRobotCost.obsidian, clayRobotCost.obsidian, oreRobotCost.obsidian)
        }
        val clayMaxSpend = blueprint.run {
            maxOf(geodeRobotCost.clay, obsidianRobotCost.clay, clayRobotCost.clay, oreRobotCost.clay)
        }
        val oreMaxSpend = blueprint.run {
            maxOf(geodeRobotCost.ore, obsidianRobotCost.ore, clayRobotCost.ore, oreRobotCost.ore)
        }

        fun SimulationState.geodePotential(canBuyGeodeRobot: Boolean): Int {
            val timeLeft = timeLimit - minute

            val possiblePurchases = if (canBuyGeodeRobot || timeLeft == 0) timeLeft else timeLeft - 1

            val potential = geodes + (geodeRobots + possiblePurchases) * timeLeft
            return potential
        }

        val bestPotentials = buildMap {
            fun calculateFromState(state: SimulationState) {
                val canBuyGeodeRobot = blueprint.geodeRobotCost.canBePurchased(state)
                val potential = state.geodePotential(canBuyGeodeRobot)

                if ((this[state.minute] ?: 0) > potential) {
                    return
                } else {
                    this[state.minute] = potential
                }

                when {
                    state.minute == timeLimit -> return
                    !cache.add(state.hashCode()) -> return
                }

                val nextState = state.collectRocks()

                if (canBuyGeodeRobot) {
                    calculateFromState(nextState.buyGeodeRobot(blueprint))
                }

                if (state.obsidianRobots < obsidianMaxSpend && blueprint.obsidianRobotCost.canBePurchased(state)) {
                    calculateFromState(nextState.buyObsidianRobot(blueprint))
                }

                if (state.clayRobots < clayMaxSpend && blueprint.clayRobotCost.canBePurchased(state)) {
                    calculateFromState(nextState.buyClayRobot(blueprint))
                }

                if (state.oreRobots < oreMaxSpend && blueprint.oreRobotCost.canBePurchased(state)) {
                    calculateFromState(nextState.buyOreRobot(blueprint))
                }

                if (!canBuyGeodeRobot) {
                    calculateFromState(nextState)
                }
            }

            val initState = SimulationState(1, 0, 0, 0, 0, 1, 0, 0, 0)
            calculateFromState(initState)
        }

        return bestPotentials[timeLimit] ?: 0
    }

    private fun SimulationState.buyOreRobot(blueprint: Blueprint) =
        copy(oreRobots = oreRobots + 1, ore = ore - blueprint.oreRobotCost.ore)

    private fun SimulationState.buyClayRobot(blueprint: Blueprint) =
        copy(clayRobots = clayRobots + 1, ore = ore - blueprint.clayRobotCost.ore)

    private fun SimulationState.buyObsidianRobot(blueprint: Blueprint) = copy(
        obsidianRobots = obsidianRobots + 1,
        ore = ore - blueprint.obsidianRobotCost.ore,
        clay = clay - blueprint.obsidianRobotCost.clay,
    )

    private fun SimulationState.buyGeodeRobot(blueprint: Blueprint) = copy(
        geodeRobots = geodeRobots + 1,
        ore = ore - blueprint.geodeRobotCost.ore,
        obsidian = obsidian - blueprint.geodeRobotCost.obsidian,
    )

    private fun SimulationState.collectRocks() = copy(
        minute = minute + 1,
        ore = ore + oreRobots,
        clay = clay + clayRobots,
        obsidian = obsidian + obsidianRobots,
        geodes = geodes + geodeRobots,
        oreRobots = oreRobots,
        clayRobots = clayRobots,
        obsidianRobots = obsidianRobots,
        geodeRobots = geodeRobots,
    )
}
