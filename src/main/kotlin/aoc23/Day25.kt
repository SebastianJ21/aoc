package aoc23

import AOCYear
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import readInput
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext

class Day25 {

    fun solve() {
        val rawInput = readInput("day25.txt", AOCYear.TwentyThree)

        val nodeToConnectedUnidirectional = rawInput.associate { line ->
            val (node, rest) = line.split(": ")
            val connectedWith = rest.split(" ")

            node to connectedWith
        }

        val missingConnections = nodeToConnectedUnidirectional.flatMap { (node, connected) ->
            connected.filter { connection ->
                nodeToConnectedUnidirectional[connection]?.let { node !in it } ?: true
            }.map { it to node }
        }

        val nodeToConnected: Map<String, List<String>> = buildMap {
            putAll(nodeToConnectedUnidirectional)

            missingConnections.forEach { (node, connection) ->
                this[node] = this[node]?.plus(connection) ?: listOf(connection)
            }
        }

        val edges = nodeToConnectedUnidirectional.flatMap { (node, connected) ->
            connected.map { node to it }
        }

        val virtualThreadDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val threadsToSpawn = 5

        val persistentGraph = nodeToConnected.mapValues { (_, v) -> v.toPersistentList() }.toPersistentHashMap()

        val tripleToRemove = runBlocking {
            val deferreds = (1..threadsToSpawn).map {
                async(virtualThreadDispatcher) {
                    searchForAnswer(persistentGraph, edges)
                }
            }

            val result = select {
                deferreds.forEach { deferred -> deferred.onAwait { it } }
            }

            // Cancel remaining jobs
            deferreds.forEach { it.cancel() }

            result
        }

        val graphWithoutTriple = persistentGraph.withoutEdges(tripleToRemove)

        // Just a check that the triple is correct
        check(!graphWithoutTriple.isConnected())

        val partOne = graphWithoutTriple.componentSizes()

        println("Part one: $partOne")
    }

    // Note: Focus put on immutability
    private suspend fun searchForAnswer(
        graph: PersistentMap<String, PersistentList<String>>,
        edges: List<Pair<String, String>>,
    ): List<Pair<String, String>> {
        while (coroutineContext.isActive) {
            val splitEdges = tryFindMinimumSplitEdges(graph, edges, 250, batch = 75)

            if (splitEdges.size == 3 && coroutineContext.isActive) {
                return splitEdges
            }
        }
        return emptyList()
    }

    private fun PersistentMap<String, PersistentList<String>>.addEdge(
        edge: Pair<String, String>,
    ): PersistentMap<String, PersistentList<String>> {
        val newFrom = this[edge.first]!!.add(edge.second)
        val newTo = this[edge.second]!!.add(edge.first)

        return put(edge.first, newFrom).put(edge.second, newTo)
    }

    private fun PersistentMap<String, PersistentList<String>>.withoutEdges(edges: List<Pair<String, String>>) =
        edges.fold(this) { graph, (from, to) ->
            val newFrom = graph[from]!!.remove(to)
            val newTo = graph[to]!!.remove(from)

            graph.put(from, newFrom).put(to, newTo)
        }

    private fun Map<String, Iterable<String>>.isConnected(): Boolean {
        val seen = hashSetOf<String>()

        fun dfs(node: String) {
            if (!seen.add(node)) return

            get(node)!!.forEach {
                if (it !in seen) dfs(it)
            }
        }

        dfs(keys.first())

        return seen.size == keys.size
    }

    private fun Map<String, List<String>>.componentSizes(): Int {
        fun getAllConnectedComponentsFrom(fromNode: String): Set<String> {
            val queue = ArrayDeque(listOf(fromNode))
            val seen = mutableSetOf<String>()

            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()

                if (!seen.add(node)) continue

                queue.addAll(getValue(node))
            }

            return seen
        }

        val firstPartComponents = getAllConnectedComponentsFrom(keys.first())
        val secondPartComponents = getAllConnectedComponentsFrom(keys.first { it !in firstPartComponents })

        return firstPartComponents.size * secondPartComponents.size
    }

    private fun tryFindMinimumSplitEdges(
        graph: PersistentMap<String, PersistentList<String>>,
        edges: List<Pair<String, String>>,
        limit: Int,
        batch: Int,
    ): List<Pair<String, String>> {
        val shuffledEdges = edges.shuffled().take(limit)

        val initialRemovedEdges = shuffledEdges.take(limit / 2)
        val initialEdges = shuffledEdges.drop(limit / 2)
        val initialGraph = graph.withoutEdges(initialRemovedEdges)

        val edgeRemovalSequence =
            generateSequence(Triple(initialGraph, initialRemovedEdges, initialEdges)) { (graph, removedEdges, edges) ->
                if (removedEdges.size >= limit) return@generateSequence null

                val newRemoved = edges.take(batch)

                val newRemovedEdges = removedEdges.plus(newRemoved)
                val newGraph = graph.withoutEdges(newRemoved)

                Triple(newGraph, newRemovedEdges, edges.drop(batch))
            }

        val noEdges = emptyList<Pair<String, String>>()

        val (resultGraph, edgesRemoved) =
            edgeRemovalSequence.firstOrNull { (graph) -> !graph.isConnected() } ?: return noEdges

        val (_, bridgeEdges) = edgesRemoved.fold(resultGraph to noEdges) { (graph, resultEdges), edge ->
            val graphWithEdge = graph.addEdge(edge)

            val isBridge = graphWithEdge.isConnected()

            if (isBridge) {
                graph to resultEdges.plusElement(edge)
            } else {
                graphWithEdge to resultEdges
            }
        }

        return bridgeEdges
    }
}
