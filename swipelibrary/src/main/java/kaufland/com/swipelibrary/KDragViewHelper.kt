package kaufland.com.swipelibrary

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.VelocityTrackerCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ScrollerCompat
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Interpolator

import java.util.Arrays

/**
 * Created by sbra0902 on 17.03.17.
 */

class KDragViewHelper
/**
 * Apps should use ViewDragHelper.create() to get a new instance.
 * This will allow VDH to use internal compatibility implementations for different
 * platform versions.
 *
 * @param context Context to initialize config-dependent params from
 * @param forParent Parent view to monitor
 */
private constructor(context: Context, private val mParentView: ViewGroup?, private val mCallback: android.support.v4.widget.ViewDragHelper.Callback?) {

    // Current drag state; idle, dragging or settling
    /**
     * Retrieve the current drag state of this helper. This will return one of
     * [.STATE_IDLE], [.STATE_DRAGGING] or [.STATE_SETTLING].
     * @return The current drag state
     */
    var viewDragState: Int = 0
        private set

    // Distance to travel before a drag may begin
    /**
     * @return The minimum distance in pixels that the user must travel to initiate a drag
     */
    var touchSlop: Int = 0
        private set

    // Last known position/pointer tracking
    /**
     * @return The ID of the pointer currently dragging the captured view,
     * or [.INVALID_POINTER].
     */
    var activePointerId = INVALID_POINTER
        private set
    private var mInitialMotionX: FloatArray? = null
    private var mInitialMotionY: FloatArray? = null
    private var mLastMotionX: FloatArray? = null
    private var mLastMotionY: FloatArray? = null
    private var mInitialEdgesTouched: IntArray? = null
    private var mEdgeDragsInProgress: IntArray? = null
    private var mEdgeDragsLocked: IntArray? = null
    private var mPointersDown: Int = 0

    private var mVelocityTracker: VelocityTracker? = null
    private val mMaxVelocity: Float
    /**
     * Return the currently configured minimum velocity. Any flings with a magnitude less
     * than this value in pixels per second. Callback methods accepting a velocity will receive
     * zero as a velocity value if the real detected velocity was below this threshold.
     *
     * @return the minimum velocity that will be detected
     */
    /**
     * Set the minimum velocity that will be detected as having a magnitude greater than zero
     * in pixels per second. Callback methods accepting a velocity will be clamped appropriately.
     *
     * @param minVel Minimum velocity to detect
     */
    var minVelocity: Float = 0.toFloat()

    /**
     * Return the size of an edge. This is the range in pixels along the edges of this view
     * that will actively detect edge touches or drags if edge tracking is enabled.
     *
     * @return The size of an edge in pixels
     * @see .setEdgeTrackingEnabled
     */
    val edgeSize: Int
    private var mTrackingEdges: Int = 0

    private val mScroller: ScrollerCompat

    /**
     * @return The currently captured view, or null if no view has been captured.
     */
    var capturedView: View? = null

    private var mReleaseInProgress: Boolean = false

    private val mSetIdleRunnable = Runnable { setDragState(STATE_IDLE) }

    init {
        if (mParentView == null) {
            throw IllegalArgumentException("Parent view may not be null")
        }
        if (mCallback == null) {
            throw IllegalArgumentException("Callback may not be null")
        }

        val vc = ViewConfiguration.get(context)
        val density = context.resources.displayMetrics.density
        edgeSize = (EDGE_SIZE * density + 0.5f).toInt()

        touchSlop = vc.scaledTouchSlop
        mMaxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        minVelocity = vc.scaledMinimumFlingVelocity.toFloat()
        mScroller = ScrollerCompat.create(context, sInterpolator)
    }

    /**
     * Enable edge tracking for the selected edges of the parent view.
     * The callback's [android.support.v4.widget.ViewDragHelper.Callback.onEdgeTouched] and
     * [android.support.v4.widget.ViewDragHelper.Callback.onEdgeDragStarted] methods will only be invoked
     * for edges for which edge tracking has been enabled.
     *
     * @param edgeFlags Combination of edge flags describing the edges to watch
     * @see .EDGE_LEFT
     *
     * @see .EDGE_TOP
     *
     * @see .EDGE_RIGHT
     *
     * @see .EDGE_BOTTOM
     */
    fun setEdgeTrackingEnabled(edgeFlags: Int) {
        mTrackingEdges = edgeFlags
    }

    /**
     * Capture a specific child view for dragging within the parent. The callback will be notified
     * but [android.support.v4.widget.ViewDragHelper.Callback.tryCaptureView] will not be asked permission to
     * capture this view.
     *
     * @param childView Child view to capture
     * @param activePointerId ID of the pointer that is dragging the captured child view
     */
    fun captureChildView(childView: View, activePointerId: Int) {
        if (childView.parent !== mParentView) {
            throw IllegalArgumentException("captureChildView: parameter must be a descendant " + "of the ViewDragHelper's tracked parent view (" + mParentView + ")")
        }

        capturedView = childView
        this.activePointerId = activePointerId
        mCallback?.onViewCaptured(childView, activePointerId)
        setDragState(STATE_DRAGGING)
    }

    /**
     * The result of a call to this method is equivalent to
     * [.processTouchEvent] receiving an ACTION_CANCEL event.
     */
    fun cancel() {
        activePointerId = INVALID_POINTER
        clearMotionHistory()

        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    /**
     * [.cancel], but also abort all motion in progress and snap to the end of any
     * animation.
     */
    fun abort() {
        cancel()
        if (viewDragState == STATE_SETTLING) {
            val oldX = mScroller.currX
            val oldY = mScroller.currY
            mScroller.abortAnimation()
            val newX = mScroller.currX
            val newY = mScroller.currY
            mCallback?.onViewPositionChanged(capturedView, newX, newY, newX - oldX, newY - oldY)
        }
        setDragState(STATE_IDLE)
    }

    /**
     * Animate the view `child` to the given (left, top) position.
     * If this method returns true, the caller should invoke [.continueSettling]
     * on each subsequent frame to continue the motion until it returns false. If this method
     * returns false there is no further work to do to complete the movement.
     *
     *
     * This operation does not count as a capture event, though [.getCapturedView]
     * will still report the sliding view while the slide is in progress.
     *
     * @param child Child view to capture and animate
     * @param finalLeft Final left position of child
     * @param finalTop Final top position of child
     * @return true if animation should continue through [.continueSettling] calls
     */
    fun smoothSlideViewTo(child: View, finalLeft: Int, finalTop: Int): Boolean {
        capturedView = child
        activePointerId = INVALID_POINTER

        val continueSliding = forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0)
        if (!continueSliding && viewDragState == STATE_IDLE && capturedView != null) {
            // If we're in an IDLE state to begin with and aren't moving anywhere, we
            // end up having a non-null capturedView with an IDLE dragState
            capturedView = null
        }

        return continueSliding
    }

    /**
     * Settle the captured view at the given (left, top) position.
     * The appropriate velocity from prior motion will be taken into account.
     * If this method returns true, the caller should invoke [.continueSettling]
     * on each subsequent frame to continue the motion until it returns false. If this method
     * returns false there is no further work to do to complete the movement.
     *
     * @param finalLeft Settled left edge position for the captured view
     * @param finalTop Settled top edge position for the captured view
     * @return true if animation should continue through [.continueSettling] calls
     */
    fun settleCapturedViewAt(finalLeft: Int, finalTop: Int): Boolean {
        if (!mReleaseInProgress) {
            throw IllegalStateException("Cannot settleCapturedViewAt outside of a call to " + "Callback#onViewReleased")
        }

        return forceSettleCapturedViewAt(finalLeft, finalTop, VelocityTrackerCompat.getXVelocity(mVelocityTracker, activePointerId).toInt(), VelocityTrackerCompat.getYVelocity(mVelocityTracker, activePointerId).toInt())
    }

    /**
     * Settle the captured view at the given (left, top) position.
     *
     * @param finalLeft Target left position for the captured view
     * @param finalTop Target top position for the captured view
     * @param xvel Horizontal velocity
     * @param yvel Vertical velocity
     * @return true if animation should continue through [.continueSettling] calls
     */
    private fun forceSettleCapturedViewAt(finalLeft: Int, finalTop: Int, xvel: Int, yvel: Int): Boolean {
        val startLeft = capturedView!!.x.toInt()
        val startTop = capturedView!!.y.toInt()
        val dx = finalLeft - startLeft
        val dy = finalTop - startTop

        if (dx == 0 && dy == 0) {
            // Nothing to do. Send callbacks, be done.
            mScroller.abortAnimation()
            setDragState(STATE_IDLE)
            return false
        }

        val duration = computeSettleDuration(capturedView!!, dx, dy, xvel, yvel)
        mScroller.startScroll(startLeft, startTop, dx, dy, duration)

        setDragState(STATE_SETTLING)
        return true
    }

    private fun computeSettleDuration(child: View, dx: Int, dy: Int, xVel: Int, yVel: Int): Int {
        var xvel = xVel
        var yvel = yVel
        xvel = clampMag(xvel, minVelocity.toInt(), mMaxVelocity.toInt())
        yvel = clampMag(yvel, minVelocity.toInt(), mMaxVelocity.toInt())
        val absDx = Math.abs(dx)
        val absDy = Math.abs(dy)
        val absXVel = Math.abs(xvel)
        val absYVel = Math.abs(yvel)
        val addedVel = absXVel + absYVel
        val addedDistance = absDx + absDy

        val xweight = if (xvel != 0) absXVel.toFloat() / addedVel
        else absDx.toFloat() / addedDistance
        val yweight = if (yvel != 0) absYVel.toFloat() / addedVel
        else absDy.toFloat() / addedDistance

        val xduration = computeAxisDuration(dx, xvel, mCallback!!.getViewHorizontalDragRange(child))
        val yduration = computeAxisDuration(dy, yvel, mCallback.getViewVerticalDragRange(child))

        return (xduration * xweight + yduration * yweight).toInt()
    }

    private fun computeAxisDuration(delta: Int, velocity: Int, motionRange: Int): Int {
        var velocity = velocity
        if (delta == 0) {
            return 0
        }

        val width = mParentView?.getWidth()
        val halfWidth = width!! / 2
        val distanceRatio = Math.min(1f, Math.abs(delta).toFloat() / width)
        val distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio)

        val duration: Int
        velocity = Math.abs(velocity)
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity))
        } else {
            val range = Math.abs(delta).toFloat() / motionRange
            duration = ((range + 1) * BASE_SETTLE_DURATION).toInt()
        }
        return Math.min(duration, MAX_SETTLE_DURATION)
    }

    /**
     * Clamp the magnitude of value for absMin and absMax.
     * If the value is below the minimum, it will be clamped to zero.
     * If the value is above the maximum, it will be clamped to the maximum.
     *
     * @param value Value to clamp
     * @param absMin Absolute value of the minimum significant value to return
     * @param absMax Absolute value of the maximum value to return
     * @return The clamped value with the same sign as `value`
     */
    private fun clampMag(value: Int, absMin: Int, absMax: Int): Int {
        val absValue = Math.abs(value)
        if (absValue < absMin) return 0
        return if (absValue > absMax) if (value > 0) absMax else -absMax else value
    }

    /**
     * Clamp the magnitude of value for absMin and absMax.
     * If the value is below the minimum, it will be clamped to zero.
     * If the value is above the maximum, it will be clamped to the maximum.
     *
     * @param value Value to clamp
     * @param absMin Absolute value of the minimum significant value to return
     * @param absMax Absolute value of the maximum value to return
     * @return The clamped value with the same sign as `value`
     */
    private fun clampMag(value: Float, absMin: Float, absMax: Float): Float {
        val absValue = Math.abs(value)
        if (absValue < absMin) return 0f
        return if (absValue > absMax) if (value > 0) absMax else -absMax else value
    }

    private fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= (0.3f * Math.PI / 2.0f).toFloat()
        return Math.sin(f.toDouble()).toFloat()
    }

    /**
     * Settle the captured view based on standard free-moving fling behavior.
     * The caller should invoke [.continueSettling] on each subsequent frame
     * to continue the motion until it returns false.
     *
     * @param minLeft Minimum X position for the view's left edge
     * @param minTop Minimum Y position for the view's top edge
     * @param maxLeft Maximum X position for the view's left edge
     * @param maxTop Maximum Y position for the view's top edge
     */
    fun flingCapturedView(minLeft: Int, minTop: Int, maxLeft: Int, maxTop: Int) {
        if (!mReleaseInProgress) {
            throw IllegalStateException("Cannot flingCapturedView outside of a call to " + "Callback#onViewReleased")
        }

        mScroller.fling(capturedView!!.x.toInt(), capturedView!!.y.toInt(), VelocityTrackerCompat.getXVelocity(mVelocityTracker, activePointerId).toInt(), VelocityTrackerCompat.getYVelocity(mVelocityTracker, activePointerId).toInt(), minLeft, maxLeft, minTop, maxTop)

        setDragState(STATE_SETTLING)
    }

    /**
     * Move the captured settling view by the appropriate amount for the current time.
     * If `continueSettling` returns true, the caller should call it again
     * on the next frame to continue.
     *
     * @param deferCallbacks true if state callbacks should be deferred via posted message.
     * Set this to true if you are calling this method from
     * [android.view.View.computeScroll] or similar methods
     * invoked as part of layout or drawing.
     * @return true if settle is still in progress
     */
    fun continueSettling(deferCallbacks: Boolean): Boolean {
        if (viewDragState == STATE_SETTLING) {
            var keepGoing = mScroller.computeScrollOffset()
            val x = mScroller.currX
            val y = mScroller.currY
            val dx = (x - capturedView!!.x).toInt()
            val dy = (y - capturedView!!.y).toInt()

            if (dx != 0) {
                ViewCompat.offsetLeftAndRight(capturedView, dx)
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom(capturedView, dy)
            }

            if (dx != 0 || dy != 0) {
                mCallback?.onViewPositionChanged(capturedView, x, y, dx, dy)
            }

            if (keepGoing && x == mScroller.finalX && y == mScroller.finalY) {
                // Close enough. The interpolator/scroller might think we're still moving
                // but the user sure doesn't.
                mScroller.abortAnimation()
                keepGoing = false
            }

            if (!keepGoing) {
                if (deferCallbacks) {
                    mParentView?.post(mSetIdleRunnable)
                } else {
                    setDragState(STATE_IDLE)
                }
            }
        }

        return viewDragState == STATE_SETTLING
    }

    /**
     * Like all callback events this must happen on the UI thread, but release
     * involves some extra semantics. During a release (mReleaseInProgress)
     * is the only time it is valid to call [.settleCapturedViewAt]
     * or [.flingCapturedView].
     */
    private fun dispatchViewReleased(xvel: Float, yvel: Float) {
        mReleaseInProgress = true
        mCallback?.onViewReleased(capturedView, xvel, yvel)
        mReleaseInProgress = false

        if (viewDragState == STATE_DRAGGING) {
            // onViewReleased didn't call a method that would have changed this. Go idle.
            setDragState(STATE_IDLE)
        }
    }

    private fun clearMotionHistory() {
        if (mInitialMotionX == null) {
            return
        }
        Arrays.fill(mInitialMotionX!!, 0f)
        Arrays.fill(mInitialMotionY!!, 0f)
        Arrays.fill(mLastMotionX!!, 0f)
        Arrays.fill(mLastMotionY!!, 0f)
        Arrays.fill(mInitialEdgesTouched!!, 0)
        Arrays.fill(mEdgeDragsInProgress!!, 0)
        Arrays.fill(mEdgeDragsLocked!!, 0)
        mPointersDown = 0
    }

    private fun clearMotionHistory(pointerId: Int) {
        if (mInitialMotionX == null || !isPointerDown(pointerId)) {
            return
        }
        mInitialMotionX!![pointerId] = 0f
        mInitialMotionY?.set(pointerId, 0f)
        mLastMotionX?.set(pointerId, 0f)
        mLastMotionY?.set(pointerId, 0f)
        mInitialEdgesTouched?.set(pointerId, 0)
        mEdgeDragsInProgress?.set(pointerId, 0)
        mEdgeDragsLocked?.set(pointerId, 0)
        mPointersDown = mPointersDown and (1 shl pointerId).inv()
    }

    private fun ensureMotionHistorySizeForId(pointerId: Int) {
        if (mInitialMotionX == null || mInitialMotionX!!.size <= pointerId) {
            val imx = FloatArray(pointerId + 1)
            val imy = FloatArray(pointerId + 1)
            val lmx = FloatArray(pointerId + 1)
            val lmy = FloatArray(pointerId + 1)
            val iit = IntArray(pointerId + 1)
            val edip = IntArray(pointerId + 1)
            val edl = IntArray(pointerId + 1)

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX!!, 0, imx, 0, mInitialMotionX!!.size)
                System.arraycopy(mInitialMotionY!!, 0, imy, 0, mInitialMotionY!!.size)
                System.arraycopy(mLastMotionX!!, 0, lmx, 0, mLastMotionX!!.size)
                System.arraycopy(mLastMotionY!!, 0, lmy, 0, mLastMotionY!!.size)
                System.arraycopy(mInitialEdgesTouched!!, 0, iit, 0, mInitialEdgesTouched!!.size)
                System.arraycopy(mEdgeDragsInProgress!!, 0, edip, 0, mEdgeDragsInProgress!!.size)
                System.arraycopy(mEdgeDragsLocked!!, 0, edl, 0, mEdgeDragsLocked!!.size)
            }

            mInitialMotionX = imx
            mInitialMotionY = imy
            mLastMotionX = lmx
            mLastMotionY = lmy
            mInitialEdgesTouched = iit
            mEdgeDragsInProgress = edip
            mEdgeDragsLocked = edl
        }
    }

    private fun saveInitialMotion(x: Float, y: Float, pointerId: Int) {
        ensureMotionHistorySizeForId(pointerId)
        mLastMotionX?.set(pointerId, x)
        mLastMotionX?.get(pointerId)?.let { mInitialMotionX?.set(pointerId, it) }
        mLastMotionY?.set(pointerId, y)
        mLastMotionY?.get(pointerId)?.let { mInitialMotionY?.set(pointerId, it) }
        mInitialEdgesTouched?.set(pointerId, getEdgesTouched(x.toInt(), y.toInt()))
        mPointersDown = mPointersDown or (1 shl pointerId)
    }

    private fun saveLastMotion(ev: MotionEvent) {
        val pointerCount = ev.pointerCount
        for (i in 0 until pointerCount) {
            val pointerId = ev.getPointerId(i)
            // If pointer is invalid then skip saving on ACTION_MOVE.
            if (!isValidPointerForActionMove(pointerId)) {
                continue
            }
            val x = ev.getX(i)
            val y = ev.getY(i)
            mLastMotionX?.set(pointerId, x)
            mLastMotionY?.set(pointerId, y)
        }
    }

    /**
     * Check if the given pointer ID represents a pointer that is currently down (to the best
     * of the ViewDragHelper's knowledge).
     *
     *
     * The state used to report this information is populated by the methods
     * [.shouldInterceptTouchEvent] or
     * [.processTouchEvent]. If one of these methods has not
     * been called for all relevant MotionEvents to track, the information reported
     * by this method may be stale or incorrect.
     *
     * @param pointerId pointer ID to check; corresponds to IDs provided by MotionEvent
     * @return true if the pointer with the given ID is still down
     */
    fun isPointerDown(pointerId: Int): Boolean {
        return mPointersDown and (1 shl pointerId) != 0
    }

    internal fun setDragState(state: Int) {
        mParentView?.removeCallbacks(mSetIdleRunnable)
        if (viewDragState != state) {
            viewDragState = state
            mCallback?.onViewDragStateChanged(state)
            if (viewDragState == STATE_IDLE) {
                capturedView = null
            }
        }
    }

    /**
     * Attempt to capture the view with the given pointer ID. The callback will be involved.
     * This will put us into the "dragging" state. If we've already captured this view with
     * this pointer this method will immediately return true without consulting the callback.
     *
     * @param toCapture View to capture
     * @param pointerId Pointer to capture with
     * @return true if capture was successful
     */
    internal fun tryCaptureViewForDrag(toCapture: View?, pointerId: Int): Boolean {
        if (toCapture === capturedView && activePointerId == pointerId) {
            // Already done!
            return true
        }
        if (toCapture != null && mCallback != null && mCallback.tryCaptureView(toCapture, pointerId)) {
            activePointerId = pointerId
            captureChildView(toCapture, pointerId)
            return true
        }
        return false
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dx Delta scrolled in pixels along the X axis
     * @param dy Delta scrolled in pixels along the Y axis
     * @param x X coordinate of the active touch point
     * @param y Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected fun canScroll(v: View, checkV: Boolean, dx: Int, dy: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = v.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                val child = v.getChildAt(i)
                if (x + scrollX >= child.left && x + scrollX < child.right && y + scrollY >= child.top && y + scrollY < child.bottom && canScroll(child, true, dx, dy, x + scrollX - child.left, y + scrollY - child.top)) {
                    return true
                }
            }
        }

        return checkV && (ViewCompat.canScrollHorizontally(v, -dx) || ViewCompat.canScrollVertically(v, -dy))
    }

    /**
     * Check if this event as provided to the parent view's onInterceptTouchEvent should
     * cause the parent to intercept the touch event stream.
     *
     * @param ev MotionEvent provided to onInterceptTouchEvent
     * @return true if the parent view should return true from onInterceptTouchEvent
     */
    fun shouldInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)
        val actionIndex = MotionEventCompat.getActionIndex(ev)

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                val pointerId = ev.getPointerId(0)
                saveInitialMotion(x, y, pointerId)

                val toCapture = findTopChildUnder(x.toInt(), y.toInt())

                // Catch a settling view if possible.
                if (toCapture === capturedView && viewDragState == STATE_SETTLING) {
                    tryCaptureViewForDrag(toCapture, pointerId)
                }

                val edgesTouched = mInitialEdgesTouched!![pointerId]
                if (edgesTouched and mTrackingEdges != 0) {
                    mCallback?.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                }
            }

            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val pointerId = ev.getPointerId(actionIndex)
                val x = ev.getX(actionIndex)
                val y = ev.getY(actionIndex)

                saveInitialMotion(x, y, pointerId)

                // A ViewDragHelper can only manipulate one view at a time.
                if (viewDragState == STATE_IDLE) {
                    val edgesTouched = mInitialEdgesTouched!![pointerId]
                    if (edgesTouched and mTrackingEdges != 0) {
                        mCallback?.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                    }
                } else if (viewDragState == STATE_SETTLING) {
                    // Catch a settling view if possible.
                    val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                    if (toCapture === capturedView) {
                        tryCaptureViewForDrag(toCapture, pointerId)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
//                if (mInitialMotionX == null || mInitialMotionY == null) { //TODO
//                    break
//                }

                // First to cross a touch slop over a draggable view wins. Also report edge drags.
                val pointerCount = ev.pointerCount
                for (i in 0 until pointerCount) {
                    val pointerId = ev.getPointerId(i)

                    // If pointer is invalid then skip the ACTION_MOVE.
                    if (!isValidPointerForActionMove(pointerId)) continue

                    val x = ev.getX(i)
                    val y = ev.getY(i)
                    val dx = x - mInitialMotionX!![pointerId]
                    val dy = y - mInitialMotionY!![pointerId]

                    val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                    val pastSlop = toCapture != null && checkTouchSlop(toCapture, dx, dy)
                    if (pastSlop) {
                        // check the callback's
                        // getView[Horizontal|Vertical]DragRange methods to know
                        // if you can move at all along an axis, then see if it
                        // would clamp to the same value. If you can't move at
                        // all in every dimension with a nonzero range, bail.
                        val oldLeft = toCapture!!.x.toInt()
                        val targetLeft = oldLeft + dx.toInt()
                        val newLeft = mCallback?.clampViewPositionHorizontal(toCapture, targetLeft, dx.toInt())
                        val oldTop = toCapture.top
                        val targetTop = oldTop + dy.toInt()
                        val newTop = mCallback?.clampViewPositionVertical(toCapture, targetTop, dy.toInt())
                        val horizontalDragRange = mCallback?.getViewHorizontalDragRange(toCapture)
                        val verticalDragRange = mCallback?.getViewVerticalDragRange(toCapture)
                        if ((horizontalDragRange == 0 || horizontalDragRange!! > 0 && newLeft == oldLeft) && (verticalDragRange == 0 || verticalDragRange!! > 0 && newTop == oldTop)) {
                            break
                        }
                    }
                    reportNewEdgeDrags(dx, dy, pointerId)
                    if (viewDragState == STATE_DRAGGING) {
                        // Callback might have started an edge drag
                        break
                    }

                    if (pastSlop && tryCaptureViewForDrag(toCapture, pointerId)) {
                        break
                    }
                }
                saveLastMotion(ev)
            }

            MotionEventCompat.ACTION_POINTER_UP -> {
                val pointerId = ev.getPointerId(actionIndex)
                clearMotionHistory(pointerId)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cancel()
            }
        }

        return viewDragState == STATE_DRAGGING
    }

    /**
     * Process a touch event received by the parent view. This method will dispatch callback events
     * as needed before returning. The parent view's onTouchEvent implementation should call this.
     *
     * @param ev The touch event received by the parent view
     */
    fun processTouchEvent(ev: MotionEvent) {
        val action = MotionEventCompat.getActionMasked(ev)
        val actionIndex = MotionEventCompat.getActionIndex(ev)

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                val pointerId = ev.getPointerId(0)
                val toCapture = findTopChildUnder(x.toInt(), y.toInt())

                saveInitialMotion(x, y, pointerId)

                // Since the parent is already directly processing this touch event,
                // there is no reason to delay for a slop before dragging.
                // Start immediately if possible.
                tryCaptureViewForDrag(toCapture, pointerId)

                val edgesTouched = mInitialEdgesTouched!![pointerId]
                if (edgesTouched and mTrackingEdges != 0) {
                    mCallback?.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                }
            }

            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val pointerId = ev.getPointerId(actionIndex)
                val x = ev.getX(actionIndex)
                val y = ev.getY(actionIndex)

                saveInitialMotion(x, y, pointerId)

                // A ViewDragHelper can only manipulate one view at a time.
                if (viewDragState == STATE_IDLE) {
                    // If we're idle we can do anything! Treat it like a normal down event.

                    val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                    tryCaptureViewForDrag(toCapture, pointerId)

                    val edgesTouched = mInitialEdgesTouched!![pointerId]
                    if (edgesTouched and mTrackingEdges != 0) {
                        mCallback?.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                    }
                } else if (isCapturedViewUnder(x.toInt(), y.toInt())) {
                    // We're still tracking a captured view. If the same view is under this
                    // point, we'll swap to controlling it with this pointer instead.
                    // (This will still work if we're "catching" a settling view.)

                    tryCaptureViewForDrag(capturedView, pointerId)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (viewDragState == STATE_DRAGGING) {
                    // If pointer is invalid then skip the ACTION_MOVE.
//                    if (!isValidPointerForActionMove(activePointerId)) break //TODO

                    val index = ev.findPointerIndex(activePointerId)
                    val x = ev.getX(index)
                    val y = ev.getY(index)
                    val idx = (x - mLastMotionX!![activePointerId]).toInt()
                    val idy = (y - mLastMotionY!![activePointerId]).toInt()

                    dragTo((capturedView!!.x + idx).toInt(), (capturedView!!.y + idy).toInt(), idx, idy)

                    saveLastMotion(ev)
                } else {
                    // Check to see if any pointer is now over a draggable view.
                    val pointerCount = ev.pointerCount
                    for (i in 0 until pointerCount) {
                        val pointerId = ev.getPointerId(i)

                        // If pointer is invalid then skip the ACTION_MOVE.
                        if (!isValidPointerForActionMove(pointerId)) continue

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        val dx = x - mInitialMotionX!![pointerId]
                        val dy = y - mInitialMotionY!![pointerId]

                        reportNewEdgeDrags(dx, dy, pointerId)
                        if (viewDragState == STATE_DRAGGING) {
                            // Callback might have started an edge drag.
                            break
                        }

                        val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                        if (checkTouchSlop(toCapture, dx, dy) && tryCaptureViewForDrag(toCapture, pointerId)) {
                            break
                        }
                    }
                    saveLastMotion(ev)
                }
            }

            MotionEventCompat.ACTION_POINTER_UP -> {
                val pointerId = ev.getPointerId(actionIndex)
                if (viewDragState == STATE_DRAGGING && pointerId == activePointerId) {
                    // Try to find another pointer that's still holding on to the captured view.
                    var newActivePointer = INVALID_POINTER
                    val pointerCount = ev.pointerCount
                    for (i in 0 until pointerCount) {
                        val id = ev.getPointerId(i)
                        if (id == activePointerId) {
                            // This one's going away, skip.
                            continue
                        }

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        if (findTopChildUnder(x.toInt(), y.toInt()) === capturedView && tryCaptureViewForDrag(capturedView, id)) {
                            newActivePointer = activePointerId
                            break
                        }
                    }

                    if (newActivePointer == INVALID_POINTER) {
                        // We didn't find another pointer still touching the view, release it.
                        releaseViewForPointerUp()
                    }
                }
                clearMotionHistory(pointerId)
            }

            MotionEvent.ACTION_UP -> {
                if (viewDragState == STATE_DRAGGING) {
                    releaseViewForPointerUp()
                }
                cancel()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (viewDragState == STATE_DRAGGING) {
                    dispatchViewReleased(0f, 0f)
                }
                cancel()
            }
        }
    }

    private fun reportNewEdgeDrags(dx: Float, dy: Float, pointerId: Int) {
        var dragsStarted = 0
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_LEFT)) {
            dragsStarted = dragsStarted or EDGE_LEFT
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_TOP)) {
            dragsStarted = dragsStarted or EDGE_TOP
        }
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_RIGHT)) {
            dragsStarted = dragsStarted or EDGE_RIGHT
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_BOTTOM)) {
            dragsStarted = dragsStarted or EDGE_BOTTOM
        }

        if (dragsStarted != 0) {
            mEdgeDragsInProgress?.set(pointerId, mEdgeDragsInProgress!![pointerId] or dragsStarted)
            mCallback?.onEdgeDragStarted(dragsStarted, pointerId)
        }
    }

    private fun checkNewEdgeDrag(delta: Float, odelta: Float, pointerId: Int, edge: Int): Boolean {
        val absDelta = Math.abs(delta)
        val absODelta = Math.abs(odelta)

        if (mInitialEdgesTouched!![pointerId] and edge != edge || mTrackingEdges and edge == 0 || mEdgeDragsLocked!![pointerId] and edge == edge || mEdgeDragsInProgress!![pointerId] and edge == edge || absDelta <= touchSlop && absODelta <= touchSlop) {
            return false
        }
        if (absDelta < absODelta * 0.5f && mCallback?.onEdgeLock(edge)!!) {
            mEdgeDragsLocked!![pointerId] = mEdgeDragsLocked!![pointerId] or edge
            return false
        }
        return mEdgeDragsInProgress!![pointerId] and edge == 0 && absDelta > touchSlop
    }

    /**
     * Check if we've crossed a reasonable touch slop for the given child view.
     * If the child cannot be dragged along the horizontal or vertical axis, motion
     * along that axis will not count toward the slop check.
     *
     * @param child Child to check
     * @param dx Motion since initial position along X axis
     * @param dy Motion since initial position along Y axis
     * @return true if the touch slop has been crossed
     */
    private fun checkTouchSlop(child: View?, dx: Float, dy: Float): Boolean {
        if (child == null) {
            return false
        }
        val checkHorizontal = mCallback?.getViewHorizontalDragRange(child)!! > 0
        val checkVertical = mCallback.getViewVerticalDragRange(child) > 0

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > touchSlop * touchSlop
        } else if (checkHorizontal) {
            return Math.abs(dx) > touchSlop
        } else if (checkVertical) {
            return Math.abs(dy) > touchSlop
        }
        return false
    }

    /**
     * Check if any pointer tracked in the current gesture has crossed
     * the required slop threshold.
     *
     *
     * This depends on internal state populated by
     * [.shouldInterceptTouchEvent] or
     * [.processTouchEvent]. You should only rely on
     * the results of this method after all currently available touch data
     * has been provided to one of these two methods.
     *
     * @param directions Combination of direction flags, see [.DIRECTION_HORIZONTAL],
     * [.DIRECTION_VERTICAL], [.DIRECTION_ALL]
     * @return true if the slop threshold has been crossed, false otherwise
     */
    fun checkTouchSlop(directions: Int): Boolean {
        val count = mInitialMotionX!!.size
        for (i in 0 until count) {
            if (checkTouchSlop(directions, i)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if the specified pointer tracked in the current gesture has crossed
     * the required slop threshold.
     *
     *
     * This depends on internal state populated by
     * [.shouldInterceptTouchEvent] or
     * [.processTouchEvent]. You should only rely on
     * the results of this method after all currently available touch data
     * has been provided to one of these two methods.
     *
     * @param directions Combination of direction flags, see [.DIRECTION_HORIZONTAL],
     * [.DIRECTION_VERTICAL], [.DIRECTION_ALL]
     * @param pointerId ID of the pointer to slop check as specified by MotionEvent
     * @return true if the slop threshold has been crossed, false otherwise
     */
    fun checkTouchSlop(directions: Int, pointerId: Int): Boolean {
        if (!isPointerDown(pointerId)) {
            return false
        }

        val checkHorizontal = directions and DIRECTION_HORIZONTAL == DIRECTION_HORIZONTAL
        val checkVertical = directions and DIRECTION_VERTICAL == DIRECTION_VERTICAL

        val dx = mLastMotionX!![pointerId] - mInitialMotionX!![pointerId]
        val dy = mLastMotionY!![pointerId] - mInitialMotionY!![pointerId]

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > touchSlop * touchSlop
        } else if (checkHorizontal) {
            return Math.abs(dx) > touchSlop
        } else if (checkVertical) {
            return Math.abs(dy) > touchSlop
        }
        return false
    }

    /**
     * Check if any of the edges specified were initially touched in the currently active gesture.
     * If there is no currently active gesture this method will return false.
     *
     * @param edges Edges to check for an initial edge touch. See [.EDGE_LEFT],
     * [.EDGE_TOP], [.EDGE_RIGHT], [.EDGE_BOTTOM] and
     * [.EDGE_ALL]
     * @return true if any of the edges specified were initially touched in the current gesture
     */
    fun isEdgeTouched(edges: Int): Boolean {
        val count = mInitialEdgesTouched!!.size
        for (i in 0 until count) {
            if (isEdgeTouched(edges, i)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if any of the edges specified were initially touched by the pointer with
     * the specified ID. If there is no currently active gesture or if there is no pointer with
     * the given ID currently down this method will return false.
     *
     * @param edges Edges to check for an initial edge touch. See [.EDGE_LEFT],
     * [.EDGE_TOP], [.EDGE_RIGHT], [.EDGE_BOTTOM] and
     * [.EDGE_ALL]
     * @return true if any of the edges specified were initially touched in the current gesture
     */
    fun isEdgeTouched(edges: Int, pointerId: Int): Boolean {
        return isPointerDown(pointerId) && mInitialEdgesTouched!![pointerId] and edges != 0
    }

    private fun releaseViewForPointerUp() {
        mVelocityTracker!!.computeCurrentVelocity(1000, mMaxVelocity)
        val xvel = clampMag(VelocityTrackerCompat.getXVelocity(mVelocityTracker, activePointerId), minVelocity, mMaxVelocity)
        val yvel = clampMag(VelocityTrackerCompat.getYVelocity(mVelocityTracker, activePointerId), minVelocity, mMaxVelocity)
        dispatchViewReleased(xvel, yvel)
    }

    private fun dragTo(left: Int, top: Int, dx: Int, dy: Int) {
        var clampedX = left
        var clampedY = top
        val oldLeft = capturedView!!.x.toInt()
        val oldTop = capturedView!!.y.toInt()
        if (dx != 0) {
            clampedX = mCallback?.clampViewPositionHorizontal(capturedView, left, dx)!!
            ViewCompat.offsetLeftAndRight(capturedView, clampedX - oldLeft)
        }
        if (dy != 0) {
            clampedY = mCallback?.clampViewPositionVertical(capturedView, top, dy)!!
            ViewCompat.offsetTopAndBottom(capturedView, clampedY - oldTop)
        }

        if (dx != 0 || dy != 0) {
            val clampedDx = clampedX - oldLeft
            val clampedDy = clampedY - oldTop
            mCallback?.onViewPositionChanged(capturedView, clampedX, clampedY, clampedDx, clampedDy)
        }
    }

    /**
     * Determine if the currently captured view is under the given point in the
     * parent view's coordinate system. If there is no captured view this method
     * will return false.
     *
     * @param x X position to test in the parent's coordinate system
     * @param y Y position to test in the parent's coordinate system
     * @return true if the captured view is under the given point, false otherwise
     */
    fun isCapturedViewUnder(x: Int, y: Int): Boolean {
        return isViewUnder(capturedView, x, y)
    }

    /**
     * Determine if the supplied view is under the given point in the
     * parent view's coordinate system.
     *
     * @param view Child view of the parent to hit test
     * @param x X position to test in the parent's coordinate system
     * @param y Y position to test in the parent's coordinate system
     * @return true if the supplied view is under the given point, false otherwise
     */
    fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
        return if (view == null) {
            false
        } else x >= view.x && x < view.x + view.width && y >= view.y && y < view.y + view.height
    }

    /**
     * Find the topmost child under the given point within the parent view's coordinate system.
     * The child order is determined using [android.support.v4.widget.ViewDragHelper.Callback.getOrderedChildIndex].
     *
     * @param x X position to test in the parent's coordinate system
     * @param y Y position to test in the parent's coordinate system
     * @return The topmost child view under (x, y) or null if none found.
     */
    fun findTopChildUnder(x: Int, y: Int): View? {
        val childCount = mParentView?.getChildCount()
        if (childCount != null) {
            for (i in childCount - 1 downTo 0) {
                val child = mCallback?.getOrderedChildIndex(i)?.let { mParentView?.getChildAt(it) }
                if (child != null && x >= child.x && x < child.x + child.width && y >= child.y && y < child.y + child.height) {
                    return child
                }
            }
        }
        return null
    }

    private fun getEdgesTouched(x: Int, y: Int): Int {
        var result = 0

        if (mParentView != null) {
                    if (x < mParentView.getX() + edgeSize) result = result or EDGE_LEFT
                    if (y < mParentView.getY() + edgeSize) result = result or EDGE_TOP
                    if (x > mParentView.getX() + mParentView.getWidth() - edgeSize) result = result or EDGE_RIGHT
                    if (y > mParentView.getY() + mParentView.getHeight() - edgeSize) result = result or EDGE_BOTTOM
                }

        return result
    }

    private fun isValidPointerForActionMove(pointerId: Int): Boolean {
        if (!isPointerDown(pointerId)) {
            Log.e(TAG, "Ignoring pointerId=" + pointerId + " because ACTION_DOWN was not received " + "for this pointer before ACTION_MOVE. It likely happened because " + " ViewDragHelper did not receive all the events in the event stream.")
            return false
        }
        return true
    }

    companion object {

        private val TAG = "ViewDragHelper"

        /**
         * A null/invalid pointer ID.
         */
        val INVALID_POINTER = -1

        /**
         * A view is not currently being dragged or animating as a result of a fling/snap.
         */
        val STATE_IDLE = 0

        /**
         * A view is currently being dragged. The position is currently changing as a result
         * of user input or simulated user input.
         */
        val STATE_DRAGGING = 1

        /**
         * A view is currently settling into place as a result of a fling or
         * predefined non-interactive motion.
         */
        val STATE_SETTLING = 2

        /**
         * Edge flag indicating that the left edge should be affected.
         */
        val EDGE_LEFT = 1 shl 0

        /**
         * Edge flag indicating that the right edge should be affected.
         */
        val EDGE_RIGHT = 1 shl 1

        /**
         * Edge flag indicating that the top edge should be affected.
         */
        val EDGE_TOP = 1 shl 2

        /**
         * Edge flag indicating that the bottom edge should be affected.
         */
        val EDGE_BOTTOM = 1 shl 3

        /**
         * Edge flag set indicating all edges should be affected.
         */
        val EDGE_ALL = EDGE_LEFT or EDGE_TOP or EDGE_RIGHT or EDGE_BOTTOM

        /**
         * Indicates that a check should occur along the horizontal axis
         */
        val DIRECTION_HORIZONTAL = 1 shl 0

        /**
         * Indicates that a check should occur along the vertical axis
         */
        val DIRECTION_VERTICAL = 1 shl 1

        /**
         * Indicates that a check should occur along all axes
         */
        val DIRECTION_ALL = DIRECTION_HORIZONTAL or DIRECTION_VERTICAL

        private val EDGE_SIZE = 20 // dp

        private val BASE_SETTLE_DURATION = 256 // ms
        private val MAX_SETTLE_DURATION = 600 // ms


        /**
         * Interpolator defining the animation curve for mScroller
         */
        private val sInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }

        /**
         * Factory method to create a new ViewDragHelper.
         *
         * @param forParent Parent view to monitor
         * @param cb Callback to provide information and receive events
         * @return a new ViewDragHelper instance
         */
        fun create(forParent: ViewGroup, cb: android.support.v4.widget.ViewDragHelper.Callback): KDragViewHelper {
            return KDragViewHelper(forParent.context, forParent, cb)
        }

        /**
         * Factory method to create a new ViewDragHelper.
         *
         * @param forParent Parent view to monitor
         * @param sensitivity Multiplier for how sensitive the helper should be about detecting
         * the start of a drag. Larger values are more sensitive. 1.0f is normal.
         * @param cb Callback to provide information and receive events
         * @return a new ViewDragHelper instance
         */
        fun create(forParent: ViewGroup, sensitivity: Float, cb: android.support.v4.widget.ViewDragHelper.Callback): KDragViewHelper {
            val helper = create(forParent, cb)
            helper.touchSlop = (helper.touchSlop * (1 / sensitivity)).toInt()
            return helper
        }
    }

}
