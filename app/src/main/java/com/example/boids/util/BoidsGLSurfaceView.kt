package com.example.boids.util

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.properties.Delegates
import com.example.boids.util.BoidsGLSurfaceView.TouchEventRun



class BoidsGLSurfaceView(context: Context) : GLSurfaceView(context) {

    lateinit var renderer: BoidsRenderer

    override fun setRenderer(renderer: Renderer?) {
        this.renderer = renderer as BoidsRenderer
        super.setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.action === MotionEvent.ACTION_MOVE || event.action === MotionEvent.ACTION_DOWN) {
                if (renderer != null) {
                    queueEvent(TouchEventRun(event.x, event.y))
                }
            }
        }
        return true
    }

    inner class TouchEventRun(x: Float, y: Float) : Runnable{

    var x by Delegates.notNull<Float>()
    var y by Delegates.notNull<Float>()

    init {
        this.x = x
        this.y = y
    }

    override fun run() {
        renderer.touch(x,y)
    }

}


}