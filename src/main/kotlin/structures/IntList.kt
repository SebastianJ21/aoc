package structures

class IntList private constructor(private val elements: IntArray) : AbstractList<Int>() {

    constructor(size: Int, init: (index: Int) -> Int) : this(IntArray(size, init))

    companion object {
        fun wrap(intArray: IntArray) = IntList(intArray)
    }

    override val size: Int = elements.size

    override fun get(index: Int): Int = elements[index]

    internal inline fun forEachIndexed(action: (index: Int, value: Int) -> Unit) {
        elements.forEachIndexed(action)
    }

    internal inline fun foldIndexed(initial: Int, operation: (index: Int, acc: Int, value: Int) -> Int): Int {
        return elements.foldIndexed(initial, operation)
    }
}

fun List<Int>.toIntList() = this as? IntList ?: IntList.wrap(this.toIntArray())
