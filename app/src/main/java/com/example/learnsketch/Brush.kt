/**
 * Copyright (c) 2013, 2020 Wacom Technology Corp.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.learnsketch

import android.graphics.*

/**
 * A Brush describes how strokes are rendered. Brushes have attributes like
 * size, hardness, and color.
 *
 * @author LearnSketch
 */
class Brush @JvmOverloads constructor(
    var spacing: Int = 10,
    var size: Int = 20,
    var hardness: Int = 20
) {
    var fill: Bitmap
    var outline: Bitmap
    var fillCanvas: Canvas
    var outlineCanvas: Canvas
    var fillPaint: Paint
    var outlinePaint: Paint
    var last: State? = null
    var current_radius = 0
    var foreground = Color.BLACK
    /**
     * Create a Brush from defined settings.
     *
     * @param spacing   Percent of brush radius to move before re-stamping the brush
     * @param size      Maximum size the brush can take on
     * @param hardness  Maximum hardness the brush can take on
     */
    /**
     * Create a Brush with some basic default settings.
     */
    init {
        fill = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        fillCanvas = Canvas(fill)
        fillPaint = Paint()
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = Color.BLACK
        outline = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        outlineCanvas = Canvas(outline)
        outlinePaint = Paint()
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.color = Color.BLACK
    }

    /**
     * Ends a filled brush stroke that is currently taking place. Calling
     * this method prevents the end of one stroke from being automatically
     * connected to the begining of the next (as drawFill does by default).
     *
     * @see drawFill
     */
    fun endFill() {
        last = null
    }

    /**
     * Update the color of the brush.
     *
     * @param foreground  New brush color (e.g. 0xff000000 or Color.BLACK for black)
     */
    fun setColor(foreground: Int) {
        this.foreground = foreground
    }

    /**
     * Draw the brush as a smooth stroke through all the given states.
     * In-between states will be interpolated as necessary, matching the
     * brush's defined spacing. The final state in the array will be
     * remembered to let the next call to this function continue drawing
     * the same stroke. To begin drawing a new stroke, call 'endFill'.
     *
     * @param canvas  Canvas to draw into
     * @param state   Array of states to draw the stroke along
     * @see endFill
     */
    fun drawFill(canvas: Canvas, state: Array<State?>) {
        if (last != null) drawFill(canvas, last, state[0])
        for (i in 1 until state.size) {
            drawFill(canvas, state[i - 1], state[i])
        }
        last = state[state.size - 1]
    }

    /**
     * Draw the brush as a smooth stroke between the two given states.
     * In-between states will be interpolated as necessary, matching
     * the brush's defined spacing.
     *
     * @param canvas  Canvas to draw into
     * @param a       State to begin drawing stroke at
     * @param b       State to end drawing stroke at
     */
    fun drawFill(canvas: Canvas, a: State?, b: State?) {
        if (a != null && b != null) {
            val dist = State.distance(a, b)
            var d = 0f
            do {
                var frac = 0f
                if (dist != 0f) frac = d / dist
                val s = State.interpolate(a, b, frac)
                drawFill(canvas, s)
                d += (2 * current_radius * spacing / 100.0).toFloat()
            } while (d < dist)
        }
    }

    /**
     * Stamp the currently-rendered fill onto the provided canvas.
     *
     * @param canvas  Canvas to draw into
     * @param s       State to use for drawing
     */
    fun drawFill(canvas: Canvas, s: State) {
        val x = s.x - fill.width / 2f
        val y = s.y - fill.height / 2f
        render(foreground, Math.min(s.pressure, 1f), 1f)
        canvas.drawBitmap(fill, x, y, null)
    }

    /**
     * Stamp the currently-rendered outline onto the provided canvas.
     *
     * @param canvas  Canvas to draw into
     * @param s       State to use for drawing
     */
    fun drawOutline(canvas: Canvas, s: State) {
        val x = s.x - outline.width / 2f
        val y = s.y - outline.height / 2f
        render(foreground, Math.min(s.pressure, 1f), 1f)
        canvas.drawBitmap(outline, x, y, null)
    }

    /**
     * Update the image that will be stamped by the brush with each
     * call to drawFill and drawOutline. Settings like color and
     * radius may be changed at any time to provide dynamic brush
     * strokes.
     *
     * @param color     Fill color to use for the brush
     * @param radius    Radius as a fraction of maximum size
     * @param hardness  Hardness as a fraction of maximum hardness
     */
    fun render(color: Int, radius: Float, hardness: Float) {
        var radius = radius
        var hardness = hardness
        require(!(hardness < 0 || hardness > 1)) { "Hardness may only take on values between 0 and 1 (inclusive)" }
        require(!(radius < 0 || radius > 1)) { "Radius may only take on values between 0 and 1 (inclusive)" }
        val center = size / 2f
        radius *= size / 2f
        hardness *= this.hardness / 100.0f
        current_radius = Math.ceil(radius.toDouble()).toInt()
        fillCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        outlineCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (current_radius > 0) {
            val gradient = RadialGradient(
                center,
                center,
                radius,
                intArrayOf(color, color, color and 0x00ffffff),
                floatArrayOf(0.0f, hardness, 1.0f),
                Shader.TileMode.CLAMP
            )
            fillPaint.shader = gradient
            fillCanvas.drawCircle(center, center, radius, fillPaint)
        }
        outlinePaint.color = color
        outlineCanvas.drawCircle(center, center, center, outlinePaint)
    }
}