package com.example.Preds.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Random

import javax.microedition.khronos.opengles.GL10;
import android.graphics.Color
import com.example.boids.util.Vector

class Pred {

    lateinit var location : Vector
    lateinit var velocity : Vector
    companion object {
        var side =0.015f
        private val MAX_VELOCITY = 0.03f
        private val DESIRED_SEPARATION = 0.01f
        private val SEPARATION_WEIGHT = 0.05f
        private val ALIGNMENT_WEIGHT = 0.3f
        private val COHESION_WEIGHT = 0.3f
        private val MAX_FORCE = 0.005f
        private val INERTION = 0.001f
        private val CENTRIC_POWER = 0.8f // > 0 more power
        lateinit var mFVertexBuffer : FloatBuffer
        lateinit var mColorBuffer : FloatBuffer
        lateinit var mIndexBuffer : ByteBuffer
        val tempVector = Vector(0.0f,0.0f,0.0f)
        val sum = Vector(0.0f,0.0f,0.0f)
        val align = Vector(0.0f,0.0f,0.0f)
        val separate = Vector(0.0f,0.0f,0.0f)

        fun initModel(size: Float, model: Int){
            Pred.side = size

            val indices = byteArrayOf(// Vertex indices of the 4 Triangles
                2, 4, 3, // front face (CCW)
                1, 4, 2, // right face
                0, 4, 1, // back face
                4, 0, 3 // left face
            )
            val vertices = floatArrayOf(//
                -side / 3f, -side, -side / 3f, // 0. left-bottom-back
                side / 3f, -side, -side / 3f, // 1. right-bottom-back
                side / 3f, -side, side / 3f, // 2. right-bottom-front
                -side / 3f, -side, side / 3f, // 3. left-bottom-front
                0.0f, side, 0.0f // 4. top
            )

            val colors = floatArrayOf(//
                (Color.red(model) - 10) / 255f,
                (Color.green(model) - 10) / 255f,
                (Color.blue(model) - 10) / 255f,
                1.0f, //
                (Color.red(model) - 10) / 255f,
                (Color.green(model) - 10) / 255f,
                (Color.blue(model) - 10) / 255f,
                1.0f, //
                (Color.red(model) - 10) / 255f,
                (Color.green(model) - 10) / 255f,
                (Color.blue(model) - 10) / 255f,
                1.0f, //
                (Color.red(model) - 10) / 255f,
                (Color.green(model) - 10) / 255f,
                (Color.blue(model) - 10) / 255f,
                1.0f, //
                (Color.red(model) + 10) / 255f,
                (Color.green(model) + 10) / 255f,
                (Color.blue(model) + 10) / 255f,
                1.0f // nose
            )

            var vbb = ByteBuffer.allocateDirect(vertices.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            mFVertexBuffer = vbb.asFloatBuffer()
            mFVertexBuffer.put(vertices)
            mFVertexBuffer.position(0)

            vbb = ByteBuffer.allocateDirect(colors.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            mColorBuffer = vbb.asFloatBuffer()
            mColorBuffer.put(colors)
            mColorBuffer.position(0)

            mIndexBuffer = ByteBuffer.allocateDirect(indices.size);
            mIndexBuffer.put(indices);
            mIndexBuffer.position(0);

        }






    }

    init{
        var r = Random()
        location = Vector(//
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 2, //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 2, //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() * 0.5f
        )
        velocity = Vector(//
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f, //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f, //
            (if (r.nextBoolean()) 1f else -1f) * r.nextFloat() / 100f
        )
    }



    fun draw(gl: GL10){
        gl.glFrontFace(GL10.GL_CCW)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 12, GL10.GL_UNSIGNED_BYTE,
            mIndexBuffer);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glFrontFace(GL10.GL_CW);

    }

    fun step(Preds: Array<Pred?>, neigbours: IntArray, distances: FloatArray) {
        val acceleration = flock(Preds, neigbours, distances)
        velocity.add(acceleration).limit(MAX_VELOCITY)
        location.add(velocity)
    }

    fun flock(Preds: Array<Pred?>, neighbours: IntArray, distances: FloatArray): Vector{
        var separation = separate(Preds,neighbours,distances).multiply(SEPARATION_WEIGHT)
        val alignment = align(Preds,neighbours).multiply(ALIGNMENT_WEIGHT)
        val cohesion = cohere(Preds,neighbours).multiply(COHESION_WEIGHT)
        return separation.add(alignment).add(cohesion)
    }

    private fun cohere(Preds: Array<Pred?>, neigbours: IntArray): Vector {
        sum.init()
        for (n in neigbours) {
            sum.add(Preds[n]!!.location)
        }
        sum.z /= 2
        return steerTo(sum.divide(neigbours.size + CENTRIC_POWER))
    }

    private fun steerTo(target: Vector): Vector {
        val desired = target.subtract(location)
        val d = desired.magnitude()
        var steer: Vector? = null

        if (d > 0) {
            desired.normalize()

            if (d < INERTION) {
                desired.multiply(MAX_VELOCITY * d / INERTION)
            } else {
                desired.multiply(MAX_VELOCITY)
            }

            steer = desired.subtract(velocity).limit(MAX_FORCE)
        } else {
            steer = Vector(0f, 0f, 0f)
        }
        return steer
    }

    private fun align(Preds: Array<Pred?>, neigbours: IntArray): Vector {
        align.init()
        for (n in neigbours) {
            align.add(Preds[n]!!.velocity)
        }
        align.divide(neigbours.size.toFloat())
        return align.limit(MAX_FORCE)
    }

    private fun separate(Preds: Array<Pred?>, neigbours: IntArray, distances: FloatArray): Vector {
        separate.init()
        var count = 0
        for (n in neigbours) {
            val Pred = Preds[n]
            val d = distances[n]
            tempVector.copyFrom(location).subtract(Pred!!.location)
            if (d > 0 && d < DESIRED_SEPARATION) {
                separate.add(tempVector.divide(Math.sqrt(d.toDouble()).toFloat()))
                count++
            }
        }
        if (count != 0) {
            separate.divide(count.toFloat())
        }
        return separate
    }

    fun copyFrom(Pred: Pred) {
        this.location.copyFrom(Pred.location)
        this.velocity.copyFrom(Pred.velocity)
    }
}