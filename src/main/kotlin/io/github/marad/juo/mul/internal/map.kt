package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.model.Block
import io.github.marad.juo.mul.model.Cell
import io.github.marad.juo.mul.model.Color
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

/********************************************************************************
 * Reads mapX.mul. Block represents 8x8 tiles made of Cell's
 */

class MapMulReader(private val mapMul: RandomAccessFile) {
    constructor(mapFilePath: String) : this(RandomAccessFile(mapFilePath, "r"))

    fun getBlock(blockX: Int, blockY: Int): Block? {
        val blockPosition = ((blockX.toLong() * BLOCK_HEIGHT) + blockY) * MAP_BLOCK_SIZE
        return if (blockPosition > mapMul.length()) {
            null
        } else {
            mapMul.seek(blockPosition)
            readBlock()
        }
    }

    fun getCell(x: Int, y: Int): Cell? {
        val blockX = x / 8
        val blockY = y / 8
        val inBlockX = x % 8
        val inBlockY = y % 8
        return getBlock(blockX, blockY)?.getCell(inBlockX, inBlockY)
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
        val TILE_WIDTH = 44
        val TILE_HEIGHT = 44
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
