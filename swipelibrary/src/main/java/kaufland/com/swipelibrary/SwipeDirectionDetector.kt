package kaufland.com.swipelibrary

import android.graphics.Rect


/**
 * Created by sbra0902 on 29.03.17.
 */
class SwipeDirectionDetector {

    var xDown: Int = 0
        private set

    private var xUp: Int = 0

    var yDown: Int = 0
        private set

    private var yUp: Int = 0

    private var downRect: Rect? = null
    private var upRect: Rect? = null

    val difX: Int
        get() = xUp - xDown

    val difY: Int
        get() = yDown - yUp

    val swipeDirection: Int
        get() = if (difX > 0) SWIPE_DIRECTION_RIGHT else SWIPE_DIRECTION_LEFT

    val isHorizontalScrollChangedWhileDragging: Boolean
        get() = downRect == null || upRect == null || downRect!!.top != upRect!!.top


    fun onActionDown(x: Float, y: Float, swipeLayout: SwipeLayout) {
        xDown = x.toInt()
        yDown = y.toInt()

        downRect = Rect()
        swipeLayout.getGlobalVisibleRect(downRect)
    }

    fun onActionUp(x: Float, y: Float, swipeLayout: SwipeLayout) {

        onAction(x, y)

        upRect = Rect()
        swipeLayout.getGlobalVisibleRect(upRect)
    }

    fun onAction(x: Float, y: Float) {
        xUp = x.toInt()
        yUp = y.toInt()
    }

    companion object {

        const val SWIPE_DIRECTION_LEFT = 1

        const val SWIPE_DIRECTION_RIGHT = 2
    }
}
