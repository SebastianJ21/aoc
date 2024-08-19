package aoc23

import product
import readInput
import kotlin.math.max
import kotlin.math.min

typealias WorkflowRangeInput = Map<Char, IntRange>

class Day19 {
    data class WorkflowRange(
        val variableName: Char?,
        val range: IntRange?,
        val next: String,
    )

    fun solve() {
        val (rawWorkflows, rawValues) = readInput("day19.txt", AOCYear.TwentyThree).run {
            takeWhile { it.isNotEmpty() } to takeLastWhile { it.isNotEmpty() }
        }

        val extractedWorkflows = rawWorkflows.associate { rawWorkflow ->
            val name = rawWorkflow.takeWhile { it != '{' }

            // a<2006:qkq ,m>2090:A, rfg
            val afterName = rawWorkflow.substringAfter(name).drop(1).dropLast(1)

            val workflowFunctions = afterName.split(",").map { rawWorkflowPart ->
                val parts = rawWorkflowPart.split(":")

                if (parts.size == 1) {
                    { _: WorkflowRangeInput -> WorkflowRange(null, null, parts.single()) }
                } else {
                    { input: WorkflowRangeInput ->
                        val operatorChar = listOf('<', '>').first { it in rawWorkflowPart }

                        val (operatorInputName, operatorValue) = parts.first().split(operatorChar).let {
                            it[0].single() to it[1].toInt()
                        }

                        val possibleRange = input.getValue(operatorInputName)

                        if (operatorValue in possibleRange) {
                            val range = if (operatorChar == '<') {
                                possibleRange.first until operatorValue
                            } else {
                                operatorValue + 1..possibleRange.last
                            }
                            WorkflowRange(operatorInputName, range, parts[1])
                        } else {
                            null
                        }
                    }
                }
            }

            name to { input: WorkflowRangeInput ->
                workflowFunctions.map { workflowFunction -> workflowFunction(input) }
            }
        }

        val workflowFinishers = mapOf(
            "A" to true,
            "R" to false,
        )

        fun resolveRangeWorkflow(
            input: WorkflowRangeInput,
            workflowFunction: (WorkflowRangeInput) -> List<WorkflowRange?>,
        ): Long {
            val workflowRanges = workflowFunction(input)
            var currentInput = input

            fun WorkflowRangeInput.replacedRange(variableName: Char, range: IntRange): WorkflowRangeInput {
                val currentRange = getValue(variableName)
                val newRange = max(range.first, currentRange.first)..min(range.last, currentRange.last)

                return this.plus(variableName to newRange)
            }

            fun WorkflowRangeInput.removedRange(variableName: Char, range: IntRange): WorkflowRangeInput {
                val currentInterval = getValue(variableName)
                val (currentFirst, currentLast) = currentInterval.run { first to last }
                val (newFirst, newLast) = range.run { first to last }

                // Interval within
                val newIntervals = if ((newFirst != currentFirst && newLast != currentLast)) {
                    val first = currentFirst until newFirst
                    val second = newLast + 1..currentLast
                    listOf(first.takeIf { it.first < it.last } ?: second)
                } else if (newFirst == currentFirst) {
                    listOf((newLast + 1)..currentLast)
                } else {
                    listOf(currentFirst until newFirst)
                }

                return currentInput + (variableName to newIntervals.single())
            }

            return workflowRanges.sumOf {
                if (it == null) return@sumOf 0L

                val nextWorkflow = it.next
                val isLastWorkflow = it.range == null || it.variableName == null

                if (nextWorkflow in workflowFinishers) {
                    val toSum = currentInput.run {
                        if (!isLastWorkflow) replacedRange(it.variableName!!, it.range!!) else this
                    }

                    if (!isLastWorkflow) {
                        currentInput = currentInput.removedRange(it.variableName!!, it.range!!)
                    }

                    if (workflowFinishers[nextWorkflow] == true) {
                        toSum.values.map { range -> range.last - range.first + 1L }.product()
                    } else {
                        0L
                    }
                } else {
                    if (isLastWorkflow) {
                        resolveRangeWorkflow(currentInput, extractedWorkflows.getValue(nextWorkflow))
                    } else {
                        val newInput = currentInput.replacedRange(it.variableName!!, it.range!!)

                        currentInput = currentInput.removedRange(it.variableName, it.range)

                        resolveRangeWorkflow(newInput, extractedWorkflows.getValue(nextWorkflow))
                    }
                }
            }
        }

        val workflows = extractedWorkflows.mapValues { (_, workflowFunc) ->
            { input: WorkflowRangeInput -> resolveRangeWorkflow(input, workflowFunc) }
        }

        val startWorkflow = workflows.getValue("in")

        val rangeInput = mapOf(
            'x' to 1..4000,
            'm' to 1..4000,
            'a' to 1..4000,
            's' to 1..4000,
        )

        val partTwo = startWorkflow(rangeInput)

        println("Part two: $partTwo")
    }
}
