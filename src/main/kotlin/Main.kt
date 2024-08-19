
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull
import org.reflections.Reflections
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters

fun main() {
    solveYear(AOCYear.Twenty, days = listOf(19, 1), timeout = 1000)
}

fun solveYear(
    year: AOCYear = AOCYear.TwentyThree,
    days: List<Int> = (1..25).toList(),
    skipDays: List<Int> = emptyList(),
    timeout: Long? = null,
    quietlySkipMissing: Boolean = false,
    logTotalPerformance: Boolean = true,
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
        val start = System.currentTimeMillis()

        println("Day $dayNumber:")

        if (timeout != null) {
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(timeout) {
                    runInterruptible { solveMethod.call(classInstance) }
                }
            }
        } else {
            solveMethod.call(classInstance)
        }
        System.currentTimeMillis() - start
    }

    if (logTotalPerformance) {
        println("Performance: ${timings.sum()} ms")
    }
}
