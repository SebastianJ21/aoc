package aoc20

import AOCYear
import kotlinx.collections.immutable.putAll
import kotlinx.collections.immutable.toPersistentHashMap
import readInput

class Day7 {
    fun solve() {
        val rawInput = readInput("day7.txt", AOCYear.Twenty)

        val bagToContents = rawInput.associate { line ->
            val (bag, contents) = line.replace(Regex(" bags| bag|\\."), "").split(" contain ")

            if (contents == "no other") return@associate bag to emptyList()

            val mappedContents = contents.split(", ").map { content ->
                val (amount, name1, name2) = content.split(" ")

                "$name1 $name2" to amount.toInt()
            }

            bag to mappedContents
        }

        fun <T> resolveBagsBy(
            initialResolved: List<Pair<String, T>>,
            resolveFn: (contents: List<Pair<String, Int>>, resolved: Map<String, T>) -> T,
        ): Map<String, T> {
            val initialResolvedMap = initialResolved.toMap().toPersistentHashMap()

            val resolveSeq = generateSequence(initialResolvedMap to bagToContents) { (resolved, toResolve) ->
                if (toResolve.isEmpty()) return@generateSequence null

                val newlyResolved = toResolve.mapNotNull { (bag, contents) ->
                    val canResolve = contents.all { (name) -> name in resolved }

                    if (canResolve) {
                        bag to resolveFn(contents, resolved)
                    } else {
                        null
                    }
                }

                val newResolved = resolved.putAll(newlyResolved)

                newResolved to toResolve.filter { (bag, _) -> bag !in newResolved }
            }

            val (finalResolved) = resolveSeq.last()

            return finalResolved
        }

        val target = "shiny gold"

        val partOne = resolveBagsBy(listOf(target to true)) { contents, resolved ->
            contents.any { (name) -> resolved[name] == true }
        }.count { (_, value) -> value } - 1

        val partTwo = resolveBagsBy<Int>(listOf()) { contents, resolved ->
            contents.sumOf { (name, amount) -> resolved.getValue(name).inc() * amount }
        }.getValue(target)

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
