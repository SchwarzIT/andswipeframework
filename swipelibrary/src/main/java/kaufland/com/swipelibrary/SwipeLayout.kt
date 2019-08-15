package kaufland.com.swipelibrary

import android.content.Context
import android.graphics.Canvas
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EViewGroup

import java.security.InvalidParameterException

import kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED
import kaufland.com.swipelibrary.SwipeState.DragViewState.LEFT_OPEN
import kaufland.com.swipelibrary.SwipeState.DragViewState.RIGHT_OPEN
import kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL
import kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL

@EViewGroup
class SwipeLayout : FrameLayout {

    @Bean
    var swipeState: SwipeState? = null
        protected set

    @Bean
    protected var mSwipeDirectionDetector: SwipeDirectionDetector? = null

    @Bean
    protected var mDraggingProxy: DraggingProxy? = null

    private var mDragHelper: KDragViewHelper? = null

    private var mSwipeListener: SwipeListener? = null

    var swipeEnabled = true

    private var mDragHelperTouchSlop: Float = 0.toFloat()


    private var mRestoreOnDraw: Boolean = false

    interface SwipeListener {
        fun onSwipeOpened(openedDragView: SwipeState.DragViewState, isFullSwipe: Boolean)

        fun onSwipeClosed(dragViewState: SwipeState.DragViewState)

        fun onBounce(dragViewState: SwipeState.DragViewState)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initSwipe()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initSwipe()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {


        if (!swipeEnabled) {
            return false
        }

        val action = ev.action

        when (action) {
            MotionEvent.ACTION_DOWN -> {

                mDragHelper!!.abort()
                mSwipeDirectionDetector!!.onActionDown(ev.x, ev.y, this)
                mDragHelper!!.processTouchEvent(ev)
            }

            MotionEvent.ACTION_MOVE -> {
                mSwipeDirectionDetector!!.onAction(ev.x, ev.y)


                val isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector!!.difX)

                if (!isClick) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    try {
                        mDragHelper!!.processTouchEvent(ev)
                    } catch (e: IllegalArgumentException) {
                        // https://code.google.com/p/android/issues/detail?id=64553
                        mSwipeDirectionDetector!!.onAction(mSwipeDirectionDetector!!.xDown.toFloat(), mSwipeDirectionDetector!!.yDown.toFloat())
                        ev.action = MotionEvent.ACTION_UP
                        mDragHelper!!.processTouchEvent(ev)
                    }

                }
            }

            MotionEvent.ACTION_UP -> {

                mSwipeDirectionDetector!!.onActionUp(ev.x, ev.y, this)


                if (ev.x < 0 || ev.y < 0 || ev.x > measuredWidth || ev.y > measuredHeight) {


                    ev.action = MotionEvent.ACTION_UP
                    mDragHelper!!.processTouchEvent(ev)
                    return true
                }


                mDragHelper!!.processTouchEvent(ev)
            }

            MotionEvent.ACTION_CANCEL ->

                mDragHelper!!.processTouchEvent(ev)

            else -> mDragHelper!!.processTouchEvent(ev)
        }

        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {


        if (!swipeEnabled) {
            return false
        }

        val action = MotionEventCompat.getActionMasked(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mSwipeDirectionDetector!!.onActionDown(ev.x, ev.y, this)
                mDragHelper!!.abort()
                mDragHelper!!.processTouchEvent(ev)
            }

            MotionEvent.ACTION_MOVE -> {
                mSwipeDirectionDetector!!.onAction(ev.x, ev.y)

                val isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector!!.difX)
                if (!isClick) {
                    try {
                        mDragHelper!!.processTouchEvent(ev)
                    } catch (e: IllegalArgumentException) {
                        // https://code.google.com/p/android/issues/detail?id=64553
                        mSwipeDirectionDetector!!.onAction(mSwipeDirectionDetector!!.xDown.toFloat(), mSwipeDirectionDetector!!.yDown.toFloat())
                        ev.action = MotionEvent.ACTION_UP
                        mDragHelper!!.processTouchEvent(ev)
                    }

                }


                if (!isClick) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->

                mDragHelper!!.processTouchEvent(ev)
        }

        return false
    }


    fun smoothSlideTo(slideOffset: Float) {

        val leftOffset = if (mDraggingProxy!!.dragDirection == HORIZONTAL) slideOffset.toInt() else 0
        val topOffset = if (mDraggingProxy!!.dragDirection == VERTICAL) slideOffset.toInt() else 0

        if (mDragHelper!!.smoothSlideViewTo(mDraggingProxy!!.surfaceView, leftOffset, topOffset)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun computeScroll() {
        super.computeScroll()


        if (mDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }

    }

    override fun dispatchDraw(canvas: Canvas) {

        super.dispatchDraw(canvas)

        if (!mDraggingProxy!!.isInitilized) {
            mDraggingProxy!!.init(this)
        }

        if (mRestoreOnDraw) {
            post {
                mDraggingProxy!!.restoreState(swipeState!!.state)
                mRestoreOnDraw = false
            }

        }


    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mDraggingProxy!!.restoreChildrenBound()
    }

    private inner class SwipeDragViewHelper internal constructor(private val parent: ViewGroup) : ViewDragHelper.Callback() {

        private var xBeforeDrag: Int = 0

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {

            xBeforeDrag = child.x.toInt()
            return mDraggingProxy!!.isCapturedViewDraggable(child)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == 0) {
                mDraggingProxy!!.captureChildrenBound()
            }
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {

            if (mDraggingProxy!!.dragDirection == HORIZONTAL) {

                if (changedView != null) {
                    mDraggingProxy!!.moveView(changedView, left)
                }

            } else if (mDraggingProxy!!.dragDirection == VERTICAL) {
                throw InvalidParameterException("VerticalSwipeNotImplemented")
            }
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {

            if (mDraggingProxy!!.dragDirection == VERTICAL) {
                throw InvalidParameterException("VerticalSwipeNotImplemented")
            } else if (mDraggingProxy!!.dragDirection == HORIZONTAL) {

                var swipeResult = SwipeResult(xBeforeDrag)
                val isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector!!.difX)

                if (!mSwipeDirectionDetector!!.isHorizontalScrollChangedWhileDragging && !isClick) {
                    swipeResult = mDraggingProxy!!.determineSwipeHorizontalState(xvel, mSwipeDirectionDetector!!, swipeState!!, mSwipeListener!!, releasedChild!!)
                }

                if (mDragHelper!!.smoothSlideViewTo(releasedChild!!, swipeResult.settleX, 0)) {
                    ViewCompat.postInvalidateOnAnimation(parent)

                }

                mDraggingProxy!!.requestLayout()

                if (mSwipeListener != null) {
                    swipeResult.notifyListener?.run()
                }
            }
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            return if (swipeState!!.state == LEFT_OPEN || swipeState!!.state == RIGHT_OPEN || mDraggingProxy!!.dragDirection == HORIZONTAL) {
                0
            } else 0

        }

        override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            return if (swipeState!!.state == SwipeState.DragViewState.TOP_OPEN || swipeState!!.state == SwipeState.DragViewState.BOTTOM_OPEN || !mDraggingProxy!!.canSwipe(mSwipeDirectionDetector!!, swipeState!!.state) || mDraggingProxy!!.dragDirection == VERTICAL) {
                0
            } else mDraggingProxy!!.clampViewPositionHorizontal(child!!, left)

        }

        override fun getViewHorizontalDragRange(child: View?): Int {
            return width
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return 0
        }

    }

    fun markForRestoreOnDraw(swipeState: SwipeState.DragViewState) {
        this.swipeState!!.state = swipeState
        mRestoreOnDraw = true
        if (mDragHelper!!.viewDragState != KDragViewHelper.STATE_IDLE) {
            mDragHelper!!.abort()
        }
    }

    fun closeSwipeNoAnimation() {
        swipeState!!.state = CLOSED
        mDraggingProxy!!.restoreState(swipeState!!.state)
    }

    @JvmOverloads
    fun openSwipe(position: Int, notifyCallback: Boolean = true) {

        val mSurfaceOpenOffsetByDragView = mDraggingProxy!!.getSurfaceOpenOffsetByDragView(position)

        when (position) {
            LEFT_DRAG_VIEW -> {
                swipeState!!.state = LEFT_OPEN
                smoothSlideTo(mSurfaceOpenOffsetByDragView.toFloat())
            }

            RIGHT_DRAG_VIEW -> {
                swipeState!!.state = RIGHT_OPEN
                smoothSlideTo(mSurfaceOpenOffsetByDragView.toFloat())
            }

            else -> return
        }


        if (mSwipeListener != null && notifyCallback) {
            notifySwipeOpened()
        }
    }

    fun closeSwipe() {
        when (swipeState!!.state) {
            LEFT_OPEN, RIGHT_OPEN, SwipeState.DragViewState.TOP_OPEN, SwipeState.DragViewState.BOTTOM_OPEN -> {
                smoothSlideTo(SWIPE_CLOSING_POINT.toFloat())
                swipeState!!.state = CLOSED
            }
        }
    }

    fun setSwipeListener(listener: SwipeListener) {
        mSwipeListener = listener
    }

    private fun notifySwipeOpened() {
        mSwipeListener!!.onSwipeOpened(swipeState!!.state, true)

    }

    private fun initSwipe() {

        mDragHelper = KDragViewHelper.create(this, 1.0f, SwipeDragViewHelper(this))
        mDragHelperTouchSlop = (mDragHelper!!.touchSlop * 2).toFloat()
    }

    companion object {

        private val SWIPE_CLOSING_POINT = 0

        const val LEFT_DRAG_VIEW = 1
        const val RIGHT_DRAG_VIEW = 2
        const val TOP_DRAG_VIEW = 3
        const val BOTTOM_DRAG_VIEW = 4
        const val SURFACE_VIEW = 5

        private val TAG = SwipeLayout::class.java.getSimpleName()
    }

}
