package io.github.marad.juo

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
    private val scale = 6

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

fun main(args: Array<String>) {

    val indexCreator = IndexCreator()
    val animReader = AnimMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\anim.idx", "D:\\Gry\\UO\\anim.mul"))
    val artMulReader = ArtMulReader(indexCreator.regularIndex("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul"))
//    val item = 0x4000 + 0x3df4
    val item = 5
//    val image = artMulReader.readImage(item)
    val images = animReader.getAnimation(111)

    val frame = JFrame("Testing").also {
        it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        it.layout = BorderLayout()
        it.add(TestPane(images ?: throw RuntimeException("Image not loaded!")))
        it.pack()
        it.setLocationRelativeTo(null)
        it.isVisible = true
    }

}