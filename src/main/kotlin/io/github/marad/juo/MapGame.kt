package io.github.marad.juo

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.math.Vector3
import io.github.marad.juo.gfx.map.GroundMapLayer
import io.github.marad.juo.gfx.map.UOMapRenderer
import io.github.marad.juo.gfx.map.lookAtTile
import io.github.marad.juo.mul.MulFacade

class MapGame(private val mul: MulFacade) : ApplicationListener {
    private lateinit var camera: OrthographicCamera
    private val tiledMap = TiledMap()
    private lateinit var tiledMapRenderer: TiledMapRenderer
    private lateinit var groundLayer: GroundMapLayer
    private var zoomLevel = 3f

    override fun render() {

        val speed = 1f

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-speed, 0f, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(speed, 0f, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0f, speed, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0f, -speed, 0f)
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            zoomLevel += 0.1f
            camera.zoom = zoomLevel / 22f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && zoomLevel > 1f) {
            zoomLevel -= 0.1f
            camera.zoom = zoomLevel / 22f
        }


        gl.glClearColor(0f, 0f, 0f, 0f)
        gl.glClear(GL30.GL_COLOR_BUFFER_BIT)

        camera.update()
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()
        groundLayer.reset()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
        val pos = Vector3(camera.position.x, camera.position.y, camera.position.z)
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        camera.position.set(pos)
    }

    override fun create() {
        val w = Gdx.graphics.width
        val h = Gdx.graphics.height
        camera = OrthographicCamera(w.toFloat(), h.toFloat())
        camera.setToOrtho(false, w.toFloat(), h.toFloat())

        camera.lookAtTile(761, 1400)
        camera.update()
        camera.zoom = 3 / 22f
        tiledMapRenderer = UOMapRenderer(tiledMap, 1 / 22f)
        tiledMapRenderer.setView(camera)
        groundLayer = GroundMapLayer(mul)
        tiledMap.layers.add(groundLayer)

    }

    override fun dispose() {
    }
}
