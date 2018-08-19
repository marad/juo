package io.github.marad.juo.mul

import java.io.DataInputStream

class AnimMulReader(private val indexedMulFacade: IndexFacade) {
    fun getAnimation(animId: Int): List<Image> {
        val index = indexedMulFacade.getIndex(animId)
        if (index.lookup == null) {
            throw RuntimeException("Invalid animation id") // TODO: better exceptions
        }
        val stream = DataInputStream(indexedMulFacade.getInputStream(animId))
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
            rowOffset = rowOffset shr 6

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

        return Image(width.toInt(), height.toInt(), imageData)
    }
}

