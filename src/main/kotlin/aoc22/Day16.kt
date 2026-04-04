package aoc22

import AOCAnswer
import AOCSolution
import maxOrZero
import readInput

class Day16 : AOCSolution {

    private data class State(
        val time: Int,
        val pipe: Int,
        val bitmask: Int,
        val value: Int,
    )

    data class Valve(
        val id: Int,
        val name: String,
        val flowRate: Int,
        val connectedValveNames: List<String>,
        val isOpen: Boolean,
    )

    companion object {
        const val START_NODE = "AA"
    }

    override fun solve(): AOCAnswer {
        val rawInput = readInput("day16.txt", AOCYear.TwentyTwo)

        val valves = rawInput.mapIndexed { index, line ->
            // Valve ZN has flow rate=0; tunnels lead to valves SD, ZV
            val (valvePart, tunnelPart) = line.split("; ")

            val (name, flowRate) = valvePart.split(" ").drop(1)
                .run { first() to last().replace("rate=", "").toInt() }

            val connected = tunnelPart.replace(",", "").split(" ")
                .takeLastWhile { str -> str.all { it.isUpperCase() } }

            Valve(id = index, name = name, flowRate = flowRate, connectedValveNames = connected, isOpen = false)
        }

        val output = floydWarshall(valves)

        val partOne = mostPressure(valves, output)
        val partTwo = mostPressure2(valves, output)

        return AOCAnswer(partOne, partTwo)
    }

    private fun mostPressure(valves: List<Valve>, distances: Map<String, Map<String, Int>>): Int {
        val nameToPipe = valves.filter { it.flowRate != 0 }.associateBy { it.name }

        val pipeToBit = nameToPipe.keys.mapIndexed { index, name -> name to (1 shl index) }.toMap()

        val pipeDistances = distances.filterKeys { it in pipeToBit || it == START_NODE }
            .mapValues { (key, values) -> values.filterKeys { it in pipeToBit && it != key } }

        val cache = hashMapOf<Triple<Int, String, Int>, Int>()

        fun dfs(time: Int, pipe: String, bitmask: Int): Int {
            cache[Triple(time, pipe, bitmask)]?.let { return it }

            val reachablePipes = pipeDistances.getValue(pipe)

            val maxVal = reachablePipes.maxOf { (targetPipe, distance) ->
                val targetPipeBit = pipeToBit.getValue(targetPipe)
                val remtime = time - distance - 1

                if (bitmask and targetPipeBit != 0 || remtime < 1) return@maxOf 0

                val targetPipeValue = remtime * nameToPipe.getValue(targetPipe).flowRate

                dfs(remtime, targetPipe, bitmask or targetPipeBit) + targetPipeValue
            }

            cache[Triple(time, pipe, bitmask)] = maxVal
            return maxVal
        }

        return dfs(30, START_NODE, 0)
    }

    private data class CacheKey(val time1: Int, val pipe1: Int, val time2: Int, val pipe2: Int, val bitmask: Int)

    private fun mostPressure2(valves: List<Valve>, distances: Map<String, Map<String, Int>>): Int {
        val nameToValve = valves.associateBy { it.name }

        val idToPipe = valves.filter { it.flowRate != 0 }.associateBy { it.id }
        val idToBit = idToPipe.keys.mapIndexed { index, id -> id to (1 shl index) }.toMap()

        val valveBits = valves.map { idToBit[it.id] ?: -1 }.toIntArray()

        val idToFlowrate = valves.map { valve -> valve.flowRate }.toIntArray()

        val startNodeId = nameToValve.getValue(START_NODE).id

        val dist = distances
            .mapKeys { (name) -> nameToValve.getValue(name) }
            .mapValues { (parent, nameToDistance) ->
                nameToDistance.mapKeys { (name) -> nameToValve.getValue(name).id }
                    .filterKeys { it != parent.id && it in idToPipe }
                    .toList()
            }
            .mapValues { (valve, distances) -> distances.takeIf { valve.id in idToPipe || valve.name == START_NODE } }
            .values
            .toList()

        val cacheList = List(26) { List(valves.size) { HashMap<Int, Int>() } }

        fun getNextActions(time: Int, pipe: Int, bitmask: Int): List<State> {
            val reachablePipes = dist[pipe]!!

            return reachablePipes.mapNotNull { (targetPipe, distance) ->
                val targetPipeBit = valveBits[targetPipe] // idToBit.getValue(targetPipe)
                check(targetPipeBit != -1)

                val remtime = time - distance - 1

                if (bitmask and targetPipeBit != 0 || remtime < 1) return@mapNotNull null

                val targetPipeValue = remtime * idToFlowrate[targetPipe] // idToPipe.getValue(targetPipe).flowRate

                State(
                    time = remtime,
                    pipe = targetPipe,
                    bitmask = bitmask or targetPipeBit,
                    value = targetPipeValue,
                )
            }
        }

        fun dfs(time: Int, pipe: Int, bitmask: Int): Int {
            cacheList[time][pipe][bitmask]?.let { return it }

            val nextActions = getNextActions(time, pipe, bitmask)

            val maxVal = nextActions.maxOfOrNull { action ->
                dfs(action.time, action.pipe, action.bitmask) + action.value
            } ?: 0

            cacheList[time][pipe][bitmask] = maxVal

            return maxVal
        }

        val cache2 = hashMapOf<CacheKey, Int>()

        // Part one: 1376
        // Part two: 1933
        fun dfs2(time1: Int, pipe1: Int, time2: Int, pipe2: Int, bitmask: Int): Int {
            val mainKey = CacheKey(time1, pipe1, time2, pipe2, bitmask)
            (cache2[mainKey] ?: cache2[CacheKey(time2, pipe2, time1, pipe1, bitmask)])?.let { return it }

            val pairOneActions = getNextActions(time1, pipe1, bitmask)

            val maxValPairOne = pairOneActions.maxOfOrNull { action ->
                dfs(action.time, action.pipe, action.bitmask) + action.value
            }

            val maxValPairTwo = getNextActions(time2, pipe2, bitmask)
                .maxOfOrNull { action -> dfs(action.time, action.pipe, action.bitmask) + action.value }

            val maxValBoth = pairOneActions.maxOfOrNull { action1 ->
                val pairTwoActions = getNextActions(time2, pipe2, action1.bitmask)

                pairTwoActions.maxOfOrNull { action2 ->
                    dfs2(action1.time, action1.pipe, action2.time, action2.pipe, action2.bitmask) +
                        action1.value + action2.value
                } ?: 0
            }

            val maxVal = listOfNotNull(maxValPairOne, maxValPairTwo, maxValBoth).maxOrZero()

            cache2[mainKey] = maxVal
            return maxVal
        }

        return dfs2(26, startNodeId, 26, startNodeId, 0)
    }

    private fun floydWarshall(valves: List<Valve>): Map<String, Map<String, Int>> {
        val valveNames = valves.map { it.name }

        val distances = valveNames.associateWithTo(mutableMapOf()) { _ ->
            valveNames.associateWithTo(mutableMapOf()) { Int.MAX_VALUE / 2 }
        }

        valves.forEach { valve ->
            valve.connectedValveNames.forEach { connectedValve ->
                distances[valve.name]!![connectedValve] = 1
            }
        }

        valveNames.forEach { valveName ->
            distances[valveName]!![valveName] = 0
        }

        valveNames.forEach { k ->
            valveNames.forEach { i ->
                valveNames.forEach { j ->
                    if (distances[i]!![j]!! > distances[i]!![k]!! + distances[k]!![j]!!) {
                        distances[i]!![j] = distances[i]!![k]!! + distances[k]!![j]!!
                    }
                }
            }
        }

        return distances
    }
}
