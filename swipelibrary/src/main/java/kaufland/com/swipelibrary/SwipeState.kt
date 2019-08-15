package kaufland.com.swipelibrary

import android.util.Log

import org.androidannotations.annotations.EBean

@EBean
class SwipeState {

    var isOpen: Boolean = false
    var state = DragViewState.CLOSED
    private val isSettled: Boolean = false
    private val settleState: Int = 0


    enum class DragViewState {
        LEFT_OPEN, RIGHT_OPEN, TOP_OPEN, BOTTOM_OPEN, CLOSED
    }


}
