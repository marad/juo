package io.github.marad.juo.mul

import java.io.DataInputStream

data class Frame(val imageCenterX: Short, val imageCenterY: Short,
                 val width: Short, val height: Short)

private data class FrameChunk(val xOffset: Short, val yOffset: Short, val pixelIndices: ByteArray)

class AnimMulReader(private val indexedMulFile: IndexedMulFile) {
    fun getAnimation(animId: Int): List<Image> {
        val index = indexedMulFile.getIndex(animId)
        if (index.lookup == null) {
            throw RuntimeException("Invalid animation id") // TODO: better exceptions
        }
        val stream = DataInputStream(indexedMulFile.getInputStream(animId))
        val palette = readPalette(stream)
        stream.mark(stream.available())
        val frameCount = stream.readInt().toBigEndian()
        val frameOffsets = readFrameOffsets(frameCount, stream)

        return (0 until frameCount)
                .map {
                    stream.reset()
                    stream.skip(frameOffsets[it].toLong())
                    readFrame(palette, stream)
                }
    }

    private fun readPalette(stream: DataInputStream): Array<Color> {
        val palette = Array<Color>(256) { -1 }
        (0 until 256).forEach {
            palette[it] = stream.readShort().toBigEndian()
        }
        return palette
    }

    private fun readFrameOffsets(frameCount: Int, stream: DataInputStream): Array<Int> {
        val frameOffsets = Array(frameCount) { -1 }
        (0 until frameCount).forEach {
            frameOffsets[it] = stream.readInt().toBigEndian()
        }
        return frameOffsets
    }

    private fun readFrame(palette: Array<Color>, stream: DataInputStream): Image {
        val imageCenterX = stream.readShort().toBigEndian()
        val imageCenterY = stream.readShort().toBigEndian() // to może być ważne do renderowania później
        // do poprawnego nakladania obrazkow
        val width = stream.readShort().toBigEndian()
        val height = stream.readShort().toBigEndian()
        val imageData = Array<Color>(width * height) { -1 }

        var prevLineNum = 0xFF
        var y = 0
        while (true) {
            val rowHeader = stream.readShort().toBigEndian().toInt()
            var rowOffset = stream.readShort().toBigEndian().toInt()
            if (rowHeader == 0x7FFF || rowOffset == 0x7FFF) {
                break
            }
            val runLength = (rowHeader and 0x0FFF).toShort()
            val lineNum = ((rowHeader shr 12) and 0x000F)
//            val unknown = (rowOffset and 0x03f)
//            val tmp = (rowOffset and 0x8000)
//            rowOffset = tmp or (rowOffset shr 6)
            rowOffset = rowOffset shr 6

            println("runLength: $runLength")
            println("yOffset: $lineNum")
            println("xOffset: $rowOffset")
            val x = imageCenterX + rowOffset
            if (prevLineNum != 0xFF && lineNum != prevLineNum) {
                y++
            }
            prevLineNum = lineNum
            if (y >= 0) {
                if (y >= height) {
                    break
                }

                for (j in 0 until runLength) {
                    val color = palette[stream.readByte().toInt() and 0xFF]
                    imageData[y * width + x + j] = color
                }
            } else {
                stream.skip(runLength.toLong())
            }
        }

//        println("cx: $imageCenterX cy: $imageCenterY")
//        println("width: $width height: $height")
//        println(readChunk(stream))
//
//        generateSequence { readChunk(stream) }
//                .forEach { chunk ->
//                    var x = imageCenterX + chunk.xOffset
//                    val y = imageCenterY + chunk.yOffset
//                    chunk.pixelIndices
//                            .map { palette[it.toInt() and 0xFF] }
//                            .forEach {
//                                println("x: $x y: $y")
//                                imageData[y * width + x] = it
//                                x += 1
//                            }
//                }

        return Image(width.toInt(), height.toInt(), imageData)
    }

//    private fun readChunk(stream: DataInputStream): FrameChunk? {
//        val header = stream.readInt().toBigEndian()
//        if (header == 0x7FFF7FFF) {
//            return null
//        }
//        val pixelCount = header and 0x0FFF
//        val yOffset = ((header ushr 12) and 0xF)
//        val xOffset = ((header ushr 22) and 0x3FF)
//
//            println("runLength: $pixelCount")
//            println("yOffset: $xOffset")
//            println("xOffset: $yOffset")
//
//        val pixelIndices = ByteArray(pixelCount)
//        stream.readNBytes(pixelIndices, 0, pixelCount)
//
//        return FrameChunk(xOffset.toShort(), yOffset.toShort(), pixelIndices)
//    }

}

fun main(args: Array<String>) {
    val reader = AnimMulReader(IndexedMulFile("D:\\Gry\\UO\\anim.idx", "D:\\Gry\\UO\\anim.mul"))
    reader.getAnimation(110)

//    val b: Byte = -1
//    val i = b.toInt() and 0xFF
//    println(i)
}