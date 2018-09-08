package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.model.Color
import io.github.marad.juo.mul.model.Image
import java.io.DataInputStream
import java.io.RandomAccessFile
import java.nio.file.Paths

/********************************************************************************
 * Module's Public API
 */
interface AnimMulFacade {
    fun getAnimation(animId: Int): List<Image>
}

class AnimMulCreator {
    fun create(animFilesPath: String, useBodyConf: Boolean = true): AnimMulFacade {
        if (useBodyConf) {
            return CompoundAnimMul(animFilesPath).also { it.load() }
        } else {
            val indexCreator = IndexCreator()
            return AnimMulReader(indexCreator.regularIndex(
                    Paths.get(animFilesPath, "anim.idx").toString(),
                    Paths.get(animFilesPath, "anim.mul").toString()
            ))
        }
    }
}

/********************************************************************************
 * Module main implementaiton. Abstracts away using multiple anim files with
 * bodyconv.def file
 */
private class CompoundAnimMul(private val animFilesPath: String) : AnimMulFacade {
    private lateinit var muls: Array<AnimMulReader>
    private val animMappings = BodyConvDefinitions(Paths.get(animFilesPath, "Bodyconv.def").toString())

    override fun getAnimation(animId: Int): List<Image> {
        val mapping = animMappings.getMapping(animId)
        return muls[mapping.animFileIndex].getAnimation(mapping.animIdInFile)
    }

    fun load() {
        animMappings.load()
        val indexCreator = IndexCreator()
        muls = (1..5).mapIndexed { index, it ->
            val baseName = if (it == 1) "anim" else "anim$it"
            AnimMulReader(indexCreator.regularIndex(
                    Paths.get(animFilesPath, "$baseName.idx").toString(),
                    Paths.get(animFilesPath, "$baseName.mul").toString()
            ))
        }.toTypedArray()
    }

}

/********************************************************************************
 * Reads bodyconv.dev mappings
 */
private data class BodyConvMapping(val animId: Int, val animFileIndex: Int, val animIdInFile: Int)
private class BodyConvDefinitions(private val randomAccessFile: RandomAccessFile) {

    private lateinit var mappings: Map<Int, BodyConvMapping>

    constructor(path: String) : this(RandomAccessFile(path, "r"))

    fun getMapping(animId: Int): BodyConvMapping {
        return mappings.getOrDefault(animId, BodyConvMapping(animId, 0, animId))
    }

    fun load() {
        mappings = generateSequence { randomAccessFile.readLine() }
                .map { removeComment(it) }
                .filter { it.isNotBlank() }
                .map { readMapping(it) }
                .map { it.animId to it }
                .toMap()
    }

    private fun removeComment(line: String): String {
        val commentStart = line.indexOf('#')
        return if (commentStart >= 0) {
            line.substring(0 until commentStart).trim('"')
        } else {
            line.trim('"')
        }
    }

    private fun readMapping(line: String): BodyConvMapping {
        val parsedLine = line.split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .map { it.toInt() }

        val animId = parsedLine[0]
        return if (parsedLine[1] != -1) {
            BodyConvMapping(animId, 1, parsedLine[1])
        } else if (parsedLine[2] != -1) {
            BodyConvMapping(animId, 2, parsedLine[2])
        } else if (parsedLine[3] != -1) {
            BodyConvMapping(animId, 3, parsedLine[3])
        } else if (parsedLine[4] != -1) {
            BodyConvMapping(animId, 4, parsedLine[4])
        } else {
            BodyConvMapping(animId, 0, animId)
        }

    }

}

/********************************************************************************
 * Reads animX.mul file
 */
private class AnimMulReader(private val indexedAnimMul: IndexFacade) : AnimMulFacade {
    override fun getAnimation(animId: Int): List<Image> {
        val index = indexedAnimMul.getIndex(animId)
        if (index.lookup == null) {
            throw RuntimeException("Invalid animation id") // TODO: better exceptions
        }
        val stream = DataInputStream(indexedAnimMul.getInputStream(animId))
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
        val imageData = ShortArray(width * height) { -1 }

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

