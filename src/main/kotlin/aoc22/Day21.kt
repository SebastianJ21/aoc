@file:Suppress("MemberVisibilityCanBePrivate")

package aoc22

import readInput

private typealias Function_OperandNames = Pair<(Long, Long) -> Long, Pair<String, String>>

class Day21 {
    fun solve() {
        val rawInput = readInput("day21.txt")

        val (initialEvaluated, initialWaiting) = rawInput.fold(
            mapOf<String, Long>() to mapOf<String, Function_OperandNames>(),
        ) { (evaluated, waiting), line ->
            val (name, valueOrOperation) = line.split(": ")

            val numberValue = valueOrOperation.toLongOrNull()

            if (numberValue != null) {
                evaluated.plus(name to numberValue) to waiting
            } else {
                val (operandA, functionChar, operandB) = valueOrOperation.split(" ")

                val function = getFunctionFromSymbol(functionChar)

                evaluated to waiting.plus(name to Function_OperandNames(function, operandA to operandB))
            }
        }

        val evaluateSequence = generateSequence(initialEvaluated to initialWaiting) { (evaluated, waiting) ->
            if (waiting.isEmpty()) return@generateSequence null

            val toEvaluate = waiting.filter { (_, operation) ->
                val (operandA, operandB) = operation.second

                operandA in evaluated && operandB in evaluated
            }

            val newlyEvaluated = toEvaluate.mapValues { (_, operation) ->
                val result = operation.let { (evalFunction, operands) ->
                    evalFunction(evaluated.getValue(operands.first), evaluated.getValue(operands.second))
                }

                result
            }

            evaluated.plus(newlyEvaluated) to waiting.minus(newlyEvaluated.keys)
        }

        val (finalEvaluated, finalWaiting) = evaluateSequence.last()
        // Everything should be evaluated
        check(finalWaiting.isEmpty())

        val partOne = finalEvaluated.getValue("root")

        val evaluatedNamesSeq = evaluateSequence.fold(
            listOf<String>() to initialWaiting.keys,
        ) { (evaluatedNames, waitingNames), (_, waiting) ->
            val lastEvaluated = waitingNames.minus(waiting.keys)

            evaluatedNames + lastEvaluated to waiting.keys
        }.first

        val sequenceWithoutRoot = evaluatedNamesSeq.dropLast(1)

        val evalFunction = createHumnInputFunction(sequenceWithoutRoot, initialEvaluated, initialWaiting)

        val initialMax = generateSequence("9") { number ->
            number.plus('9').takeIf { evalFunction(number.toLong()) > 0 }
        }.last()

        val initialDecimals = initialMax.length

        val findAnswerSeq = generateSequence(initialMax to initialDecimals) { (current, decimals) ->
            if (decimals <= 0) return@generateSequence null

            val startIndex = current.length - decimals

            val replaceDecimal = (9 downTo 0).firstOrNull {
                val toTry = current.replaceRange(startIndex, startIndex + 1, it.toString())

                evalFunction(toTry.toLong()) > 0
            }?.inc() ?: 0

            val newNumber = current.replaceRange(startIndex, startIndex + 1, replaceDecimal.toString())

            newNumber to decimals - 1
        }

        val partTwo = findAnswerSeq.last().first

        println("Part one:  $partOne")
        println("Part two:  $partTwo")
    }

    fun createHumnInputFunction(
        sequence: List<String>,
        initialEvaluated: Map<String, Long>,
        initialWaiting: Map<String, Function_OperandNames>,
    ): (Long) -> Long {
        val evaluatedToFunc = mapOf<String, (Long) -> Long>("humn" to { l -> l })

        val evaluatedWithoutHumn = initialEvaluated.minus("humn")

        val (evaluated, waiting, evaluatedFn) = sequence.fold(
            Triple(evaluatedWithoutHumn, initialWaiting, evaluatedToFunc),
        ) { (evaluated, waiting, evaluatedFunc), toEvaluate ->
            val (function, operands) = waiting.getValue(toEvaluate)

            val firstAsValue = evaluated[operands.first]
            val firstAsFunc = evaluatedFunc[operands.first]

            val secondAsValue = evaluated[operands.second]
            val secondAsFunc = evaluatedFunc[operands.second]

            if (firstAsValue != null && secondAsValue != null) {
                val newEvaluated = evaluated.plus(toEvaluate to function(firstAsValue, secondAsValue))
                val newWaiting = waiting.minus(toEvaluate)

                Triple(newEvaluated, newWaiting, evaluatedFunc)
            } else {
                val fn: (Long) -> Long = when {
                    firstAsValue != null && secondAsFunc != null -> { l -> function(firstAsValue, secondAsFunc(l)) }
                    firstAsFunc != null && secondAsFunc != null -> { l -> function(firstAsFunc(l), secondAsFunc(l)) }
                    firstAsFunc != null && secondAsValue != null -> { l -> function(firstAsFunc(l), secondAsValue) }
                    else -> error("")
                }

                Triple(evaluated, waiting.minus(toEvaluate), evaluatedToFunc.plus(toEvaluate to fn))
            }
        }

        val (_, operands) = waiting.getValue("root")
        val firstAsFunc = evaluatedFn.getValue(operands.first)
        val secondValue = evaluated.getValue(operands.second)

        val resultFunction = { l: Long ->
            firstAsFunc(l) - secondValue
        }
        return resultFunction
    }

    fun getFunctionFromSymbol(symbol: String): (Long, Long) -> Long = when (symbol) {
        "*" -> Long::times
        "+" -> Long::plus
        "-" -> Long::minus
        "/" -> Long::div
        else -> error("Illegal operation $symbol")
    }
}
