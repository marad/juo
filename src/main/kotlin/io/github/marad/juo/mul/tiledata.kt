package io.github.marad.juo.mul

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.RandomAccessFile

typealias Flags = Int

data class LandEntry(
        val flags: Flags,
        val textureId: Short,
        val name: String)

data class StaticEntry(
        val flags: Flags,
        val weight: Byte,
        val quality: Byte,
//        val unknown1: Short,
//        val unknown2: Byte,
        val quantity: Byte,
        val animation: Short,
//        val unknown3: Byte,
        val hue: Byte,
//        val unknown4: Byte,
//        val unknown5: Byte,
        val height: Byte,
        val name: String) {

    fun getWeaponClass(): Byte {
        if (flags.isWeapon()) {
            return quantity
        } else {
            throw RuntimeException("This is not a weapon!")
        }
    }

    fun getArmorClass(): Byte {
        if (flags.isArmor()) {
            return quantity
        } else {
            throw RuntimeException("This is not an armor!")
        }
    }

    fun getWearableLayer(): Byte {
        if (flags.isWearable()) {
            return quantity
        } else {
            throw RuntimeException("This is not wearable!")
        }
    }

    fun getLightId(): Byte {
        if (flags.isLightShource()) {
            return quantity
        } else {
            throw RuntimeException("This is not a light source!")
        }
    }

    fun getContains(): Byte {
        if (flags.isContainer()) {
            return quantity
        } else {
            throw RuntimeException("This is not a container!")
        }
    }

    companion object {
        val ENTRY_SIZE = 37
        val ENTRY_BLOCK_HEADER_SIZE = 4
        val ENTRY_BLOCK_SIZE = 32 * ENTRY_SIZE + ENTRY_BLOCK_HEADER_SIZE
    }
}

private fun readLandBlock(stream: DataInputStream): List<LandEntry> {
    stream.readInt() // header, ignored
    return (0 until 32).map {
        val flags = stream.readInt().toBigEndian()
        val textureId = stream.readShort().toBigEndian()
        val name = ByteArray(20)
        stream.readNBytes(name, 0, name.size)
        LandEntry(flags, textureId, String(name))
    }
}

private fun readStaticBlock(stream: DataInputStream): List<StaticEntry> {
    stream.readInt() // header, not used
    return (0 until 32).map {
        val flags = stream.readInt().toBigEndian()
        val weight = stream.readByte()
        val quality = stream.readByte()
        stream.skip(3) // ignored fields
        val quantity = stream.readByte()
        val animation = stream.readShort().toBigEndian()
        stream.skip(1) // ignored
        val hue = stream.readByte()
        stream.skip(2)
        val height = stream.readByte()
        val name = ByteArray(20)
        stream.readNBytes(name, 0, name.size)
        StaticEntry(
                flags, weight, quality, quantity, animation,
                hue, height, String(name)
        )
    }
}

class TiledataMulReader(private val file: RandomAccessFile) {
    constructor(filePath: String) : this(RandomAccessFile(filePath, "r"))

    private lateinit var landEntries: Array<LandEntry>
    private lateinit var staticEntries: Array<StaticEntry>

    fun load() {
        val data = ByteArray(file.length().toInt())
        file.readFully(data)
        val stream = DataInputStream(ByteArrayInputStream(data))

        landEntries = (0 until 512).flatMap {
            readLandBlock(stream)
        }.toTypedArray()


        val staticEntryBlockCount = stream.available() / StaticEntry.ENTRY_BLOCK_SIZE
        staticEntries = (0 until staticEntryBlockCount).flatMap {
            readStaticBlock(stream)
        }.toTypedArray()

        staticEntries.forEach { println(it) }
    }

}

fun Flags.isBackground(): Boolean = (this and TiledataFlag.Background.value) == 1
fun Flags.isWeapon(): Boolean = (this and TiledataFlag.Weapon.value) == 1
fun Flags.isTransparent(): Boolean = (this and TiledataFlag.Transparent.value) == 1
fun Flags.isTranslucent(): Boolean = (this and TiledataFlag.Translucent.value) == 1
fun Flags.isWall(): Boolean = (this and TiledataFlag.Wall.value) == 1
fun Flags.isSurface(): Boolean = (this and TiledataFlag.Surface.value) == 1
fun Flags.isBridge(): Boolean = (this and TiledataFlag.Bridge.value) == 1
fun Flags.isGeneric(): Boolean = (this and TiledataFlag.Generic.value) == 1
fun Flags.isWindow(): Boolean = (this and TiledataFlag.Window.value) == 1
fun Flags.isNoShoot(): Boolean = (this and TiledataFlag.NoShoot.value) == 1
fun Flags.isArticleA(): Boolean = (this and TiledataFlag.ArticleA.value) == 1
fun Flags.isArticleAn(): Boolean = (this and TiledataFlag.ArticleAn.value) == 1
fun Flags.isInternal(): Boolean = (this and TiledataFlag.Internal.value) == 1
fun Flags.isFoliage(): Boolean = (this and TiledataFlag.Foliage.value) == 1
fun Flags.isPartialHue(): Boolean = (this and TiledataFlag.PartialHue.value) == 1
fun Flags.isMap(): Boolean = (this and TiledataFlag.Map.value) == 1
fun Flags.isContainer(): Boolean = (this and TiledataFlag.Container.value) == 1
fun Flags.isWearable(): Boolean = (this and TiledataFlag.Wearable.value) == 1
fun Flags.isLightShource(): Boolean = (this and TiledataFlag.LightSource.value) == 1
fun Flags.isAnimation(): Boolean = (this and TiledataFlag.Animation.value) == 1
fun Flags.isNoDiagonal(): Boolean = (this and TiledataFlag.NoDiagonal.value) == 1
fun Flags.isArmor(): Boolean = (this and TiledataFlag.Armor.value) == 1
fun Flags.isRoof(): Boolean = (this and TiledataFlag.Roof.value) == 1
fun Flags.isDoor(): Boolean = (this and TiledataFlag.Door.value) == 1
fun Flags.isStairBack(): Boolean = (this and TiledataFlag.StairBack.value) == 1
fun Flags.isStairRight(): Boolean = (this and TiledataFlag.StairRight.value) == 1

enum class TiledataFlag(val value: Int) {
    Background(0x00000001),
    Weapon(0x00000002),
    Transparent(0x00000004),
    Translucent(0x00000008),
    Wall(0x00000010),
    Damaging(0x00000020),
    Impassable(0x00000040),
    Wet(0x00000080),
    Unknown1(0x00000100),
    Surface(0x00000200),
    Bridge(0x00000400),
    Generic(0x00000800),
    Window(0x00001000),
    NoShoot(0x00002000),
    ArticleA(0x00004000),
    ArticleAn(0x00008000),
    Internal(0x00010000),
    Foliage(0x00020000),
    PartialHue(0x00040000),
    Unknown2(0x00080000),
    Map(0x00100000),
    Container(0x00200000),
    Wearable(0x00400000),
    LightSource(0x00800000),
    Animation(0x01000000),
    NoDiagonal(0x02000000),
    Unknown3(0x04000000),
    Armor(0x08000000),
    Roof(0x10000000),
    Door(0x20000000),
    StairBack(0x40000000),
    StairRight(0x80000000.toInt())
}

fun main(args: Array<String>) {
    val reader = TiledataMulReader("D:\\Gry\\UO\\tiledata.mul")
    reader.load()
}