package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.model.Color
import io.github.marad.juo.mul.model.Image
import java.io.DataInputStream

private fun isLandTile(index: Index): Boolean {
    return index.id < 0x4000
}

class ArtMulReader(private val indexedArtMul: IndexFacade) {

    fun readImage(imageId: Int): Image? {
        return indexedArtMul.getInputStream(imageId)
                ?.let {
                    val dataInputStream = DataInputStream(it)
                    if (isLandTile(indexedArtMul.getIndex(imageId))) {
                        readLandTile(dataInputStream)
                    } else {
                        readStatic(dataInputStream)
                    }
                }
    }

    private fun readLandTile(dataInputStream: DataInputStream): Image? {
        var startX = 22
        var lineWidth = 2
        val data = ShortArray(44 * 44) { -1 }

        for (y in 0..21) {
            startX -= 1
            for (x in startX until (startX + lineWidth)) {
                data[y * 44 + x] = dataInputStream.readShort().toBigEndian()
            }
            lineWidth += 2
        }

        for (y in 22..43) {
            lineWidth -= 2
            for (x in startX until (startX + lineWidth)) {
                data[y * 44 + x] = dataInputStream.readShort().toBigEndian()
            }
            startX += 1
        }

        return Image(44, 44, data)
    }

    private fun readStatic(dataInputStream: DataInputStream): Image? {
        dataInputStream.mark(dataInputStream.available())
        dataInputStream.readInt()
        val width = dataInputStream.readUnsignedShort().toUshortBigEndian()
        val height = dataInputStream.readUnsignedShort().toUshortBigEndian()
        val rowLookup = (0 until height)
                .map { dataInputStream.readUnsignedShort().toUshortBigEndian() }
                .map { (it + height + 4) * 2 }
                .toIntArray()

        val pixelData = ShortArray(width * height) { -1 }

        (0 until height)
                .forEach { y ->
                    dataInputStream.reset()
                    dataInputStream.skip(rowLookup[y].toLong())

                    var xOffset = 0
                    while (true) {
                        val additionalXOffset = dataInputStream.readUnsignedShort().toUshortBigEndian()
                        val chunkSize = dataInputStream.readUnsignedShort().toUshortBigEndian()

                        if (additionalXOffset == 0 && chunkSize == 0) {
                            break
                        } else {
                            xOffset += additionalXOffset
                            for (x in 0 until chunkSize) {
                                pixelData[(y * width) + x + xOffset] = dataInputStream.readShort().toBigEndian()
                            }
                            xOffset += chunkSize
                        }
                    }

                }

        return Image(width, height, pixelData)
    }
}
