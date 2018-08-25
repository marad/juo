package io.github.marad.juo

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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
    private lateinit var texture: Texture
    private lateinit var batch: SpriteBatch

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)

        val scale = 1f
        batch.begin()
        batch.draw(
                texture, 0f, 0f, 0f, 0f, texture.width.toFloat(), texture.height.toFloat(), scale, scale, 0f, 0, 0,
                texture.width, texture.height, false, false)
        batch.end()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun create() {
        texture = imageToTexture(image)
        batch = SpriteBatch()
    }

    override fun dispose() {
    }
}


fun main(args: Array<String>) {
    val indexCreator = IndexCreator()
//    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim2.idx", "D:\\Gry\\UO\\anim2.mul"))
    val animReader = AnimMulCreator().create("D:\\Gry\\Electronic Arts\\Ultima Online Classic", true)
    val images = animReader.getAnimation(233)


    val config = LwjglApplicationConfiguration()
    LwjglApplication(Game(images.first()), config)

}