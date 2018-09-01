package io.github.marad.juo.mul.internal

import io.github.marad.juo.mul.MulFacade
import io.github.marad.juo.mul.model.Color
import java.nio.file.Paths

class MulFacadeImpl(private val uoPath: String) : MulFacade {
    private lateinit var anim: AnimMulFacade
    private lateinit var art: ArtMulReader
    private lateinit var gump: GumpartMulReader
    private lateinit var hues: HuesMulReader
    private lateinit var map: MapMulReader
    private lateinit var tileData: TiledataMulReader
    private lateinit var radarCol: RadarColReader

    fun load() {
        val indexCreator = IndexCreator()
        anim = AnimMulCreator().create(uoPath, useBodyConf = false)
        art = ArtMulReader(indexCreator.regularIndex(
                Paths.get(uoPath, "artidx.mul").toString(),
                Paths.get(uoPath, "art.mul").toString()
        ))
        gump = GumpartMulReader(indexCreator.regularIndex(
                Paths.get(uoPath, "gumpidx.mul").toString(),
                Paths.get(uoPath, "gumpart.mul").toString()
        ))

        hues = HuesMulReader(Paths.get(uoPath, "hues.mul").toString())
        map = MapMulReader(Paths.get(uoPath, "map1.mul").toString())
        radarCol = RadarColReader(Paths.get(uoPath, "radarcol.mul").toString())
        radarCol.load()
        tileData = TiledataMulReader(Paths.get(uoPath, "tiledata.mul").toString())
        tileData.load()
    }

    override fun getAnimation(animId: Int) = anim.getAnimation(animId)

    override fun getArt(artId: Int) = art.readImage(artId)

    override fun getGump(gumpId: Int) = gump.getGump(gumpId)

    override fun getHue(hueId: Int) = hues.getHue(hueId)

    override fun getMapBlock(blockX: Int, blockY: Int) = map.getBlock(blockX, blockY)

    override fun getTileColor(tileId: Int) = radarCol.getTileColor(tileId)

    override fun getTileData(tileId: Int) = tileData.getTileData(tileId)

    override fun getStaticData(staticId: Int) = tileData.getStaticData(staticId)
}
