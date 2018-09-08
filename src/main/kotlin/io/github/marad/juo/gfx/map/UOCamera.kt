package io.github.marad.juo.gfx.map

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import kotlin.math.sqrt

private object Transform {
    private val isoTransform: Matrix4
    private val invIsotransform: Matrix4

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
    }

    fun screenToIso(x: Float, y: Float): Vector3 {
        return Vector3(x, y, 0f).mul(isoTransform)
    }

    fun isoToScreen(x: Float, y: Float): Vector3 {
        return Vector3(x, y, 0f).mul(invIsotransform)
    }
}

fun Camera.lookAtTile(x: Int, y: Int) {
    val iso = Transform.isoToScreen(x.toFloat(), y.toFloat())
    this.position.set(iso.x, iso.y, this.position.z)
}