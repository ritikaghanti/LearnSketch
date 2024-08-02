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

import android.view.MotionEvent

/**
 * State represents the tool state at a single point in time. Objects of
 * this class provide a snapshot of tool location, pressure, etc. Data
 * may be constructed directly from a MotionEvent's current or historic
 * values, or interpolated between other states.
 *
 * @author LearnSketch
 */
class State internal constructor(
    var time: Long,
    var x: Float,
    var y: Float,
    var pressure: Float,
    var size: Float
) {
    /**
     * Obtain a State from the given MotionEvent's history buffer.
     *
     * @param e    Event to use as the data source
     * @param pos  Index for each call to 'getHistoricalFoo'
     */
    internal constructor(e: MotionEvent, pos: Int) : this(
        e.getHistoricalEventTime(pos),
        e.getHistoricalX(pos),
        e.getHistoricalY(pos),
        e.getHistoricalPressure(pos),
        e.getHistoricalSize(pos)
    ) {
    }

    /**
     * Obtain a State from the given MotionEvent's most-current data.
     *
     * @param e  Event to use as the data source.
     */
    internal constructor(e: MotionEvent) : this(
        e.eventTime,
        e.x,
        e.y,
        e.pressure,
        e.size
    ) {
    }

    override fun toString(): String {
        return String.format(
            "State(%d, %f, %f, %f, %f)",
            time, x, y, pressure, size
        )
    }

    companion object {
        /**
         * Obtain an array of States, one for each historic and current data
         * point contained in the provided MotionEvent.
         *
         * @param event  The event to create all the States from
         * @return       An array of states, one for each point in time
         */
        fun getStates(event: MotionEvent): Array<State?> {
            val n = event.historySize
            val states = arrayOfNulls<State>(n + 1)
            for (i in 0 until n) {
                states[i] = State(event, i)
            }
            states[n] = State(event)
            return states
        }

        /**
         * Obtain a State which is linearly interpolated between two others.
         * The degree of interpolation is specified by 'frac'. As 'frac' is
         * increased, the interpolated State will resemble 'a' less and 'b'
         * more.
         *
         * @param a     The "first" state
         * @param b     The "second" state
         * @param frac  The fraction to use in interpolation, between 0 and 1
         * @return      A linear interpolation of states 'a' and 'b'
         */
        fun interpolate(a: State, b: State, frac: Float): State {
            return State(
                interpolate(a.time.toFloat(), b.time.toFloat(), frac).toLong(),
                interpolate(a.x, b.x, frac),
                interpolate(a.y, b.y, frac),
                interpolate(a.pressure, b.pressure, frac),
                interpolate(a.size, b.size, frac)
            )
        }

        private fun interpolate(a: Float, b: Float, frac: Float): Float {
            return (b - a) * frac + a
        }

        fun distance(a: State, b: State): Float {
            return Math.sqrt(
                Math.pow(
                    (b.x - a.x).toDouble(),
                    2.0
                ) + Math.pow((b.y - a.y).toDouble(), 2.0)
            ).toFloat()
        }
    }
}