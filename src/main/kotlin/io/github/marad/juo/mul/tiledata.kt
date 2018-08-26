package io.github.marad.juo.mul

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

data class LandEntry(
        val flags: Int,
        val textureId: Short,
        val name: String)

data class StaticEntry(
        val flags: Int,
        val weight: Byte,
        val quality: Byte,
//        val unknown1: Short,
//        val unknown2: Byte,
        val quantity: Byte,
        val animation: Short,
//        val unknown3: Byte,
        val hue: Byte,
//        val unknown4: Byte,
//        val unknown5: Byte,
        val height: Byte,
        val name: String) {
    companion object {
        val ENTRY_SIZE = 37
        val ENTRY_BLOCK_HEADER_SIZE = 4
        val ENTRY_BLOCK_SIZE = 32 * ENTRY_SIZE + ENTRY_BLOCK_HEADER_SIZE
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

        staticEntries.forEach { println(it) }
    }

}

fun main(args: Array<String>) {
    val reader = TiledataMulReader("D:\\Gry\\UO\\tiledata.mul")
    reader.load()
}