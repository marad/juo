package io.github.marad.juo.mul

import java.io.DataInputStream

typealias Color = Short

private fun isLandTile(index: Index): Boolean {
    return index.id < 0x4000
}

fun Color.red(): Int {
    val intVal = this.toInt()
    return ((intVal and 0x7C00) shr 10) shl 3
}

fun Color.green(): Int {
    val intVal = this.toInt()
    return ((intVal and 0x3E0) shr 5) shl 3
}

fun Color.blue(): Int {
    val intVal = this.toInt()
    return (intVal and 0x1F) shl 3
}


data class Image(val width: Int, val height: Int, val data: Array<Color>)

class ArtMulReader(private val indexedMulFile: IndexedMulFile) {

    fun readImage(imageId: Int): Image? {

        val dataInputStream = DataInputStream(indexedMulFile.getInputStream(imageId))
        return if (isLandTile(indexedMulFile.getIndex(imageId))) {
            readLandTile(dataInputStream)
        } else {
            readStatic(dataInputStream)
        }
    }

    private fun readLandTile(dataInputStream: DataInputStream): Image? {
        var startX = 22
        var lineWidth = 2
        val data = Array<Color>(44 * 44) { -1 }

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

        val pixelData = Array<Color>(width * height) { -1 }

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

//fun main(args: Array<String>){
//    val mulFile = IndexedMulFile("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul")
//    val artMulReader = ArtMulReader()
//    val image = artMulReader.readImage(mulFile.getIndex(128), mulFile.getInputStream(128) ?: throw RuntimeException("No data!"))
//
//    for(y in 0 .. 43) {
//        val start = y * 44
//        val slice = image!!.data.sliceArray(start .. start + 43).map { if(it.toInt() != -1) '#' else '.' }
//        println("$slice $y")
//    }
//}