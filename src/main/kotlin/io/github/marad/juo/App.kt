package io.github.marad.juo

import io.github.marad.juo.mul.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.JPanel

class TestPane(private val image: Image) : JPanel() {
    private val scale = 6

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

    val mulFile = IndexedMulFile("D:\\Gry\\UO\\artidx.mul", "D:\\Gry\\UO\\art.mul")
    val artMulReader = ArtMulReader()
//    val item = 0x4000 + 0x3df4
    val item = 5
    println("Index: ${mulFile.getIndex(item)}")
    val image = artMulReader.readImage(mulFile.getIndex(item), mulFile.getInputStream(item)
            ?: throw RuntimeException("Whoops!"))

    val frame = JFrame("Testing").also {
        it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        it.layout = BorderLayout()
        it.add(TestPane(image ?: throw RuntimeException("Image not loaded!")))
        it.pack()
        it.setLocationRelativeTo(null)
        it.isVisible = true
    }
}