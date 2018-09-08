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

    val block = mul.getMapBlock(96, 172)
    val images = mul.getAnimation(111)
//    val image = block.let { mapBlockToImage(it, radarCol) }
    val image = mul.getArt(24)

    val config = LwjglApplicationConfiguration().also {
        it.width = 640
        it.height = 480
    }
//    LwjglApplication(Game { mapToTexture(mul, 80..110, 160..190) }, config)
//    LwjglApplication(Game { imageToTexture(image!!)}, config)
    LwjglApplication(MapGame(mul), config)

}