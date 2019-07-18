package com.example.boids

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.boids.util.BoidsGLSurfaceView
import com.example.boids.util.BoidsRenderer
import kotlinx.android.synthetic.main.activity_single_boid.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_boid)

        btnSingle.setOnClickListener(this)
        btnMultiple.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnSingle -> startActivity(Intent(this,Single_Boid_Activity::class.java))
            R.id.btnMultiple -> startActivity(Intent(this,PredBoidActivity::class.java))

        }
    }
}
