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
import io.github.marad.juo.mul.*


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

fun mapToTexture(mapMulReader: MapMulReader, radarColReader: RadarColReader, xRange: IntRange, yRange: IntRange): Texture {

    val width = (xRange.endInclusive - xRange.first + 1) * 8
    val height = (yRange.endInclusive - yRange.first + 1) * 8

    val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
    for (yBlock in yRange) {
        for (xBlock in xRange) {
            val block = mapMulReader.getBlock(xBlock, yBlock)
            val startX = (xBlock - xRange.first) * 8
            val startY = (yBlock - yRange.first) * 8
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val color = block.getCell(x, y).let { radarColReader.getTileColor(it.tileId) }.rgba()
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
    }

    override fun dispose() {
    }
}


fun main(args: Array<String>) {
    val indexCreator = IndexCreator()
//    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim2.idx", "D:\\Gry\\UO\\anim2.mul"))
    val animReader = AnimMulCreator().create("D:\\Gry\\Electronic Arts\\Ultima Online Classic", true)
    val map = MapMulReader("D:\\Gry\\UO\\map1.mul")
    val radarCol = RadarColReader("D:\\Gry\\UO\\radarcol.mul")
    radarCol.load()

    val block = map.getBlock(96, 172)
    val images = animReader.getAnimation(111)
    val image = block.let { mapBlockToImage(it, radarCol) }


    val config = LwjglApplicationConfiguration().also {
        it.width = 640
        it.height = 480
    }
    LwjglApplication(Game { mapToTexture(map, radarCol, 80..110, 160..190) }, config)
//    LwjglApplication(Game { imageToTexture(image)}, config)

}