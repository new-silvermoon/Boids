package com.example.boids.util

import java.lang.Math
import android.R.attr.y
import android.R.attr.x
import android.R.attr.y
import android.R.attr.x




class Vector(first: Float,second: Float,third: Float) {
    var x = first
    var y = second
    var z  = third

    fun add(v : Vector):Vector{
        this.x +=v.x
        this.y += v.y
        this.z += v.z

        return this

    }

    fun subtract(v: Vector) : Vector{
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z

        return this
    }

    fun limit(limit : Float) : Vector{
        val max = Math.max(Math.abs(x),Math.max(Math.abs(y),Math.abs(z)))
        if (max > limit){
            val scaleFactor = limit / max
            x *= scaleFactor
            y *= scaleFactor
            z *= scaleFactor

        }

        return this
    }

    fun multiply(c: Float): Vector {
        x *= c
        y *= c
        z *= c
        return this
    }

    fun multiply(v: Vector) : Float{
        return this.x * v.x + this.y * v.y + this.z * v.z
    }

    fun divide(c: Float): Vector {
        x /= c
        y /= c
        z /= c
        return this
    }

    fun magnitude():Float{
        return Math.sqrt(x.toDouble() * x.toDouble() + y.toDouble() * y.toDouble() + z.toDouble() * z.toDouble()).toFloat()
    }

    fun magnitude2(): Float {
        return x * x + y * y + z * z
    }

    fun normalize(): Vector{
        return this.divide(this.magnitude())
    }

    fun copyFrom(v: Vector) : Vector {
        this.x = v.x
        this.y = v.y
        this.z = v.z

        return this
    }

    fun init() : Vector {
        this.x = 0.0f
        this.y = 0.0f
        this.z = 0.0f

        return this

    }




}