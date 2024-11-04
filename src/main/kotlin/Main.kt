
import org.reflections.Reflections
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

fun main() {
    solveYear(AOCYear.Twenty, days = listOf(19, 1), timeout = 1000)
}

data class AOCAnswer(val partOne: Any, val partTwo: Any?)

interface AOCSolution {
    fun solve(): AOCAnswer
}

enum class ErrorPropagation { CAUSE, FULL, NONE }

fun solveYear(
    year: AOCYear = AOCYear.TwentyThree,
    days: List<Int> = (1..25).toList(),
    skipDays: List<Int> = emptyList(),
    timeout: Long? = null,
    quietlySkipMissing: Boolean = false,
    logTotalPerformance: Boolean = true,
    errorPropagation: ErrorPropagation = ErrorPropagation.CAUSE,
) {
    val prefix = year.getSuffix()
    val reflections = Reflections(prefix)

    val daysToRun = days.filter { it !in skipDays }

    fun log(msg: String) = if (!quietlySkipMissing) println(msg) else Unit

    val timings = daysToRun.mapNotNull { dayNumber ->
        val klazz = reflections.forClass("$prefix.Day$dayNumber")?.kotlin

        if (klazz == null) {
            log("Unable to find Day $dayNumber for $prefix. Has it been solved?")
            log("Make sure class Day$dayNumber is present in package $prefix")
            return@mapNotNull null
        }

        val solveMethod = klazz.functions.firstOrNull {
            it.name.contains("solve") && it.valueParameters.isEmpty()
        }

        if (solveMethod == null) {
            log("Unable to find solve() method on Day $dayNumber for $prefix.")
            log("Make sure a no-param solve method is present in class Day$dayNumber.")
            return@mapNotNull null
        }

        val classInstance = klazz.createInstance()
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val start = System.currentTimeMillis()

        println("Day $dayNumber:")

        val hasIncorrectReturnType = solveMethod.returnType != AOCAnswer::class.starProjectedType

        if (hasIncorrectReturnType) {
            val warningColor = "\u001B[33m"
            val resetColor = "\u001B[0m"

            log(warningColor + "Solve method does not return the expected AOCAnswer type!" + resetColor)

            if (quietlySkipMissing) return@mapNotNull null
        }

        val callableSolve = Callable { solveMethod.call(classInstance) }
        val task = executor.submit(callableSolve)

        val result = runCatching {
            if (timeout != null) {
                task.get(timeout, TimeUnit.MILLISECONDS)
            } else {
                task.get()
            }
        }

        result.onFailure { exception ->
            when (exception) {
                is ExecutionException -> {
                    // TODO: Get the lowest level cause (the actual error in the solving code)?

                    // Possibly make this configurable? Currently, an error from one day halts the entire execution
                    when (errorPropagation) {
                        ErrorPropagation.FULL -> throw exception
                        ErrorPropagation.CAUSE -> throw exception.lowestLevelCause()
                        ErrorPropagation.NONE -> {
                            println("${exception.lowestLevelCause()}")
                        }
                    }
                }
                is InterruptedException, is TimeoutException -> {
                    executor.shutdownNow()

                    println("Timeout out...")
                }
                else -> error("Unknown exception type in result: $exception")
            }
        }

        result.onSuccess { answer ->
            if (!hasIncorrectReturnType) {
                val (partOne, partTwo) = answer as AOCAnswer

                printAOCAnswers(partOne, partTwo)
            }
        }

        // TODO: Handling of performance on timed-out tasks
        System.currentTimeMillis() - start
    }

    if (logTotalPerformance) {
        println("Performance: ${timings.sum()} ms")
    }
}

fun Throwable.lowestLevelCause(): Throwable = cause?.lowestLevelCause() ?: this
