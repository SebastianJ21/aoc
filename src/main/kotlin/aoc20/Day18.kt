@file:Suppress("MemberVisibilityCanBePrivate")

package aoc20

import AOCYear
import readInput

class Day18 {

    data class EvalData(
        val tokens: List<String>,
        val accumulator: Long = 0,
        val carry: Long? = null,
        val function: ((Long, Long) -> Long)? = null,
    )

    fun String.convertToEvalData(): EvalData {
        val tokens = this
            .replace("(", "( ")
            .replace(")", " )")
            .split(" ")

        return EvalData(tokens)
    }

    fun String.toFunctionOrNull(): ((Long, Long) -> Long)? = when (this) {
        "+" -> Long::plus
        "*" -> Long::times
        else -> null
    }

    fun EvalData.evaluateSamePriority(): Long {
        fun EvalData.eval(): EvalData {
            val seq = generateSequence(this) { (tokens, acc, carry, function) ->
                if (tokens.isEmpty()) return@generateSequence null

                val atom = tokens.first()
                val rest = tokens.drop(1)

                fun evalNumber(number: Long, restTokens: List<String>) = when {
                    carry != null -> {
                        checkNotNull(function) { "Error evaluating ($carry, $atom). Missing function" }

                        val newAcc = acc + function(carry, number)
                        EvalData(restTokens, newAcc, null, null)
                    }

                    function == null -> EvalData(restTokens, acc, number, null)

                    else -> {
                        val newAcc = function(acc, number)

                        EvalData(restTokens, newAcc, null, null)
                    }
                }

                val atomNum = atom.toLongOrNull()
                val atomFunction = atom.toFunctionOrNull()

                when {
                    atomNum != null -> evalNumber(atomNum, rest)
                    atomFunction != null -> {
                        check(function == null) { "Error trying to overwrite function" }

                        EvalData(rest, acc, carry, atomFunction)
                    }
                    atom == ")" -> return@generateSequence null
                    atom == "(" -> {
                        val (nestedEvalTokens, nestedEval) = EvalData(rest).eval()

                        val nextTokens = nestedEvalTokens.drop(1)

                        evalNumber(nestedEval, nextTokens)
                    }
                    else -> error("Unrecognized atom $atom")
                }
            }

            return seq.last()
        }

        return eval().accumulator
    }

    fun EvalData.evaluateAdditionPriority(): Long {
        fun EvalData.eval(): EvalData {
            val evalSequence = generateSequence(this) { (tokens, acc) ->
                if (tokens.isEmpty()) return@generateSequence null

                val atom = tokens.first()
                val rest = tokens.drop(1)

                val atomNum = atom.toLongOrNull()

                when {
                    atomNum != null -> EvalData(rest, acc + atomNum)
                    atom == "*" -> {
                        val evaluatedNext = EvalData(rest, 0).eval()

                        val newAcc = acc * evaluatedNext.accumulator

                        EvalData(evaluatedNext.tokens, newAcc)
                    }
                    atom == "+" -> EvalData(rest, acc)
                    atom == "(" -> {
                        val (nestedEvalTokens, nestedEval) = EvalData(rest).eval()

                        val nextTokens = nestedEvalTokens.drop(1)

                        EvalData(nextTokens, acc + nestedEval)
                    }
                    atom == ")" -> return@generateSequence null

                    else -> error("Unrecognized atom $atom")
                }
            }

            val evalResult = evalSequence.last()

            return evalResult
        }

        return eval().accumulator
    }

    fun solve() {
        val rawInput = readInput("day18.txt", AOCYear.Twenty)

        val partOne = rawInput.sumOf { line ->
            line.convertToEvalData().evaluateSamePriority()
        }

        val partTwo = rawInput.sumOf { line ->
            line.convertToEvalData().evaluateAdditionPriority()
        }

        println("Part one: $partOne")
        println("Part two: $partTwo")
    }
}
