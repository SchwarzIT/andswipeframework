package kaufland.com.swipelibrary.dragengine

import android.graphics.Rect
import android.view.View

import kaufland.com.swipelibrary.DragView
import kaufland.com.swipelibrary.SurfaceView
import kaufland.com.swipelibrary.SwipeDirectionDetector
import kaufland.com.swipelibrary.SwipeLayout
import kaufland.com.swipelibrary.SwipeResult
import kaufland.com.swipelibrary.SwipeState
import kaufland.com.swipelibrary.SwipeViewLayouter

/**
 * Created by sbra0902 on 29.03.17.
 */

interface DraggingEngine<T : View> {

    val dragView: T?

    val dragDistance: Int

    val intermmediateDistance: Int

    fun openOffset(): Int

    fun moveView(offset: Float, view: SurfaceView, changedView: View)

    fun initializePosition(orientation: SwipeViewLayouter.DragDirection)

    fun clampViewPositionHorizontal(child: View, left: Int): Int

    fun restoreState(state: SwipeState.DragViewState, view: SurfaceView)

    fun determineSwipeHorizontalState(velocity: Float, swipeDirectionDetector: SwipeDirectionDetector, swipeState: SwipeState, swipeListener: SwipeLayout.SwipeListener?, releasedChild: View): SwipeResult?
}
