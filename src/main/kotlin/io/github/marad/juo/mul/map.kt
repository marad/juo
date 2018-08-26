package io.github.marad.juo.mul

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

/********************************************************************************
 * Reads mapX.mul. Block represents 8x8 tiles made of Cell's
 */

data class Cell(val tileId: Int, val altitude: Byte)

data class Block(val cells: Array<Cell>) {
    fun getCell(x: Int, y: Int): Cell {
        return cells.getOrElse(y * 8 + x) {
            throw RuntimeException("Invalid block index") // TODO: Better exception
        }
    }
}

class MapMulReader(private val mapMul: RandomAccessFile) {
    constructor(mapFilePath: String) : this(RandomAccessFile(mapFilePath, "r"))

    fun getBlock(blockX: Int, blockY: Int): Block {
        val blockPosition = ((blockX.toLong() * BLOCK_HEIGHT) + blockY) * MAP_BLOCK_SIZE
        mapMul.seek(blockPosition)
        return readBlock()
    }

    private fun readBlock(): Block {
        mapMul.readInt() // header, unknown content
        val cells = (0 until 64).map {
            Cell(mapMul.readUnsignedShort().toUshortBigEndian(), mapMul.readByte())
        }.toTypedArray()
        return Block(cells)
    }

    companion object {
        private val MAP_BLOCK_SIZE = 196
        val BLOCK_WIDTH = 768
        val BLOCK_HEIGHT = 512
        val MAP_HEIGHT = 4096
        val MAP_WIDTH = 6144
    }
}

/********************************************************************************
 * Reads radarcol.mul. Contains mappings from tileId to tile color (used for minimap)
 */

class RadarColReader(private val radarColMul: RandomAccessFile) {
    constructor(radarColPath: String) : this(RandomAccessFile(radarColPath, "r"))

    private lateinit var dataInput: DataInputStream

    fun load() {
        val data = ByteArray(65536)
        radarColMul.readFully(data)
        dataInput = DataInputStream(ByteArrayInputStream(data))
        dataInput.mark(dataInput.available())
    }

    fun getTileColor(tileId: Int): Color {
        dataInput.reset()
        dataInput.skip(tileId * ENTRY_SIZE)
        return dataInput.readShort().toBigEndian()
    }

    companion object {
        private val ENTRY_SIZE = 2L
    }
}
