package io.github.marad.juo.mul

import io.github.marad.juo.mul.model.*

interface MulFacade {
    fun getAnimation(animId: Int): List<Image>

    fun getArt(artId: Int): Image?

    fun getGump(gumpId: Int): Image?

    fun getHue(hueId: Int): Hue?

    fun getMapBlock(blockX: Int, blockY: Int): Block?

    fun getMapCell(x: Int, y: Int): Cell?

    fun getTileColor(tileId: Int): Color?

    fun getTileData(tileId: Int): LandEntry?

    fun getStaticData(staticId: Int): StaticEntry?
}