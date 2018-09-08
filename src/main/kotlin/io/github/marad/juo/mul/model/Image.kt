package io.github.marad.juo.mul.model

typealias Color = Short

fun Color.red(): Int {
    val intVal = this.toInt()
    return ((intVal and 0x7C00) shr 10) shl 3
}

fun Color.green(): Int {
    val intVal = this.toInt()
    return ((intVal and 0x3E0) shr 5) shl 3
}

fun Color.blue(): Int {
    val intVal = this.toInt()
    return (intVal and 0x1F) shl 3
}

fun Color.rgba(): Int {
    return if (this < 0) {
        0
    } else {
        (this.red() shl 24) or (this.green() shl 16) or (this.blue() shl 8) or 0xFF
    }
}


data class Image(val width: Int, val height: Int, val data: ShortArray)
