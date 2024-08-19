package aoc22

import maxOrZero
import readInput

private typealias Pair_Bitmask_Value = Triple<Pair<Int, String>, Int, Int>

class Day16 {

    data class Valve(
        val name: String,
        val flowRate: Int,
        val connectedValveNames: Set<String>,
        val isOpen: Boolean,
    )

    val startingNode = "AA"

    fun solve() {
        val rawInput = readInput("day16.txt", AOCYear.TwentyTwo)

        val valves = rawInput.map { line ->
            val (valvePart, tunnelPart) = line.split("; ")

            val (name, flowRate) = valvePart.split(" ").drop(1)
                .run { first() to last().replace("rate=", "").toInt() }

            val connected = tunnelPart.replace(",", "").split(" ")
                .takeLastWhile { str -> str.all { it.isUpperCase() } }

            Valve(name, flowRate, connected.toSet(), false)
        }

        val output = floydWarshall(valves)

        val partOne = mostPressure(valves, output)
        val partTwo = mostPressure2(valves, output)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    fun mostPressure(valves: List<Valve>, distances: Map<String, Map<String, Int>>): Int {
        val nameToPipe = valves.filter { it.flowRate != 0 }.associateBy { it.name }

        val pipeToBit = nameToPipe.keys.mapIndexed { index, name -> name to (1 shl index) }.toMap()

        val pipeDistances = distances.filterKeys { it in pipeToBit || it == startingNode }
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

        return dfs(30, startingNode, 0)
    }

    fun mostPressure2(valves: List<Valve>, distances: Map<String, Map<String, Int>>): Int {
        val nameToPipe = valves.filter { it.flowRate != 0 }.associateBy { it.name }

        val pipeToBit = nameToPipe.keys.mapIndexed { index, name -> name to (1 shl index) }.toMap()

        val pipeDistances = distances.filterKeys { it in pipeToBit || it == startingNode }
            .mapValues { (key, values) -> values.filterKeys { it in pipeToBit && it != key } }

        val cache = hashMapOf<Pair<Pair<Int, String>, Int>, Int>()

        fun getNextActions(timePipe: Pair<Int, String>, bitmask: Int): List<Pair_Bitmask_Value> {
            val reachablePipes = pipeDistances.getValue(timePipe.second)

            return reachablePipes.mapNotNull { (targetPipe, distance) ->
                val targetPipeBit = pipeToBit.getValue(targetPipe)
                val remtime = timePipe.first - distance - 1

                if (bitmask and targetPipeBit != 0 || remtime < 1) return@mapNotNull null

                val targetPipeValue = remtime * nameToPipe.getValue(targetPipe).flowRate

                Triple(remtime to targetPipe, bitmask or targetPipeBit, targetPipeValue)
            }
        }

        fun dfs(timePipe: Pair<Int, String>, bitmask: Int): Int {
            cache[timePipe to bitmask]?.let { return it }

            val nextActions = getNextActions(timePipe, bitmask)

            val maxVal = nextActions.maxOfOrNull { (nextPair, newBitmask, targetValue) ->
                dfs(nextPair, newBitmask) + targetValue
            } ?: 0

            cache[timePipe to bitmask] = maxVal
            return maxVal
        }

        val cache2 = hashMapOf<Triple<Pair<Int, String>, Pair<Int, String>, Int>, Int>()

        fun dfs2(pair1: Pair<Int, String>, pair2: Pair<Int, String>, bitmask: Int): Int {
            (cache2[Triple(pair1, pair2, bitmask)] ?: cache2[Triple(pair2, pair1, bitmask)])?.let { return it }

            val pairOneActions = getNextActions(pair1, bitmask)

            val maxValPairOne = pairOneActions.maxOfOrNull { (newPair, newBitmask, targetValue) ->
                dfs(newPair, newBitmask) + targetValue
            }

            val maxValPairTwo = getNextActions(pair2, bitmask).maxOfOrNull { (newPair, newBitmask, targetValue) ->
                dfs(newPair, newBitmask) + targetValue
            }

            val maxValBoth = pairOneActions.maxOfOrNull { (newPair1, newBitmask1, target1Value) ->
                val pairTwoActions = getNextActions(pair2, newBitmask1)

                pairTwoActions.maxOfOrNull { (newPair2, newBitmask2, target2Value) ->
                    dfs2(newPair1, newPair2, newBitmask2) + target1Value + target2Value
                } ?: 0
            }

            val maxVal = listOfNotNull(maxValPairOne, maxValPairTwo, maxValBoth).maxOrZero()

            cache2[Triple(pair1, pair2, bitmask)] = maxVal
            return maxVal
        }

        return dfs2(26 to startingNode, 26 to startingNode, 0)
    }

    private fun floydWarshall(valves: List<Valve>): Map<String, Map<String, Int>> {
        val valveNames = valves.map { it.name }

        val distances = valveNames.associateWith { _ ->
            valveNames.associateWith { Int.MAX_VALUE / 2 }.toMutableMap()
        }.toMutableMap()

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
