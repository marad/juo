package io.github.marad.juo

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.marad.juo.mul.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class TestPane(private val images: List<Image>) : JPanel() {
    private var frame = 0
    private var image = images[frame]
    private val scale = 3

    private val timer = Timer(200) {
        this.frame += 1
        this.frame = this.frame.rem(images.size)
        this.image = images[frame]
        this.repaint()
    }.also { it.start() }

    override fun getPreferredSize(): Dimension {
        return Dimension(image.width * scale, image.height * scale)
    }

    override fun paintComponent(gr: Graphics) {
        super.paintComponent(gr)

        gr.color = Color.BLUE
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = image.data[y * image.width + x]
                val r = color.red()
                val g = color.green()
                val b = color.blue()
                gr.color = Color(r, g, b)
                gr.fillRect(x * scale, y * scale, scale, scale)
            }
        }
    }
}

fun swing(args: Array<String>) {

    val indexCreator = IndexCreator()
    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim.idx", "D:\\Gry\\UO\\anim.mul"))
    val artMulReader = ArtMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul"))
    val gumpartReader = GumpartMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\gumpidx.mul", "D:\\Gry\\UO\\gumpart.mul"))
    val item = 0x4000 + 0x3df4
    val image = artMulReader.readImage(item)
    val images = animReader.getAnimation(111)

    val frame = JFrame("Testing").also {
        it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        it.layout = BorderLayout()
//        it.add(TestPane(listOf(gumpartReader.getGump(74) ?: throw RuntimeException("Image not loaded!"))))
        it.add(TestPane(images))
        it.pack()
        it.setLocationRelativeTo(null)
        it.isVisible = true
    }

}


fun imageToTexture(image: Image): Texture {
    //val texture = Texture(image.width, image.height, Pixmap.Format.RGBA8888)
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
    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim.idx", "D:\\Gry\\UO\\anim.mul"))
    val images = animReader.getAnimation(110)


    val config = LwjglApplicationConfiguration()
    LwjglApplication(Game(images.first()), config)

}