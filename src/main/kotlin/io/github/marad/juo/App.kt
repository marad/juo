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
import io.github.marad.juo.mul.AnimMulCreator
import io.github.marad.juo.mul.Image
import io.github.marad.juo.mul.IndexCreator
import io.github.marad.juo.mul.rgba


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

class Game(private val image: Image) : ApplicationListener {
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

        val texture = imageToTexture(image)
        batch = SpriteBatch()
        sprite = Sprite(texture)
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
    val images = animReader.getAnimation(111)


    val config = LwjglApplicationConfiguration().also {
        it.width = 640
        it.height = 480
    }
    LwjglApplication(Game(images.first()), config)

}