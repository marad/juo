package io.github.marad.juo.mul

import java.io.DataInputStream

class GumpartMulReader(private val indexedGumpartMul: IndexFacade) {
    fun getGump(gumpId: Int): Image? {
        val index = indexedGumpartMul.getIndex(gumpId)
        val stream = DataInputStream(indexedGumpartMul.getInputStream(gumpId))

        val (width, height) = readDimensions(index)
        readLookupTable(height, stream)
        val imageData = readImageData(width, height, stream)

        return Image(width, height, imageData)
    }

    private fun readDimensions(index: Index): Pair<Int, Int> {
        val width = (index.extra shr 16) and 0xFFFF
        val height = index.extra and 0xFFFF
        return Pair(width, height)
    }

    private fun readLookupTable(height: Int, stream: DataInputStream): List<Int> {
        return (0 until height).map { stream.readInt().toBigEndian() }
    }

    private fun readImageData(width: Int, height: Int, stream: DataInputStream): Array<Color> {
        val imageData = Array<Color>(width * height) { -1 }
        var x = 0
        while (stream.available() > 0) {
            val color = stream.readShort().toBigEndian()
            val count = stream.readShort().toBigEndian()
            imageData.fill(color, x, x + count)
            x += count
        }
        return imageData
    }
}
