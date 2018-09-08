package io.github.marad.juo.mul.model

data class Cell(val tileId: Int, val altitude: Byte)

data class Block(val cells: Array<Cell>) {
    fun getCell(x: Int, y: Int): Cell {
        return cells.getOrElse(y * 8 + x) {
            throw RuntimeException("Invalid block index for ($x, $y)") // TODO: Better exception
        }
    }
}

