package com.example.boids

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.boids.util.BoidsGLSurfaceView
import com.example.boids.util.BoidsRenderer

class Single_Boid_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val glSurfaceView = BoidsGLSurfaceView(this)
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0)
        glSurfaceView.setRenderer(BoidsRenderer(this))
        setContentView(glSurfaceView)
    }



}
