package aoc20

import AOCYear
import mapToInt
import product
import readInput
import splitBy
import transposed

class Day16 {

    fun solve() {
        val rawInput = readInput("day16.txt", AOCYear.Twenty)

        val (rawRules, rawMyTicket, rawNearbyTickets) = rawInput.splitBy { it.isEmpty() }

        val ruleToIntervals = rawRules.associate { rawRule ->
            val (ruleName, rawIntervals) = rawRule.split(": ")

            val (left, right) = rawIntervals.split(" or ").map { it.split("-").mapToInt() }
            val intervals = listOf(left.first()..left.last(), right.first()..right.last())

            ruleName.replace(' ', '-') to intervals
        }

        val ticketNumbers = rawNearbyTickets.drop(1).map { it.split(",").mapToInt() }

        val allIntervals = ruleToIntervals.values.flatten()
        val allTicketNumbers = ticketNumbers.flatten()

        val invalidTicketsNumbers = allTicketNumbers.filter { ticket ->
            allIntervals.none { ticket in it }
        }

        val partOne = invalidTicketsNumbers.sum()

        val validTickets = ticketNumbers.filter { ticket -> ticket.none { it in invalidTicketsNumbers } }

        val myTicketNumbers = rawMyTicket.last().split(",").mapToInt()

        val columnToRuleMatches = validTickets.transposed().mapIndexed { index, ticketColumn ->
            val validRules = ruleToIntervals.filter { (_, rules) ->
                ticketColumn.all { ticketNumber -> rules.any { ticketNumber in it } }
            }

            index to validRules.keys
        }

        val indexToRule = columnToRuleMatches
            .sortedBy { it.second.size }
            .fold(listOf<Pair<Int, String>>()) { takenPositions, (index, ruleMatches) ->
                val position = ruleMatches.single { possiblePosition ->
                    takenPositions.none { (_, takenPosition) -> takenPosition == possiblePosition }
                }

                takenPositions + (index to position)
            }.toMap()

        val partTwo = myTicketNumbers.mapIndexed { index, number -> indexToRule.getValue(index) to number }
            .filter { (position) -> position.contains("departure") }
            .map { it.second.toLong() }
            .product()

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
