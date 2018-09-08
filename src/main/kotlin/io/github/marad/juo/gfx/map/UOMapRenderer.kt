package io.github.marad.juo.gfx.map

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.sqrt
import com.badlogic.gdx.graphics.g2d.Batch.*

class UOMapRenderer(tiledMap: TiledMap, unitScale: Float) : BatchTiledMapRenderer(tiledMap, unitScale) {
    private val isoTransform: Matrix4
    private val invIsotransform: Matrix4
    private var screenPos = Vector3()

    private val topRight = Vector2()
    private val bottomLeft = Vector2()
    private val topLeft = Vector2()
    private val bottomRight = Vector2()

    private val pixel: Texture

    constructor(tiledMap: TiledMap) : this(tiledMap, 1f)

    init {
        // create the isometric transform
        isoTransform = Matrix4()
        isoTransform.idt()

        // isoTransform.translate(0, 32, 0);
        isoTransform.scale(sqrt(2.0f) / 2.0f, -sqrt(2.0f) / 2.0f, 1.0f)
        isoTransform.rotate(0.0f, 0.0f, 1.0f, 45f)

        // ... and the inverse matrix
        invIsotransform = Matrix4(isoTransform)
        invIsotransform.inv()

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.drawPixel(0, 0, Color.WHITE.toIntBits())
        pixel = Texture(pixmap)

    }

    private fun translateScreenToIso(vec: Vector2): Vector3 {
        screenPos.set(vec.x, vec.y, 0f)
        screenPos.mul(invIsotransform)

        return screenPos
    }

    private fun drawIsoDot(x: Float, y: Float, color: Color) {
        val iso = translateScreenToIso(Vector2(x, y))
        batch.setColor(color.toFloatBits())
        batch.draw(pixel, iso.x, iso.y)
    }

    override fun renderTileLayer(layer: TiledMapTileLayer) {

        drawIsoDot(0f, 0f, Color.RED)
        drawIsoDot(10f, 0f, Color.GREEN)
        drawIsoDot(0f, 10f, Color.BLUE)

        batch.setColor(Color.WHITE.toFloatBits())

        val batchColor = batch.color
        val color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity())

        val tileWidth = layer.getTileWidth() * unitScale
        val tileHeight = layer.getTileHeight() * unitScale

        val layerOffsetX = layer.getRenderOffsetX() * unitScale
        // offset in tiled is y down, so we flip it
        val layerOffsetY = -layer.getRenderOffsetY() * unitScale

        val halfTileWidth = tileWidth * 0.5f
        val halfTileHeight = tileHeight * 0.5f

        // setting up the screen points
        // COL1
        topRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y - layerOffsetY)
        // COL2
        bottomLeft.set(viewBounds.x - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY)
        // ROW1
        topLeft.set(viewBounds.x - layerOffsetX, viewBounds.y - layerOffsetY)
        // ROW2
        bottomRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY)

        // transforming screen coordinates to iso coordinates
        val row1 = (translateScreenToIso(topLeft).y / tileWidth).toInt() + 2
        val row2 = (translateScreenToIso(bottomRight).y / tileWidth).toInt() - 2

        val col1 = (translateScreenToIso(bottomLeft).x / tileWidth).toInt() - 2
        val col2 = (translateScreenToIso(topRight).x / tileWidth).toInt() + 2

        for (row in row1 downTo row2) {
            for (col in col1..col2) {
                val x = (col - row) * halfTileWidth
                val y = (-row - col) * halfTileHeight

                val cell = layer.getCell(col, row) ?: continue
                val tile = cell.tile

                if (tile != null) {
                    val flipX = cell.flipHorizontally
                    val flipY = cell.flipVertically
                    val rotations = cell.rotation

                    val region = tile.textureRegion

                    val x1 = x + tile.offsetX * unitScale + layerOffsetX
                    val y1 = y + tile.offsetY * unitScale + layerOffsetY
                    val x2 = x1 + region.regionWidth * unitScale
                    val y2 = y1 + region.regionHeight * unitScale

                    val u1 = region.u
                    val v1 = region.v2
                    val u2 = region.u2
                    val v2 = region.v

                    vertices[X1] = x1
                    vertices[Y1] = y1
                    vertices[C1] = color
                    vertices[U1] = u1
                    vertices[V1] = v1

                    vertices[X2] = x1
                    vertices[Y2] = y2
                    vertices[C2] = color
                    vertices[U2] = u1
                    vertices[V2] = v2

                    vertices[X3] = x2
                    vertices[Y3] = y2
                    vertices[C3] = color
                    vertices[U3] = u2
                    vertices[V3] = v2

                    vertices[X4] = x2
                    vertices[Y4] = y1
                    vertices[C4] = color
                    vertices[U4] = u2
                    vertices[V4] = v1

                    if (flipX) {
                        var temp = vertices[U1]
                        vertices[U1] = vertices[U3]
                        vertices[U3] = temp
                        temp = vertices[U2]
                        vertices[U2] = vertices[U4]
                        vertices[U4] = temp
                    }
                    if (flipY) {
                        var temp = vertices[V1]
                        vertices[V1] = vertices[V3]
                        vertices[V3] = temp
                        temp = vertices[V2]
                        vertices[V2] = vertices[V4]
                        vertices[V4] = temp
                    }
                    if (rotations != 0) {
                        when (rotations) {
                            TiledMapTileLayer.Cell.ROTATE_90 -> {
                                val tempV = vertices[V1]
                                vertices[V1] = vertices[V2]
                                vertices[V2] = vertices[V3]
                                vertices[V3] = vertices[V4]
                                vertices[V4] = tempV

                                val tempU = vertices[U1]
                                vertices[U1] = vertices[U2]
                                vertices[U2] = vertices[U3]
                                vertices[U3] = vertices[U4]
                                vertices[U4] = tempU
                            }
                            TiledMapTileLayer.Cell.ROTATE_180 -> {
                                var tempU = vertices[U1]
                                vertices[U1] = vertices[U3]
                                vertices[U3] = tempU
                                tempU = vertices[U2]
                                vertices[U2] = vertices[U4]
                                vertices[U4] = tempU
                                var tempV = vertices[V1]
                                vertices[V1] = vertices[V3]
                                vertices[V3] = tempV
                                tempV = vertices[V2]
                                vertices[V2] = vertices[V4]
                                vertices[V4] = tempV
                            }
                            TiledMapTileLayer.Cell.ROTATE_270 -> {
                                val tempV = vertices[V1]
                                vertices[V1] = vertices[V4]
                                vertices[V4] = vertices[V3]
                                vertices[V3] = vertices[V2]
                                vertices[V2] = tempV

                                val tempU = vertices[U1]
                                vertices[U1] = vertices[U4]
                                vertices[U4] = vertices[U3]
                                vertices[U3] = vertices[U2]
                                vertices[U2] = tempU
                            }
                        }
                    }
                    batch.draw(region.texture, vertices, 0, BatchTiledMapRenderer.NUM_VERTICES)
                }
            }
        }
    }
}