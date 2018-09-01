package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.model.Color
import io.github.marad.juo.mul.model.Hue
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

class HuesMulReader(private val huesMul: RandomAccessFile) {
    constructor(huesMulPath: String) : this(RandomAccessFile(huesMulPath, "r"))

    val hues = readHues(loadAndMakeStream(huesMul))

    fun getHue(hueId: Int): Hue? {
        return hues.getOrNull(hueId)
    }

    private fun readHues(stream: DataInputStream): List<Hue> {
        val groups = countGroups(stream)
        return (0 until groups).flatMap {
            stream.readInt() // read header
            (0 until 8).map {
                val colorTable = Array<Color>(32) { -1 }
                val name = ByteArray(20)
                (0 until 32).forEach { colorTable[it] = stream.readShort().toBigEndian() }
                val tableStart = stream.readShort().toBigEndian()
                val tableEnd = stream.readShort()
                stream.readNBytes(name, 0, name.size)
                Hue(colorTable, tableStart.toInt(), tableEnd.toInt(), String(name))
            }
        }
    }

    private fun loadAndMakeStream(file: RandomAccessFile): DataInputStream {
        val data = ByteArray(file.length().toInt())
        file.readFully(data)
        return DataInputStream(ByteArrayInputStream(data))
    }

    private fun countGroups(stream: DataInputStream): Int {
        return stream.available() / HUE_GROUP_SIZE
    }

    companion object {
        const val HUE_ENTRY_SIZE = 88
        const val HUE_GROUP_HEADER_SIZE = 4
        const val HUE_GROUP_SIZE = HUE_ENTRY_SIZE * 8 + HUE_GROUP_HEADER_SIZE
    }
}
