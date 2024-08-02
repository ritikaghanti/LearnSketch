
package com.example.learnsketch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnHoverListener
import android.view.View.OnTouchListener
import org.checkerframework.checker.units.qual.min
import java.io.File
import kotlin.math.min


/**
 * CanvasView provides a View that can be drawn on by consuming touch,
 * pen, and other MotionEvents. Each device is assigned its own Brush.
 *
 */
class ProSketchCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs), OnTouchListener,
    OnHoverListener {
    var brushes: MutableMap<Int, Brush> = HashMap()
    var brush: Brush? = null
    lateinit var transform: Matrix
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

//    lateinit var refbitmap
//            : Bitmap

    lateinit var refCanvas
            : Canvas

    lateinit var gridPaint
            : Paint
    lateinit var grids:
            Bitmap
    lateinit var gridCanvas:  // Canvas for grid overlay
            Canvas

    var grab: PointF? = null


//    var brush: Brush? = null
//    var transform: Matrix? = null
//    var inverse // Transform between view-space and bitmap-space
//            : Matrix? = null
//    var checker: Bitmap? = null
//    var layer // Layer containing the drawing
//            : Bitmap? = null
//    var overlay // Overlay for fill "shadow"
//            : Bitmap? = null
//    var layerCanvas // Canvas for drawing into 'layer'
//            : Canvas? = null
//    var overlayCanvas // Canvas for drawing into 'overlay'
//            : Canvas? = null
//    var grab: PointF? = null

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
        Log.d("ProSketch canvas", "Entered")
        initBitmaps()
        if (HomeScreen.isSavedSketchClicked == true) {
            layer = ProSketchActivity.proskbitmap
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
        canvas.drawBitmap(layer, transform, null)
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
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> brush!!.drawFill(
                layerCanvas, states
            )
            MotionEvent.ACTION_UP -> {
                brush!!.drawFill(layerCanvas, states)
                brush!!.endFill()
            }
            MotionEvent.ACTION_CANCEL -> brush!!.endFill()
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
//        val viewWidth = width
//        val viewHeight = height
        val viewWidth = 2440
        val viewHeight = 1266
        val canvasWidth = Math.round(0.5f * viewWidth)
        val canvasHeight = Math.round(0.5f * viewHeight)

        val canvasSize = min(viewHeight, viewWidth)
        initBitmaps(viewWidth, viewHeight)
//        initBitmaps((canvasSize), canvasSize)
        brush?.size = 10
//        transform.preScale(5f,5f)

//        transform.postTranslate(
//            ((viewWidth - canvasSize) / 2).toFloat(),
//            ((viewHeight - canvasSize) / 2).toFloat()
//        )
//        transform.postScale(-1f,-1f)
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



//        getRefBitmap()
//        refbitmap = refbitmap.copy(Bitmap.Config.ARGB_8888,true)
//        refCanvas = Canvas(refbitmap)

        grids = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        gridCanvas = Canvas(grids)
        drawGrids()
        layer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        layerCanvas = Canvas(layer)
        layerCanvas.drawARGB(255, 255, 255, 255)
        overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        overlayCanvas = Canvas(overlay)
        transform = Matrix()
        inverse = Matrix()


    }

    private fun drawGrids() {
        // Define the number of grids in each dimension
        val numGrids = ProSketchActivity.Companion.grid

        // Calculate the size of each subregion
        val subregionWidth = gridCanvas.width / numGrids
        val subregionHeight = gridCanvas.height / numGrids

        gridPaint = Paint()
        gridPaint.setColor(Color.BLACK)
        gridPaint.strokeWidth = 2f
        gridPaint.alpha = 50

        // Draw horizontal grid lines
        for (i in 1 until numGrids) {
            val y = i * subregionHeight.toFloat()
            gridCanvas.drawLine(0f, y, gridCanvas.width.toFloat(), y, gridPaint)
        }

        // Draw vertical grid lines
        for (i in 1 until numGrids) {
            val x = i * subregionWidth.toFloat()
            gridCanvas.drawLine(x, 0f, x, gridCanvas.height.toFloat(), gridPaint)
        }
    }

    /*private fun getRefBitmap() {
        val imgFile = File(Analyze.refimfilepath)
        if (imgFile.exists()) {

//            refbitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())

            //Drawable d = new BitmapDrawable(getResources(), myBitmap);
        }


    }*/

    public fun hideGrids() {
        gridCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    public fun showGrids() {
        drawGrids()
    }
}