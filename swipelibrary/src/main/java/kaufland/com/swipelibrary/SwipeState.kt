package kaufland.com.swipelibrary



class SwipeState {

    var state = DragViewState.CLOSED

    enum class DragViewState {
        LEFT_OPEN, RIGHT_OPEN, TOP_OPEN, BOTTOM_OPEN, CLOSED
    }

}
