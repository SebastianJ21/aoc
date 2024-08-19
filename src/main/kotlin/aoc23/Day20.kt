@file:Suppress("MemberVisibilityCanBePrivate")

package aoc23

import AOCYear
import lcm
import plus
import readInput
import java.math.BigInteger

class Day20 {
    interface Module {
        val id: String
        val connections: List<String>
        fun receivePulse(pulse: Boolean, from: String): List<Pair<String, Boolean>>
    }

    data class FlipFlopModule(
        override val id: String,
        override val connections: List<String>,
    ) : Module {
        private var state: Boolean = false

        override fun receivePulse(pulse: Boolean, from: String): List<Pair<String, Boolean>> = if (pulse) {
            emptyList()
        } else {
            state = !state
            connections.map { it to state }
        }
    }

    data class ConjunctionModule(
        override val id: String,
        override val connections: List<String>,
    ) : Module {

        private val recentPulses = mutableMapOf<String, Boolean>()

        fun trackPulseFor(id: String) {
            recentPulses[id] = false
        }

        override fun receivePulse(pulse: Boolean, from: String): List<Pair<String, Boolean>> {
            recentPulses[from] = pulse

            // All recent pulses are true -> send false, otherwise send true
            val sentPulse = recentPulses.values.any { !it }

            return connections.map { it to sentPulse }
        }
    }

    data class BroadcastModule(
        override val id: String,
        override val connections: List<String>,
    ) : Module {

        override fun receivePulse(pulse: Boolean, from: String): List<Pair<String, Boolean>> {
            return connections.map { it to pulse }
        }
    }

    fun getAllInputModules(forModule: String, modules: Iterable<Module>): List<String> = modules
        .filter { module -> forModule in module.connections }
        .map { it.id }

    fun solve() {
        val rawInput = readInput("day20.txt", AOCYear.TwentyThree)

        val createModules = {
            val idToModule = rawInput.associate { line ->
                val (modulePart, connectionPart) = line.split(" -> ")

                val (moduleType, moduleId) = modulePart.first() to modulePart.dropWhile { !it.isLetter() }

                val connections = connectionPart.split(", ")

                moduleId to when (moduleType) {
                    '%' -> FlipFlopModule(moduleId, connections)
                    '&' -> ConjunctionModule(moduleId, connections)
                    'b' -> BroadcastModule(modulePart, connections)
                    else -> error("Unknown module type $moduleType")
                }
            }

            // Setup all conjunction modules to track all their inputs
            idToModule.values.filterIsInstance<ConjunctionModule>().forEach { conjModule ->
                getAllInputModules(conjModule.id, idToModule.values).forEach { inputModule ->
                    conjModule.trackPulseFor(inputModule)
                }
            }

            idToModule
        }

        fun countSignalScore(pressCount: Int): Int {
            val modules = createModules()

            val (high, low) = (1..pressCount).fold(0 to 0) { acc, _ ->
                acc + executeButtonPress(modules)
            }

            return high * low
        }

        val partOne = countSignalScore(1000)

        val idToModulePartTwo = createModules()

        val finalInputModule = getAllInputModules("rx", idToModulePartTwo.values).single()

        val seen = getAllInputModules(finalInputModule, idToModulePartTwo.values).associateWith { 0 }.toMutableMap()
        val cycleLength = mutableMapOf<String, Int>()

        var buttonPress = 0
        while (seen.values.any { it <= 1 }) {
            buttonPress++
            val buttonPulses = idToModulePartTwo.getValue("broadcaster").receivePulse(false, "button")

            val queue = ArrayDeque(listOf("broadcaster" to buttonPulses))

            while (queue.isNotEmpty()) {
                val (from, pulsesToProcess) = queue.removeFirst()

                pulsesToProcess.forEach { (to, pulse) ->
                    if (to == finalInputModule && pulse) {
                        seen[from] = seen[from]!! + 1

                        if (from !in cycleLength) {
                            cycleLength[from] = buttonPress
                        } else {
                            check(buttonPress == seen.getValue(from) * cycleLength.getValue(from))
                        }
                    }

                    val newPulses = idToModulePartTwo[to]?.receivePulse(pulse, from) ?: return@forEach

                    queue.addLast(to to newPulses)
                }
            }
        }

        val partTwo = cycleLength.values.map { it.toBigInteger() }.reduce(BigInteger::lcm)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun executeButtonPress(modules: Map<String, Module>): Pair<Int, Int> {
        val broadcastPulses = modules.getValue("broadcaster").receivePulse(false, "button")

        val queue = ArrayDeque(listOf("broadcaster" to broadcastPulses))

        val signals = buildList {
            add(false)

            while (queue.isNotEmpty()) {
                val (from, toProcess) = queue.removeFirst()
                if (toProcess.isEmpty()) continue

                toProcess.forEach { (id, pulse) ->
                    add(pulse)

                    val newPulses = modules[id]?.receivePulse(pulse, from)

                    if (newPulses != null) {
                        queue.addLast(id to newPulses)
                    }
                }
            }
        }

        return signals.partition { it }.let { (high, low) -> high.size to low.size }
    }
}
