package io.github.marad.juo.mul.internal

import java.io.DataInput

fun DataInput.readUnsignedInt(): Long {
    val value: Long = this.readInt().toLong()
    return value and 0x00000000FFFFFFFF
}

fun Int.toBigEndian(): Int {
    val i = this
    return i and 0xff shl 24 or (i and 0xff00 shl 8) or (i and 0xff0000 shr 8) or (i shr 24 and 0xff)
}

fun Int.toUshortBigEndian(): Int {
    val i = this
    return (i and 0xff shl 8 or (i and 0xff00 shr 8))
}

fun Short.toBigEndian(): Short {
    val i = this.toInt()
    return (i and 0xff shl 8 or (i and 0xff00 shr 8)).toShort()
}


