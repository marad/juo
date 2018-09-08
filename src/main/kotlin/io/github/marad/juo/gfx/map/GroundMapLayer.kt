package io.github.marad.juo.gfx.map

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import io.github.marad.juo.UoToGdx.drawOnPixmap
import io.github.marad.juo.UoToGdx.imageToTexture
import io.github.marad.juo.mul.MulFacade
import io.github.marad.juo.mul.internal.MapMulReader
import io.github.marad.juo.mul.model.Block
import io.github.marad.juo.mul.model.Image

class GroundMapLayer(private val mul: MulFacade) : TiledMapTileLayer(
        MapMulReader.MAP_WIDTH, MapMulReader.MAP_HEIGHT,
        MapMulReader.TILE_WIDTH, MapMulReader.TILE_HEIGHT) {

    private val grass: TextureRegion
    private val tiles: Array<StaticTiledMapTile?>
    private var xFrom: Int = Int.MAX_VALUE
    private var xTo: Int = Int.MIN_VALUE
    private var yFrom: Int = Int.MAX_VALUE
    private var yTo: Int = Int.MIN_VALUE

    private val blockMap: MutableMap<Pair<Int, Int>, Block> = mutableMapOf()

    fun reset() {
        xFrom = 0
        yFrom = 0
        xTo = 0
        yTo = 0
    }

    init {
        val grassTexture = imageToTexture(mul.getArt(0x4000) ?: throw RuntimeException("Invalid tile!"))
        grass = TextureRegion(grassTexture)
        val images = (0 until 0x4000).map { it -> mul.getArt(it) }
        val tilesTexture = prepareTilesTexture(images)
        tiles = splitRegions(tilesTexture, 44, 44).map {
            StaticTiledMapTile(it)
        }.toTypedArray()
    }

    override fun getCell(x: Int, y: Int): Cell? {
        if (x < 0 || y < 0) {
            return null
        }

        val blockX: Int = x / 8
        val blockY: Int = y / 8
        val inBlockX: Int = x % 8
        val inBlockY: Int = y % 8

        val key = Pair(blockX, blockY)
        val block = blockMap.getOrElse(key) {
            mul.getMapBlock(blockX, blockY)?.also {
                blockMap[key] = it
            }
        }

        return if (block == null) {
            null
        } else {
            val cell = block.getCell(inBlockX, inBlockY)
            Cell().also { it.tile = tiles[cell.tileId] }
        }
    }
}

private fun prepareTilesTexture(images: List<Image?>): Texture {
    val textureSize = 8196
    val columns = textureSize / 44
    val pixmap = Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888)
    images.forEachIndexed { index, it ->
        if (it != null) {
            val x = index % columns
            val y = index / columns
            drawOnPixmap(pixmap, it, x * 44, y * 44)
        }
    }
    return Texture(pixmap)
}

private fun splitRegions(texture: Texture, width: Int, height: Int): List<TextureRegion> {
    val columns = texture.width / width
    val rows = texture.height / height
    return (0 until rows).flatMap { y ->
        (0 until columns).map { x ->
            TextureRegion(texture, x * width, y * height, width, height)
        }
    }
}

