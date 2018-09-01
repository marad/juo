package io.github.marad.juo.mul.internal

import java.io.ByteArrayInputStream
import java.io.DataInput
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.*

data class Index(val id: Int, val lookup: Int?, val length: Int, val extra: Int)

/********************************************************************************
 * Module's Public API
 */
interface IndexFacade {
    fun getIndex(entryId: Int): Index
    fun getData(entryId: Int): ByteArray?
    fun getInputStream(entryId: Int): InputStream? {
        return getData(entryId)?.let { ByteArrayInputStream(it) }
    }
}

class IndexCreator {
    fun regularIndex(indexPath: String, dataPath: String): IndexFacade = DiskIndexFacade(indexPath, dataPath)
    fun cachedIndex(indexPath: String, dataPath: String): IndexFacade = CachedIndexFacade(indexPath, dataPath)
}

/********************************************************************************
 * Implementation that reads indices from disk
 */
private class DiskIndexFacade(private val indexFile: RandomAccessFile, private val dataFile: RandomAccessFile) : IndexFacade {

    constructor(indexPath: String, dataPath: String) :
            this(RandomAccessFile(indexPath, "r"), RandomAccessFile(dataPath, "r"))

    override fun getIndex(entryId: Int): Index {
        indexFile.seek(entryId * 12L)
        return readIndex(entryId, indexFile)
    }

    override fun getData(entryId: Int): ByteArray? {
        return readIndexedDataChunk(getIndex(entryId), dataFile)
    }
}

/********************************************************************************
 * Implementation that caches indices and then reads them from memory
 */
private class CachedIndexFacade(indexFile: RandomAccessFile, private val dataFile: RandomAccessFile) : IndexFacade {
    private val cache: ArrayList<Index> = loadIndex(indexFile)
    val entryCount = cache.size

    constructor(indexPath: String, dataPath: String) :
            this(RandomAccessFile(indexPath, "r"), RandomAccessFile(dataPath, "r"))


    override fun getIndex(entryId: Int): Index {
        return cache[entryId]
    }

    override fun getData(entryId: Int): ByteArray? {
        return readIndexedDataChunk(getIndex(entryId), dataFile)
    }

    private fun loadIndex(indexFile: RandomAccessFile): ArrayList<Index> {
        indexFile.seek(0)
        val indexCount = (indexFile.length() / 12).toInt()
        val cache = ArrayList<Index>(indexCount)
        (0 until indexCount).forEach {
            cache.add(readIndex(it, indexFile))
        }
        return cache
    }
}

/********************************************************************************
 * Helper functions for reading from data file
 */
private fun readIndexedDataChunk(index: Index, dataFile: RandomAccessFile): ByteArray? {
    return index.lookup?.let { address ->
        ByteArray(index.length)
                .also {
                    dataFile.seek(address.toLong())
                    dataFile.read(it, 0, index.length)
                }
    }
}

private fun readIndex(id: Int, inputStream: DataInput): Index {
    val lookup = inputStream.readInt().toBigEndian()
    return Index(
            id,
            if (lookup >= 0) lookup else null,
            inputStream.readInt().toBigEndian(),
            inputStream.readInt().toBigEndian())
}

