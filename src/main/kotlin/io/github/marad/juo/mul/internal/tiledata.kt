package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.model.LandEntry
import io.github.marad.juo.mul.model.StaticEntry
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

class TiledataMulReader(private val file: RandomAccessFile) {
    constructor(filePath: String) : this(RandomAccessFile(filePath, "r"))

    private lateinit var landEntries: Array<LandEntry>
    private lateinit var staticEntries: Array<StaticEntry>

    fun load() {
        val data = ByteArray(file.length().toInt())
        file.readFully(data)
        val stream = DataInputStream(ByteArrayInputStream(data))

        landEntries = (0 until 512).flatMap {
            readLandBlock(stream)
        }.toTypedArray()

        val staticEntryBlockCount = stream.available() / StaticEntry.ENTRY_BLOCK_SIZE
        staticEntries = (0 until staticEntryBlockCount).flatMap {
            readStaticBlock(stream)
        }.toTypedArray()
    }

    fun getTileData(tileId: Int): LandEntry? {
        return landEntries.getOrNull(tileId)
    }

    fun getStaticData(staticId: Int): StaticEntry? {
        return staticEntries.getOrNull(staticId)
    }
}

private fun readStaticBlock(stream: DataInputStream): List<StaticEntry> {
    stream.readInt() // header, not used
    return (0 until 32).map {
        val flags = stream.readInt().toBigEndian()
        val weight = stream.readByte()
        val quality = stream.readByte()
        stream.skip(3) // ignored fields
        val quantity = stream.readByte()
        val animation = stream.readShort().toBigEndian()
        stream.skip(1) // ignored
        val hue = stream.readByte()
        stream.skip(2)
        val height = stream.readByte()
        val name = ByteArray(20)
        stream.readNBytes(name, 0, name.size)
        StaticEntry(
                flags, weight, quality, quantity, animation,
                hue, height, String(name)
        )
    }
}

private fun readLandBlock(stream: DataInputStream): List<LandEntry> {
    stream.readInt() // header, ignored
    return (0 until 32).map {
        val flags = stream.readInt().toBigEndian()
        val textureId = stream.readShort().toBigEndian()
        val name = ByteArray(20)
        stream.readNBytes(name, 0, name.size)
        LandEntry(flags, textureId, String(name))
    }
}


