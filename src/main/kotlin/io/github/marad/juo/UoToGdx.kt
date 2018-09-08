package io.github.marad.juo

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import io.github.marad.juo.mul.MulFacade
import io.github.marad.juo.mul.model.Image
import io.github.marad.juo.mul.model.rgba

object UoToGdx {
    fun imageToTexture(image: Image): Texture {
        val pixmap = Pixmap(image.width, image.height, Pixmap.Format.RGBA8888)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = image.data[y * image.width + x].rgba()
                pixmap.drawPixel(x, y, color)
            }
        }
        return Texture(pixmap)

    }

    fun drawOnPixmap(pixmap: Pixmap, image: Image, startX: Int, startY: Int) {
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = image.data[y * image.width + x].rgba()
                pixmap.drawPixel(startX + x, startY + y, color)
            }
        }
    }

    fun minimapFragment(mul: MulFacade, xRange: IntRange, yRange: IntRange): Texture {

        val width = (xRange.endInclusive - xRange.first + 1) * 8
        val height = (yRange.endInclusive - yRange.first + 1) * 8

        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        for (yBlock in yRange) {
            for (xBlock in xRange) {
                val block = mul.getMapBlock(xBlock, yBlock)
                val startX = (xBlock - xRange.first) * 8
                val startY = (yBlock - yRange.first) * 8
                for (y in 0 until 8) {
                    for (x in 0 until 8) {
                        val color = block?.getCell(x, y)?.let { mul.getTileColor(it.tileId) }?.rgba() ?: 0
                        pixmap.drawPixel(startX + x, startY + y, color)
                    }
                }
            }
        }

        return Texture(pixmap)
    }

}
