package kaufland.com.swipelibrary

import android.view.View
import android.view.ViewGroup

import java.security.InvalidParameterException
import java.util.HashMap

import kaufland.com.swipelibrary.dragengine.DraggingEngine
import kaufland.com.swipelibrary.dragengine.LeftDragViewEngine
import kaufland.com.swipelibrary.dragengine.RightDragViewEngine
import kaufland.com.swipelibrary.dragengine.SurfaceViewEngine

import kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL
import kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL


class SwipeViewLayouter {

    private val mViewEngines = HashMap<Int, DraggingEngine<*>>()

    private val mViews = HashMap<Int, View>()

    var dragDirection = DragDirection.NONE
        private set

    val viewEngines: Map<Int, DraggingEngine<*>>
        get() = mViewEngines

    val views: Map<Int, View>
        get() = mViews

    val surfaceView: SurfaceView
        get() = mViews[SwipeLayout.SURFACE_VIEW] as SurfaceView

    val leftDragView: DragView
        get() = mViews[SwipeLayout.LEFT_DRAG_VIEW] as DragView

    val rightDragView: DragView
        get() = mViews[SwipeLayout.RIGHT_DRAG_VIEW] as DragView

    enum class DragDirection {
        HORIZONTAL, VERTICAL, NONE
    }


    fun init(parent: ViewGroup) {

        mViewEngines.clear()

        mViews.clear()

        for (i in 0 until parent.childCount) {

            val child = parent.getChildAt(i)

            if (child is DragView) {
                mViews[child.viewPosition] = child
                dragDirection = if (child.viewPosition <= 2) HORIZONTAL else VERTICAL
            } else if (child is SurfaceView) {
                mViews[SwipeLayout.SURFACE_VIEW] = child
            } else {
                throw InvalidParameterException("Only DragView or SurfaceView are supported members of SwipeLayout")
            }
        }



        for (key in mViews.keys) {

            when (key) {
                SwipeLayout.LEFT_DRAG_VIEW -> mViewEngines[SwipeLayout.LEFT_DRAG_VIEW] = LeftDragViewEngine<DragView>(this)
                SwipeLayout.RIGHT_DRAG_VIEW -> mViewEngines[SwipeLayout.RIGHT_DRAG_VIEW] = RightDragViewEngine<DragView>(this)
                SwipeLayout.SURFACE_VIEW -> mViewEngines[SwipeLayout.SURFACE_VIEW] = SurfaceViewEngine(this)
            }
        }

        if (!mViews.containsKey(SwipeLayout.SURFACE_VIEW)) {
            throw InvalidParameterException("SwipeLayout needs a SurfaceView")
        }

        if (mViews.size <= 1) {
            throw InvalidParameterException("SwipeLayout needs at least 1 DragView")
        }


    }

    fun DragViewEngineByPosition(position: Int): DraggingEngine<*>? {
        return mViewEngines[position]
    }

}
