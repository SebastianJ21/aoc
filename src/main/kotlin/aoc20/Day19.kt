@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import firstAndRest
import mapToInt
import readInput
import splitBy

class Day19 {

    fun solve() {
        val rawInput = readInput("day19.txt", AOCYear.Twenty)

        val (rawRules, textLines) = rawInput.splitBy { it.isEmpty() }

        val (resolvedRules, unresolvedRules) = rawRules.partition { it.contains("\"") }

        val terminalRules = resolvedRules.associate { ruleLine ->
            val (rawId, rawCondition) = ruleLine.split(": ")

            rawId.toInt() to rawCondition.replace("\"", "")
        }

        val nonTerminalRules = unresolvedRules.associate { ruleLine ->
            val (rawId, rawCondition) = ruleLine.split(": ")

            val ruleSet = rawCondition.split(" | ").map { ruleSet ->
                ruleSet.split(" ").mapToInt()
            }

            rawId.toInt() to ruleSet
        }

        val partOne = textLines.count { match(it, terminalRules, nonTerminalRules) }

        val partTwoNonTerminalRules = nonTerminalRules.mapValues { (id, ruleSet) ->
            when (id) {
                8 -> ruleSet.plusElement(listOf(42, 8))
                11 -> ruleSet.plusElement(listOf(42, 11, 31))
                else -> ruleSet
            }
        }

        val partTwo = textLines.count { match(it, terminalRules, partTwoNonTerminalRules) }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }

    private fun matchRuleSet(
        line: String,
        ruleSet: List<Int>,
        matchRule: (line: String, ruleId: Int) -> Boolean,
    ): Boolean {
        when {
            line.isEmpty() && ruleSet.isEmpty() -> return true
            line.isEmpty() || ruleSet.isEmpty() -> return false
        }

        return line.withIndex().any { (index) ->
            val (matchLine, restLine) = line.take(index + 1) to line.drop(index + 1)
            val (firstRule, restRules) = ruleSet.firstAndRest()

            matchRule(matchLine, firstRule) && matchRuleSet(restLine, restRules, matchRule)
        }
    }

    fun match(string: String, terminal: Map<Int, String>, nonTerminal: Map<Int, List<List<Int>>>): Boolean {
        val cache = hashMapOf<Pair<String, Int>, Boolean>()

        fun matchRule(line: String, ruleId: Int): Boolean {
            cache[line to ruleId]?.let { return it }

            val result = if (ruleId in terminal) {
                terminal[ruleId] == line
            } else {
                nonTerminal.getValue(ruleId).any { ruleSet ->
                    matchRuleSet(line, ruleSet) { line: String, ruleId: Int -> matchRule(line, ruleId) }
                }
            }

            cache[line to ruleId] = result

            return result
        }

        return matchRule(string, 0)
    }
}
