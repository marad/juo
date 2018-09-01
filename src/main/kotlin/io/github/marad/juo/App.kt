package io.github.marad.juo

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.marad.juo.mul.MulConfig
import io.github.marad.juo.mul.MulFacade
import io.github.marad.juo.mul.internal.*
import io.github.marad.juo.mul.model.Block
import io.github.marad.juo.mul.model.Image
import io.github.marad.juo.mul.model.Color
import io.github.marad.juo.mul.model.rgba


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

fun mapToTexture(mul: MulFacade, xRange: IntRange, yRange: IntRange): Texture {

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

fun mapBlockToImage(block: Block, radarColReader: RadarColReader): Image {
    val data: Array<Color> = block.cells.map {
        radarColReader.getTileColor(it.tileId)
    }.toTypedArray()
    return Image(8, 8, data)
}

class Game(private val getTexture: () -> Texture) : ApplicationListener {
    private lateinit var batch: SpriteBatch
    private lateinit var sprite: Sprite
    private lateinit var viewport: Viewport
    private lateinit var camera: Camera

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)

        batch.begin()
        sprite.draw(batch)
        batch.end()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun create() {
        camera = OrthographicCamera()
        viewport = FitViewport(640f, 480f)

        batch = SpriteBatch()
        sprite = Sprite(getTexture())
        sprite.setCenter(320f, 240f)
        sprite.scale(1f)
//        sprite.rotate(-45f)
    }

    override fun dispose() {
    }
}


fun main(args: Array<String>) {
    val mul = MulConfig().createMul("D:\\Gry\\UO")
//    val indexCreator = IndexCreator()
//    val art = ArtMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul"))
//    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim2.idx", "D:\\Gry\\UO\\anim2.mul"))
//    val animReader = AnimMulCreator().create("D:\\Gry\\Electronic Arts\\Ultima Online Classic", true)
//    val map = MapMulReader("D:\\Gry\\UO\\map1.mul")
//    val radarCol = RadarColReader("D:\\Gry\\UO\\radarcol.mul")
//    radarCol.load()

    val block = mul.getMapBlock(96, 172)
    val images = mul.getAnimation(111)
//    val image = block.let { mapBlockToImage(it, radarCol) }
    val image = mul.getArt(8)


    val config = LwjglApplicationConfiguration().also {
        it.width = 640
        it.height = 480
    }
    LwjglApplication(Game { mapToTexture(mul, 80..110, 160..190) }, config)
//    LwjglApplication(Game { imageToTexture(image!!)}, config)

}