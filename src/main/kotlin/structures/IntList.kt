package structures

import kotlin.math.min

class IntList private constructor(private val elements: IntArray) : AbstractList<Int>() {

    constructor(size: Int, init: (index: Int) -> Int) : this(IntArray(size, init))

    companion object {
        fun wrap(intArray: IntArray) = IntList(intArray)
    }

    override val size: Int
        get() = elements.size

    override fun get(index: Int): Int = elements[index]

    override fun contains(element: Int): Boolean = elements.contains(element)

    internal inline fun forEachInt(action: (value: Int) -> Unit) {
        elements.forEach(action)
    }

    internal inline fun forEachIndexed(action: (index: Int, value: Int) -> Unit) {
        elements.forEachIndexed(action)
    }

    internal inline fun foldIndexed(initial: Int, operation: (index: Int, acc: Int, value: Int) -> Int): Int {
        return elements.foldIndexed(initial, operation)
    }

    internal inline fun runningReduce(operation: (acc: Int, value: Int) -> Int): IntList {
        if (isEmpty()) return IntList(IntArray(0))

        var accumulator = elements[0]
        val result = IntArray(size)

        result[0] = accumulator

        for (index in 1 until size) {
            accumulator = operation(accumulator, elements[index])

            result[index] = accumulator
        }

        return IntList(result)
    }

    internal inline fun zip(other: IntList, transform: (a: Int, b: Int) -> Int): IntList {
        val list = IntArray(min(this.size, other.size))

        var i = 0
        for (element in this) {
            if (i >= other.size) break
            list[i] = transform(element, other[i++])
        }

        return wrap(list)
    }

    internal fun drop(n: Int): IntList {
        require(n >= 0) { "Requested element count $n is less than zero." }
        if (n >= size) return IntList(IntArray(0))

        val result = IntArray(size - n)
        var resultIndex = 0

        for (index in n until size) {
            result[resultIndex++] = elements[index]
        }

        return wrap(result)
    }

    internal inline fun sumOf(selector: (Int) -> Int) = elements.sumOf(selector)

    internal fun sum() = elements.sum()

    internal inline fun mutated(mutator: (IntArray) -> Unit): IntList {
        val copied = elements.copyOf()
        mutator(copied)
        return wrap(copied)
    }

    override fun iterator(): IntIterator = object : IntIterator() {
        private var index = 0

        override fun hasNext(): Boolean = index < size

        override fun nextInt(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return elements[index++]
        }
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is IntList -> orderedEquals(other)
        other is List<*> -> orderedEquals(other)
        else -> false
    }

    private fun orderedEquals(other: List<*>): Boolean {
        if (this.size != other.size) return false

        val otherIterator = other.iterator()
        for (elem in this.elements) {
            if (elem != otherIterator.next()) return false
        }

        return true
    }

    private fun orderedEquals(other: IntList): Boolean {
        if (this.size != other.size) return false

        val otherIterator = other.iterator()
        for (elem in this.elements) {
            if (elem != otherIterator.next()) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (elem in this.elements) {
            hashCode = 31 * hashCode + elem.hashCode()
        }
        return hashCode
    }
}

fun List<Int>.toIntList() = this as? IntList ?: IntList.wrap(this.toIntArray())
