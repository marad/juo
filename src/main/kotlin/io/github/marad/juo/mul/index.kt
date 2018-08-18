package io.github.marad.juo.mul

import java.io.ByteArrayInputStream
import java.io.DataInput
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.*

data class Index(val id: Int, val lookup: Int?, val length: Int, val extra: Int) {
    companion object {
        fun read(id: Int, inputStream: DataInput): Index {
            val lookup = inputStream.readInt().toBigEndian()
            return Index(
                    id,
                    if (lookup >= 0) lookup else null,
                    inputStream.readInt().toBigEndian(),
                    inputStream.readInt().toBigEndian())
        }
    }
}

class IndexedMulFile(private val indexFile: RandomAccessFile, private val dataFile: RandomAccessFile) {
    private val cache: ArrayList<Index> = loadIndex(indexFile)
    val entryCount = cache.size

    constructor(indexPath: String, dataPath: String) :
            this(RandomAccessFile(indexPath, "r"), RandomAccessFile(dataPath, "r"))


    fun getIndex(entryIndex: Int): Index {
        return cache[entryIndex]
    }

    fun getData(entryIndex: Int): ByteArray? {
        val index = getIndex(entryIndex)
        return if (index.lookup != null) {
            ByteArray(index.length)
                    .also {
                        dataFile.seek(index.lookup.toLong())
                        dataFile.read(it, 0, index.length)
                    }
        } else {
            null
        }
    }

    fun getInputStream(entryIndex: Int): InputStream? {
        return getData(entryIndex)?.let { ByteArrayInputStream(it) }
    }

    private fun loadIndex(indexFile: RandomAccessFile): ArrayList<Index> {
        indexFile.seek(0)
        val indexCount = (indexFile.length() / 12).toInt()
        val cache = ArrayList<Index>(indexCount)
        (0 until indexCount).forEach {
            cache.add(Index.read(it, indexFile))
        }
        return cache
    }
}

fun main(args: Array<String>) {

    val mulFile = IndexedMulFile("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul")
    println("Entries: ${mulFile.entryCount}")

    (0 until mulFile.entryCount)
            .map { (it to mulFile.getIndex(it)) }
            .filter { it.second.extra == 0 }
            .filter { it.second.length != 1850 }
            .forEach { println("${it.first}: ${it.second}") }

}