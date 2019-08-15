package kaufland.com.swipelibrary.dragengine

import android.view.View

import kaufland.com.swipelibrary.DragView
import kaufland.com.swipelibrary.SurfaceView
import kaufland.com.swipelibrary.SwipeDirectionDetector
import kaufland.com.swipelibrary.SwipeDirectionDetector.Companion.SWIPE_DIRECTION_LEFT
import kaufland.com.swipelibrary.SwipeDirectionDetector.Companion.SWIPE_DIRECTION_RIGHT
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeResult
import kaufland.com.swipelibrary.SwipeState
import kaufland.com.swipelibrary.SwipeViewLayouter

import kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED
import kaufland.com.swipelibrary.SwipeState.DragViewState.LEFT_OPEN
import kaufland.com.swipelibrary.SwipeState.DragViewState.RIGHT_OPEN

/**
 * Created by sbra0902 on 29.03.17.
 */

class SurfaceViewEngine(private val mLayouter: SwipeViewLayouter) : DraggingEngine<SurfaceView> {

    override var dragView: SurfaceView? = null

    private var mLeftDragViewEngine: DraggingEngine<*>? = null

    private var mRightDragViewEngine: DraggingEngine<*>? = null


    override val dragDistance: Int
        get() = 0

    override val intermmediateDistance: Int
        get() = 0

    override val openOffset: Int
        get() = 0

    override fun moveView(offset: Float, view: SurfaceView, changedView: View) {

        if (changedView == dragView) {
            return
        }

        if (mLeftDragViewEngine != null && changedView == mLeftDragViewEngine!!.dragView) {
            dragView?.x = mLeftDragViewEngine!!.dragView!!.x + mLeftDragViewEngine!!.dragView!!.width
        }

        if (mRightDragViewEngine != null && changedView == mRightDragViewEngine!!.dragView) {
            dragView?.x = mRightDragViewEngine!!.dragView!!.x - dragView!!.width
        }
    }

    private fun isMinimumDifReached(distanceToNextState: Int, detector: SwipeDirectionDetector): Boolean {
        return Math.abs(distanceToNextState / 2) <= Math.abs(detector.difX) && Math.abs(detector.difX) > Math.abs(detector.difY) && !detector.isHorizontalScrollChangedWhileDragging
    }

    override fun determineSwipeHorizontalState(velocity: Float, swipeDirectionDetector: SwipeDirectionDetector, swipeState: SwipeState, swipeListener: SwipeLayout.SwipeListener, releasedChild: View): SwipeResult? {

        if (dragView != releasedChild) {
            return null
        }

        val swipeDirection = swipeDirectionDetector.swipeDirection

        if (swipeState.state == CLOSED) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                if (mLeftDragViewEngine == null || !getDragViewForEngine(mLeftDragViewEngine)!!.isDraggable || !isMinimumDifReached(mLeftDragViewEngine!!.intermmediateDistance, swipeDirectionDetector)) {
                    return SwipeResult(0)
                }

                swipeState.state = LEFT_OPEN
                return if (velocity > LEFT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.difX) > mLeftDragViewEngine!!.dragDistance / 2) {
                    SwipeResult(mLeftDragViewEngine!!.dragDistance, Runnable { swipeListener.onSwipeOpened(LEFT_OPEN, true) })
                } else {
                    SwipeResult(mLeftDragViewEngine!!.intermmediateDistance, Runnable { swipeListener.onSwipeOpened(LEFT_OPEN, mLeftDragViewEngine!!.intermmediateDistance == mLeftDragViewEngine!!.dragDistance) })
                }

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                if (mRightDragViewEngine == null || !getDragViewForEngine(mRightDragViewEngine)!!.isDraggable || !isMinimumDifReached(mRightDragViewEngine!!.intermmediateDistance, swipeDirectionDetector)) {
                    return SwipeResult(0)
                }

                swipeState.state = RIGHT_OPEN

                return if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.difX) > mRightDragViewEngine!!.dragDistance / 2) {
                    SwipeResult(-mRightDragViewEngine!!.dragDistance, Runnable { swipeListener.onSwipeOpened(RIGHT_OPEN, true) })
                } else {
                    SwipeResult(-mRightDragViewEngine!!.intermmediateDistance, Runnable { swipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragViewEngine!!.intermmediateDistance == mRightDragViewEngine!!.dragDistance) })
                }
            }
        } else if (swipeState.state == LEFT_OPEN) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                swipeState.state = LEFT_OPEN

                return SwipeResult(mLeftDragViewEngine!!.intermmediateDistance, Runnable { swipeListener.onBounce(LEFT_OPEN) })

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {

                swipeState.state = CLOSED
                return SwipeResult(0, Runnable { swipeListener.onSwipeClosed(CLOSED) })
            }
        } else if (swipeState.state == RIGHT_OPEN) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {

                swipeState.state = CLOSED
                return SwipeResult(0, Runnable { swipeListener.onSwipeClosed(CLOSED) })

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                swipeState.state = RIGHT_OPEN
                return if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.difX) > mRightDragViewEngine!!.dragDistance / 2) {
                    SwipeResult(-mRightDragViewEngine!!.dragDistance, Runnable { swipeListener.onSwipeOpened(RIGHT_OPEN, true) })
                } else {
                    SwipeResult(-mRightDragViewEngine!!.intermmediateDistance, Runnable { swipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragViewEngine!!.intermmediateDistance == mRightDragViewEngine!!.dragDistance) })
                }
            }

        }

        return SwipeResult(0)
    }

    override fun initializePosition(orientation: SwipeViewLayouter.DragDirection) {

        dragView = mLayouter.surfaceView

        mLeftDragViewEngine = mLayouter.getDragViewEngineByPosition(SwipeLayout.LEFT_DRAG_VIEW)
        mRightDragViewEngine = mLayouter.getDragViewEngineByPosition(SwipeLayout.RIGHT_DRAG_VIEW)
    }

    override fun clampViewPositionHorizontal(child: View, left: Int): Int {

        if (mRightDragViewEngine == null) {
            if (left <= 0) {
                return 0
            }
        } else {
            val isOutsideRightRangeAndBounceNotPossible = left < -mRightDragViewEngine!!.dragDistance && !getDragViewForEngine(mRightDragViewEngine)!!.isBouncePossible
            if (isOutsideRightRangeAndBounceNotPossible) {
                return -mRightDragViewEngine!!.dragDistance
            }
        }


        if (mLeftDragViewEngine == null) {
            if (left <= -mRightDragViewEngine!!.dragDistance) {
                return -mRightDragViewEngine!!.dragDistance
            }
        } else {
            val isOutsideLeftRangeAndBounceNotPossible = left > mLeftDragViewEngine!!.dragDistance && !getDragViewForEngine(mLeftDragViewEngine)!!.isBouncePossible


            if (isOutsideLeftRangeAndBounceNotPossible) {
                return mLeftDragViewEngine!!.dragDistance
            }
        }


        return left
    }


    override fun restoreState(state: SwipeState.DragViewState, view: SurfaceView) {

        when (state) {
            LEFT_OPEN -> dragView?.x = mLeftDragViewEngine!!.dragDistance.toFloat()
            RIGHT_OPEN -> dragView?.x = (-mRightDragViewEngine!!.dragDistance).toFloat()
            SwipeState.DragViewState.TOP_OPEN -> {
            }
            SwipeState.DragViewState.BOTTOM_OPEN -> {
            }
            else -> dragView?.x = 0f
        }//TODO Implementation
        //TODO Implementation
    }

    private fun getDragViewForEngine(engine: DraggingEngine<*>?): DragView? {
        return if (engine != null) engine.dragView as DragView else null
    }

    companion object {

        val AUTO_OPEN_SPEED_TRESHOLD = 800.0
        val LEFT_FULL_AUTO_OPEN_TRESHOLD = AUTO_OPEN_SPEED_TRESHOLD * 2
        val RIGHT_FULL_AUTO_OPEN_TRESHOLD = -LEFT_FULL_AUTO_OPEN_TRESHOLD
    }
}
