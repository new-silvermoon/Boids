package com.example.boids.util


import android.content.Context
import android.opengl.GLSurfaceView.Renderer
import android.util.Log
import android.view.Surface
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates
import android.R.attr.y
import android.R.attr.x
import android.text.method.TextKeyListener.clear
import javax.microedition.khronos.opengles.GL11
import android.opengl.GLES10.glRotatef
import android.R.attr.x
import android.R.attr.y
import android.opengl.GLES10.glTranslatef
import android.opengl.GLES10.glLoadIdentity
import android.opengl.GLES10.glEnableClientState
import android.opengl.GLES10.glLoadIdentity
import android.opengl.GLES10.glMatrixMode
import android.opengl.GLU
import android.R.attr.rotation
import android.support.v4.content.ContextCompat.getSystemService
import android.view.WindowManager
import android.opengl.GLES10.glShadeModel
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.graphics.Color
import com.example.Preds.util.Pred

class PredBoidRenderer (context: Context) : Renderer {

    companion object {
        val NEIGHBOURS = 7
        val PRED_NEIGHBOURS = 4
        var DISTANCE: Float by Delegates.notNull<Float>()
        val TAG = "BoidsRenderer"
    }


    lateinit var boids: Array<Boid?>
    lateinit var newBoids: Array<Boid?>
    lateinit var preds: Array<Pred?>
    lateinit var newPreds: Array<Pred?>
    var context: Context
    var rotation = Surface.ROTATION_0
    var width: Int by Delegates.notNull<Int>()
    var height: Int by Delegates.notNull<Int>()
    val tempVector = Vector(0.0f, 0.0f, 0.0f)
    lateinit var distances: Array<FloatArray>
    lateinit var neighbours: Array<IntArray>
    lateinit var predDistances: Array<FloatArray>
    lateinit var predNeighbours: Array<IntArray>
    lateinit var grid: Array<Array<BitSet?>>
    lateinit var predGrid: Array<Array<BitSet?>>
    val temp_dists = FloatArray(NEIGHBOURS)
    var ratio: Float by Delegates.notNull<Float>()
    var r: Float by Delegates.notNull<Float>()
    var g: Float by Delegates.notNull<Float>()
    var b: Float by Delegates.notNull<Float>()

    val startTime = System.currentTimeMillis()
    var dt: Long by Delegates.notNull<Long>()

    init {
        this.context = context
    }


    override fun onDrawFrame(gl: GL10?) {

        dt = System.currentTimeMillis() - startTime

        if (dt < 30) {
            try {
                Thread.sleep(30 - dt)
            } catch (e: InterruptedException) {

                Log.e(TAG, "error occured")
            }
        }

        calculateScene()

        gl?.glClearColor(r, g, b, 1.0f)
        gl?.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        gl?.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        gl?.glEnableClientState(GL11.GL_COLOR_ARRAY)
        for (boid in boids) {
            gl?.glLoadIdentity()
            gl?.glTranslatef(boid!!.location.x, boid.location.y, -DISTANCE + boid.location.z)

            tempVector.copyFrom(boid!!.velocity).normalize()
            val theta = Math.atan2(tempVector.y.toDouble(), tempVector.x.toDouble()).toFloat() * 57.3f
            val fi = Math.acos(tempVector.z.toDouble() / tempVector.magnitude()).toFloat() * 57.3f

            gl?.glRotatef(-90f, 0f, 0f, 1f)
            gl?.glRotatef(theta, 0f, 0f, 1f)
            gl?.glRotatef(fi, 0f, 1f, 0f)

            boid.draw(gl!!)
        }

        for (pred in preds){
            gl?.glLoadIdentity()
            gl?.glTranslatef(pred!!.location.x, pred.location.y,-DISTANCE + pred.location.z)

            tempVector.copyFrom(pred!!.velocity).normalize()
            val theta = Math.atan2(tempVector.y.toDouble(), tempVector.x.toDouble()).toFloat() * 57.3f
            val fi = Math.acos(tempVector.z.toDouble() / tempVector.magnitude()).toFloat() * 57.3f

            gl?.glRotatef(-90f, 0f, 0f, 1f)
            gl?.glRotatef(theta, 0f, 0f, 1f)
            gl?.glRotatef(fi, 0f, 1f, 0f)

            pred.draw(gl!!)
        }


    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        this.width = width
        this.height = height
        ratio = width.toFloat() / height

        rotation = (context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .rotation
        when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> DISTANCE = 12f
            Surface.ROTATION_90, Surface.ROTATION_270 -> DISTANCE = 12f / ratio
        }

        gl?.glViewport(0, 0, width, height)
        gl?.glMatrixMode(GL11.GL_PROJECTION)
        gl?.glLoadIdentity()
        GLU.gluPerspective(gl, 45f, ratio, DISTANCE - 5f, DISTANCE + 5f)
        gl?.glMatrixMode(GL11.GL_MODELVIEW)
        gl?.glLoadIdentity()

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val sp = PreferenceManager
            .getDefaultSharedPreferences(context)
        val size = sp.getInt("boidsSize", 4) / 100f
        val model = sp.getInt("model", -0xff6634)

        val predSize = 4/80f
        val predModel = -0x33ccff

        Boid.initModel(size, model)
        Pred.initModel(predSize,predModel)

        val backgroud = sp.getInt("background", -0x1000000)
        r = Color.red(backgroud) / 255f
        g = Color.green(backgroud) / 255f
        b = Color.blue(backgroud) / 255f

        //region Boids init
        val count = sp.getInt("boidsCount", 100)
        boids = arrayOfNulls<Boid>(count)
        newBoids = arrayOfNulls<Boid>(boids.size)
        for (i in 0 until boids.size) {
            boids[i] = Boid()
            newBoids[i] = Boid()
        }
        distances = Array(boids.size) { FloatArray(boids.size) }
        neighbours = Array(boids.size) { IntArray(NEIGHBOURS) }
        grid = Array(64) { arrayOfNulls<BitSet>(64) }
        for (i in 0 until grid.size) {
            for (j in 0 until grid.size) {
                grid[i][j] = BitSet()
                grid[j][i] = BitSet()
            }
        }
        //endregion

        //region Pred init
        val predCount = 50
        preds = arrayOfNulls<Pred>(predCount)
        newPreds = arrayOfNulls<Pred>(preds.size)
        for (i in 0 until preds.size) {
            preds[i] = Pred()
            newPreds[i] = Pred()
        }
        predDistances = Array(preds.size) { FloatArray(preds.size) }
        predNeighbours = Array(preds.size) { IntArray(PRED_NEIGHBOURS) }
        predGrid = Array(64) { arrayOfNulls<BitSet>(64) }
        for (i in 0 until predGrid.size) {
            for (j in 0 until predGrid.size) {
                predGrid[i][j] = BitSet()
                predGrid[j][i] = BitSet()
            }
        }
        //endregion

        gl?.glClearDepthf(1.0f)
        gl?.glEnable(GL10.GL_DEPTH_TEST)
        gl?.glDepthFunc(GL10.GL_LEQUAL)
        gl?.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        gl?.glShadeModel(GL10.GL_SMOOTH)
        gl?.glDisable(GL10.GL_DITHER)


    }

    private fun index(x: Float): Int {
        var x = x
        if (Math.abs(x) > 4f) {
            x = (if (x > 0) 1 else 1) * 4f
        }
        x += 4f
        x *= 3f
        if (x >= 24) {
            x = 23f
        }
        return Math.floor(x.toDouble()).toInt()
    }

    private fun calculateScene() {
        for (i in 0 until grid.size) {
            for (j in 0 until grid.size) {
                grid[i][j]!!.clear()
                grid[j][i]!!.clear()
            }
        }

        for (i in 0 until predGrid.size) {
            for (j in 0 until predGrid.size) {
                predGrid[i][j]!!.clear()
                predGrid[j][i]!!.clear()
            }
        }

        for (b in 0 until boids.size) {
            val i = index(boids[b]!!.location.x)
            val j = index(boids[b]!!.location.y)
            grid[i][j]!!.set(b)
        }

        for (b in 0 until preds.size) {
            val i = index(preds[b]!!.location.x)
            val j = index(preds[b]!!.location.y)
            predGrid[i][j]!!.set(b)
        }

        for (b in 0 until boids.size) {
            var count = 0
            val x = index(boids[b]!!.location.x)
            val y = index(boids[b]!!.location.y)
            var r = 0
            while (count < neighbours[b].size) {
                var i = Math.max(0, x - r)
                while (i <= x + r && i < grid.size) {
                    var j = Math.max(0, y - r)
                    while (j <= y + r && j < grid.size) {
                        if (Math.abs(x - i) == r || Math.abs(y - j) == r) {
                            var bit = 0
                            while (grid[i][j]!!.nextSetBit(bit) >= 0 && count < neighbours[b].size && !grid[i][j]!!.isEmpty) {
                                bit = grid[i][j]!!.nextSetBit(bit)
                                neighbours[b][count] = bit
                                count++
                                bit++
                            }
                        }
                        j++
                    }
                    i++
                }
                r++
            }




            for (n in 0 until neighbours[b].size) {
                val distance = tempVector.copyFrom(boids[b]!!.location)
                    .subtract(boids[neighbours[b][n]]!!.location).magnitude2()
                distances[b][neighbours[b][n]] = distance
            }
        }

        for (p in 0 until preds.size) {
            var count = 0
            val x = index(preds[p]!!.location.x)
            val y = index(preds[p]!!.location.y)
            var r = 0
            while (count < predNeighbours[p].size) {
                var i = Math.max(0, x - r)
                while (i <= x + r && i < predGrid.size) {
                    var j = Math.max(0, y - r)
                    while (j <= y + r && j < predGrid.size) {
                        if (Math.abs(x - i) == r || Math.abs(y - j) == r) {
                            var bit = 0
                            while (predGrid[i][j]!!.nextSetBit(bit) >= 0 && count < predNeighbours[p].size && !predGrid[i][j]!!.isEmpty) {
                                bit = predGrid[i][j]!!.nextSetBit(bit)
                                predNeighbours[p][count] = bit
                                count++
                                bit++
                            }
                        }
                        j++
                    }
                    i++
                }
                r++
            }




            for (n in 0 until predNeighbours[p].size) {
                val distance = tempVector.copyFrom(preds[p]!!.location)
                    .subtract(preds[predNeighbours[p][n]]!!.location).magnitude2()
                predDistances[p][predNeighbours[p][n]] = distance
            }
        }

        for (b in 0 until boids.size) {
            newBoids[b]!!.copyFrom(boids[b]!!)
        }

        for (b in 0 until boids.size) {
            boids[b]!!.step(newBoids, neighbours[b], distances[b])
        }

        for (p in 0 until preds.size) {
            newPreds[p]!!.copyFrom(newPreds[p]!!)
        }

        for (p in 0 until preds.size) {
            preds[p]!!.step(newPreds, predNeighbours[p], predDistances[p])
        }
    }

    fun touch(x: Float, y: Float) {


        lateinit var relxList : FloatArray
        lateinit var relyList : FloatArray
        relxList = FloatArray(boids.size)
        relyList = FloatArray(boids.size)

        for (i in 0..(boids.size-1)){
            relxList[i] = boids[i]!!.location.x
            relyList[i] = boids[i]!!.location.y
        }


       for (i in 0..(preds.size -1)){
           preds[i]!!.velocity.x = relxList[i] - preds[i]!!.location.x
           preds[i]!!.velocity.y = relyList[i] - preds[i]!!.location.y
           preds[i]!!.velocity.z = 0 - preds[i]!!.location.z
           preds[i]!!.velocity.copyFrom(
               tempVector.copyFrom(preds[i]!!.velocity)
                   .normalize()
           )
       }

    }
}
