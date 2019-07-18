package com.example.boids

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.boids.util.BoidsGLSurfaceView
import com.example.boids.util.BoidsRenderer
import com.example.boids.util.PredBoidRenderer
import com.example.boids.util.PredsGLSurfaceView

class PredBoidActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val glSurfaceView = PredsGLSurfaceView(this)
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0)
        glSurfaceView.setRenderer(PredBoidRenderer(this))
        setContentView(glSurfaceView)
    }
}
