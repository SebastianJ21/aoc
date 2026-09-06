package structures

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

    internal inline fun sumOf(selector: (Int) -> Int) = elements.sumOf(selector)

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
