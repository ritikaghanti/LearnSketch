
package com.example.learnsketch

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnHoverListener
import android.view.View.OnTouchListener
import android.widget.Toast
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import kotlin.math.min


class CanvasView(context: Context?, attrs: AttributeSet?) : View(context, attrs), OnTouchListener,
    OnHoverListener {
    var brushes: MutableMap<Int, Brush> = HashMap()
    var brush: Brush? = null

    var scaleGestureDetector: ScaleGestureDetector? = null

    companion object {
        lateinit var transform: Matrix
    }


    lateinit var inverse // Transform between view-space and bitmap-space
            : Matrix
    lateinit var checker: Bitmap
    lateinit var layer // Layer containing the drawing
            : Bitmap
    lateinit var overlay // Overlay for fill "shadow"
            : Bitmap
    lateinit var layerCanvas // Canvas for drawing into 'layer'
            : Canvas
    lateinit var overlayCanvas // Canvas for drawing into 'overlay'
            : Canvas

    lateinit var refbitmap
            : Bitmap

    lateinit var refCanvas
            : Canvas

    lateinit var gridPaint
            : Paint
    lateinit var grids:
            Bitmap
    lateinit var gridCanvas:  // Canvas for grid overlay
            Canvas

    lateinit var subregionBitmap: Array<Array<Bitmap>>
    lateinit var subregionrefbitmap
            : Array<Array<Bitmap>>
    lateinit var subregionCanvas: Canvas
    lateinit var subregionrefCanvas: Canvas

    var subRegionX: Int? = null
    var subRegionY: Int? = null


    var subregion: Int? = null
    var subRegionWidth: Int? = null
    var subRegionHeight: Int? = null
    var numGrids: Int? = null
    var canvasSize: Int? = null
    var grab: PointF? = null
    var left: Int? = null
    var right: Int? = null
    var top: Int? = null
    var bottom: Int? = null

    var refsize: Int = 0

    var subregionrefstring: String? = null
    var subregionsketchstring: String? = null


    /**
     * Create a new CanvasView. Note that this constructor does not
     * initialize the bitmaps. Be sure that one of the two "initBitmaps"
     * functions is called before "onDraw" is called.
     *
     * @param context
     * @param attrs
     */
    init {
        setOnTouchListener(this)
        setOnHoverListener(this)
    }

    /**
     * This method is called "when this view should assign a size and
     * position to all of its children". We use this chance to initialize
     * the bitmaps based on the view's now-known size.
     */
    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        initBitmaps()
        if (HomeScreen.isSavedSketchClicked == true) {
            layer = DrawActivity.savedsketchbmp

        }
    }

    /**
     * This method is called whenever Android requires us to redraw
     * ourselves. We blit each of the three bitmaps to the provided
     * canvas in bottom-up order, transforming them by the current
     * viewport transformation.
     *
     * @param canvas
     */
    public override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(checker, transform, null)

        canvas.drawBitmap(refbitmap, transform, null)

        canvas.drawBitmap(layer, transform, null)   // mainsketching


        canvas.drawBitmap(grids, transform, null)


        canvas.drawBitmap(overlay, transform, null)
    }

    /**
     * This method is called whenever Android has a touch event for us
     * to process. This occurs whenever a finger or stylus is in contact
     * with the screen.
     *
     * We respond to these events by attempting to move the viewport,
     * and drawing both the fill and outline.
     *
     * @param view
     * @param event
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var handled = false
        changeTool(event)
        if (moveViewport(event)) return true
        scaleGestureDetector?.onTouchEvent(event)
//        if (getSubRegion(event)) return true
        handled = handled or drawFill(event)
        handled = handled or drawOutline(event)
        return handled

    }

    /**
     * This method is called whenever Android has a hover event for us
     * to process. An active stylus will send hover events while it is
     * in range of the sensor, but not in contact.
     *
     * We respond to these events by attempting to move the viewport
     * and redrawing the brush outline.
     *
     * @param v
     * @param event
     */
    override fun onHover(v: View, event: MotionEvent): Boolean {
        changeTool(event)
        return if (moveViewport(event)) true else drawOutline(event)
    }

    /**
     * Change the color of the active brush.
     *
     * @param color  New brush color (e.g. 0xff000000 or Color.BLACK for black)
     */
    fun setColor(color: Int) {
        if (brush == null) return
        brush!!.setColor(color)
    }

    /**
     * Attempt to draw the brush fill to the layer bitmap. This fill will
     * be drawn so long as a touch is occurring. Once the touch ends, the
     * active brush will be signaled to stop drawing the stroke, in
     * preparation for the next stroke.
     *
     * The raw MotionEvent data is transformed from being View-relative to
     * being viewport-relative.
     *
     * @param event  Event to attempt to use for drawing the fill
     * @return       'true' if the event is used to draw the fill
     */
    protected fun drawFill(event: MotionEvent): Boolean {
        val states = State.getStates(event)
        transformState(states, inverse)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {


                try {
                    getSubRegion(event, states)
                } catch (e: Exception) {
                    Log.d("DetectSubrgion Error", e.printStackTrace().toString())
                }

                brush!!.drawFill(layerCanvas, states)

            }
            MotionEvent.ACTION_UP -> {

                try {


                    var drawactiv: DrawActivity = DrawActivity()

                    brush!!.drawFill(layerCanvas, states)
                    //                subregionCanvas.drawBitmap(layer, -subRegionX!! * subRegionWidth!!.toFloat(), -subRegionY!! * subRegionHeight!!.toFloat(), null)
                    brush!!.endFill()
                    subregionCanvas.drawBitmap(
                        layer,
                        Rect(left!!, top!!, right!!, bottom!!),
                        Rect(0, 0, subRegionWidth!!, subRegionHeight!!),
                        null
                    )
                    subregionrefCanvas.drawBitmap(
                        refbitmap,
                        Rect(left!!, top!!, right!!, bottom!!),
                        Rect(0, 0, subRegionWidth!!, subRegionHeight!!),
                        null
                    )
                    println("Pen Lifted")

                    subregionrefstring =
                        drawactiv.convertImgtoString(subregionrefbitmap[subRegionX!!][subRegionY!!])
                    subregionsketchstring =
                        drawactiv.convertImgtoString(subregionBitmap[subRegionX!!][subRegionY!!])

                    //                DrawActivity.Companion.preview.setImageBitmap(subregionrefbitmap[subRegionX!!][subRegionY!!])
                } catch (e: Exception) {
                    print(e)
                }
                //                -----------------------
                // CALL TO PYTHON SCRIPTS FOR IMAGE COMPARISION
//                val handler = Handler(Looper.getMainLooper())
                val handler = Handler(Looper.getMainLooper())

                Thread {
                    try {
                        if (!Python.isStarted()) {
                            Python.start(AndroidPlatform(this.context))
                        }
                        val py = Python.getInstance()
                        val module = py.getModule("DetectError")

                        print("module: $module")
                        print("analyze button clicked")

                        val pyobj: List<PyObject> =
                            module.callAttr(
                                "detectError",
                                subregionrefstring,
                                subregionsketchstring
                            )
                                .asList()
                        var resultimgstring = pyobj.get(0).toString()
                        var ssimscore = pyobj.get(1).toString()

                        // Update UI elements on the main thread using Handler
                        handler.post {

                            DrawActivity.ssimtv.text = "SSIM Score: $ssimscore"
                            Log.d("SSIMSCORE", ssimscore)

                            var resultdata: ByteArray =
                                Base64.decode(resultimgstring, Base64.DEFAULT)
                            val resultbitmap =
                                BitmapFactory.decodeByteArray(resultdata, 0, resultdata.size)
                            DrawActivity.errorpreview.visibility = View.VISIBLE
                            DrawActivity.errorpreview.setImageBitmap(resultbitmap)
                        }
                    } catch (e: java.lang.Exception) {
                        print(e.message)
                    }
                }.start()
            }
            MotionEvent.ACTION_CANCEL -> {
                brush!!.endFill()
            }
            else -> return false
        }
        invalidate()
        return true
    }

    /**
     * Attempt to draw the brush outline as an overlay. This outline will
     * be drawn so long as a touch is occurring or the tool is hovering.
     * To ensure old outlines do not persist on the overlay bitmap, each
     * successful call results in it being cleared and redrawn.
     *
     * The raw MotionEvent data is transformed from being view-relative
     * to being viewport-relative.
     *
     * @param event  Event to attempt to use for drawing the outline
     * @return       'true' if the event is used to draw the outline
     */
    protected fun drawOutline(event: MotionEvent): Boolean {
        val states = arrayOf<State?>(State(event))
        transformState(states, inverse)
        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> brush!!.drawOutline(
                overlayCanvas, states[0]!!
            )
            else -> return false
        }
        invalidate()
        return true
    }

    /**
     * Attempt to move the bitmaps (the "viewport") around the CanvasView.
     * While a non-primary button is pressed, the canvas can be dragged
     * around.
     *
     * @param event  Event to attempt to move the viewport with
     * @return       'true' if the event is used to move the viewport
     */
    protected fun moveViewport(event: MotionEvent): Boolean {
        // XXX: Comment out the entirety of this 'if' statement if
        //building for API level < 14. Canvas dragging will not be
        //supported.
        if (event.buttonState != 0 &&
            event.buttonState != MotionEvent.BUTTON_PRIMARY
        ) {
            val x = event.x
            val y = event.y
            if (grab != null) {
                transform.postTranslate(x - grab!!.x, y - grab!!.y)
                transform.invert(inverse)
            }
            grab = PointF(x, y)
            invalidate()
            return true
        }
        grab = null
        return false
    }

    protected fun getSubRegion(event: MotionEvent, state: Array<State?>) {

        val last = state[state.size - 1]
        val numSubRegionsX = 4
        val numSubRegionsY = 4
        val touchX = last!!.x.toInt()
        val touchY = last!!.y.toInt()

//        Log.d("touchx", touchX.toString())
//        Log.d("touchy", touchY.toString())
        // Calculate the subregion that the touch event is in
        subRegionX = touchX / subRegionWidth!!
        subRegionY = touchY / subRegionHeight!!

        // Calculate the bounds of the subregion
        left = subRegionX!! * subRegionWidth!!
        top = subRegionY!! * subRegionHeight!!
        right = left!! + subRegionWidth!!
        bottom = top!! + subRegionHeight!!

        subregionBitmap = Array(numSubRegionsX) {
            Array(numSubRegionsY) {
                Bitmap.createBitmap(
                    subRegionWidth!!,
                    subRegionHeight!!,
                    Bitmap.Config.ARGB_8888
                )
            }
        }
        subregionrefbitmap = Array(numSubRegionsX) {
            Array(numSubRegionsY) {
                Bitmap.createBitmap(
                    subRegionWidth!!,
                    subRegionHeight!!,
                    Bitmap.Config.ARGB_8888
                )
            }
        }


        // Get the canvas for the subregion bitmap
        subregionCanvas = Canvas(subregionBitmap[subRegionX!!][subRegionY!!])
        subregionrefCanvas = Canvas(subregionrefbitmap[subRegionX!!][subRegionY!!])

//
//        subregionBitmap = Bitmap.createBitmap(subRegionWidth!!, subRegionHeight!!, Bitmap.Config.ARGB_8888)
//        subregionCanvas = Canvas(subregionBitmap)
//        subregionCanvas.clipRect(subRegionX!! * subRegionWidth!!, subRegionY!! * subRegionHeight!!, (subRegionX!! + 1) * subRegionWidth!!, (subRegionY!! + 1) * subRegionHeight!!)


        Log.d("Left", left.toString())
        Log.d("Right", right.toString())
        Log.d("Top", top.toString())
        Log.d("bottom", bottom.toString())

        //DEBUG
//
//        val textpaint = Paint()
//        textpaint.setColor(Color.RED)
//        textpaint.strokeWidth = 3f
//        gridCanvas.drawText(
//            "X: $touchX , Y: $touchY",
//            (touchX - 5f).toFloat(),
//            (touchY + 5f).toFloat(),
//            textpaint
//        )
//
//
//        //Marking the Subregion (Testing Purposes Only)
//        val marker = Paint()
//        marker.setColor(Color.RED)
//        marker.strokeWidth = 3f
//        marker.setStyle(Paint.Style.STROKE)
//
//        gridCanvas.drawRect(
//            left!!.toFloat(),
//            top!!.toFloat(),
//            right!!.toFloat(),
//            bottom!!.toFloat(),
//            marker
//        )


    }

    /**
     * Switch the currently-active tool to the tool associated with the
     * provided event. Each MotionEvent stores the ID of the device which
     * generated it, as well as the tool type. This information is combined
     * into a single key and used to search the "brushes" hashmap.
     *
     * @param event  Event to locate new tool with
     */
    protected fun changeTool(event: MotionEvent) {
        var key = event.deviceId

        // XXX: Comment out this line if building for API level < 14.
        key = key or (event.getToolType(0) shl 8)
        brush = brushes[key]
        if (brush == null) {
            brush = Brush()
            brushes[key] = brush!!
        }
    }

    /**
     * Transform an array of States by a given matrix. This is used to
     * relocate the coordinates from being relative to the View (as
     * contained in MotionEvents) to being relative to the bitmaps
     * being drawn on.
     *
     * @param states     Array of States to transform
     * @param transform  Matrix transformation to apply to each State
     */
    protected fun transformState(states: Array<State?>, transform: Matrix?) {
        for (i in states.indices) {
            val loc = floatArrayOf(states[i]!!.x, states[i]!!.y)
            transform!!.mapPoints(loc)
            states[i]!!.x = loc[0]
            states[i]!!.y = loc[1]
        }
    }

    /**
     * Initialize the various bitmaps that are blited to the screen.
     * This method uses the current view size to determine appropriate
     * dimensions.
     */
    fun initBitmaps() {
        val viewWidth = width
        val viewHeight = height
        val canvasWidth = Math.round(0.5f * viewWidth)
        val canvasHeight = Math.round(0.5f * viewHeight)
        try {
            getRefBitmap()
        } catch (e: java.lang.Exception) {
            println(e.printStackTrace())
        }


//        val canvasSize = min(viewHeight, viewWidth)
        canvasSize = refsize
//        initBitmaps(canvasWidth, canvasHeight)
        initBitmaps(canvasSize!!, canvasSize!!)
        brush?.size = 10
//        transform.preScale(5f,5f)

        transform.postTranslate(
            ((viewWidth - canvasSize!!) / 2).toFloat(),
            ((viewHeight - canvasSize!!) / 2).toFloat()
        )
//        transform.postScale(2f, 2f)
        transform.invert(inverse)

    }

    /**
     * Initialize the various bitmaps that are blited to the screen.
     * In addition to initializing the bitmap that is drawn to, this
     * also initializes the "checker" bitmap for visualizing the alpha
     * channel and the "overlay" bitmap for visualizing the current
     * tool location.
     *
     * @param w  Width of the bitmap to draw on
     * @param h  Height of the bitmap to draw on
     */
    protected fun initBitmaps(w: Int, h: Int) {
        var w = w
        var h = h
        if (w <= 0) {
            w = 1
        }
        if (h <= 0) {
            h = 1
        }
        checker = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        val p = Paint()
        p.shader = BitmapShader(
            BitmapFactory.decodeResource(resources, R.drawable.checker),
            Shader.TileMode.REPEAT, Shader.TileMode.REPEAT
        )


        Canvas(checker).drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)


        refbitmap = refbitmap.copy(Bitmap.Config.ARGB_8888, true)
        refCanvas = Canvas(refbitmap)


        // Refimage Opacity
        refCanvas.drawARGB(100, 255, 255, 255)


        layer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        layerCanvas = Canvas(layer)
        layerCanvas.drawRGB(255, 255, 255)


        grids = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        gridCanvas = Canvas(grids)
        drawGrids()

        overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        overlayCanvas = Canvas(overlay)


        transform = Matrix()
        inverse = Matrix()

        scaleGestureDetector = ScaleGestureDetector(this.context, CustomScaleGestureDetector())


    }


    private fun drawGrids() {
        //Define the number of grids in each dimension

        val numGrids = DrawActivity.Companion.grid

        // Calculate the size of each subregion
        subRegionWidth = gridCanvas.width / numGrids
        subRegionHeight = gridCanvas.height / numGrids

        gridPaint = Paint()
        gridPaint.setColor(Color.BLACK)
        gridPaint.strokeWidth = 2f
        gridPaint.alpha = 50

        // Draw horizontal grid lines
        for (i in 1 until numGrids) {
            val y = i * subRegionHeight!!.toFloat()
            gridCanvas.drawLine(0f, y, gridCanvas.width.toFloat(), y, gridPaint)
        }

        // Draw vertical grid lines
        for (i in 1 until numGrids) {
            val x = i * subRegionWidth!!.toFloat()
            gridCanvas.drawLine(x, 0f, x, gridCanvas.height.toFloat(), gridPaint)
        }
    }

    private fun getRefBitmap() {
        val options = BitmapFactory.Options()
        if (!HomeScreen.isSavedSketchClicked) {
            val imgFile = File(Analyze.refimfilepath)

            if (imgFile.exists()) {

                refbitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options)
                refsize = options.outHeight

            }
        } else {
            val imgFile = File(DrawActivity.pulled_refimgpath)

            if (imgFile.exists()) {

                refbitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options)
                refsize = options.outHeight

            }

        }


    }

    public fun setReferenceBitmap() {
        refCanvas.setBitmap(refbitmap)
        invalidate()
    }

    public fun setLayerBitmap(bit: Bitmap?) {
        layerCanvas.setBitmap(bit)
    }

    public fun hideGrids() {
        gridCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    public fun showGrids() {
        drawGrids()
    }

    public fun zoomIn() {
        if (transform == null) return
        transform.postScale(2f, 2f)

    }

    public fun zoomOut() {
        if (transform == null) return
        transform.postScale(-1f, -1f)

    }

    inner class CustomScaleGestureDetector : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 1) {
                // Zooming Out
                transform.postScale(scaleFactor, scaleFactor)

            } else {

                transform.postScale(scaleFactor, scaleFactor)
                // Zooming In
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {}
    }

}