package kaufland.com.swipelibrary;

import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;


public class SwipeUtil {


    public static boolean canSwipe(float x1, float y1, float x2, float y2, SwipeState.DragViewState state, SwipeViewLayouter layouter) {

        boolean canSwipe = false;

        float absDiffX = Math.abs(x1 - x2);
        float absDiffY = Math.abs(y1 - y2);
        float diffX = x2 - x1;
        float diffY = y2 - y1;
        boolean isLeftDraggable = layouter.getDragViewEngineByPosition(LEFT_DRAG_VIEW) != null ? layouter.getDragViewEngineByPosition(LEFT_DRAG_VIEW).isDraggable() : false;
        boolean isRightDraggable = layouter.getDragViewEngineByPosition(RIGHT_DRAG_VIEW) != null ? layouter.getDragViewEngineByPosition(RIGHT_DRAG_VIEW).isDraggable() : false;

        if (layouter.getDragDirection() == HORIZONTAL) {
            if (diffX > 0) {
                canSwipe = absDiffX > absDiffY && (isLeftDraggable || state == SwipeState.DragViewState.RIGHT_OPEN || layouter.getSurfaceView().getLeft() < 0);
            } else {
                canSwipe = absDiffX > absDiffY && (isRightDraggable || state == SwipeState.DragViewState.LEFT_OPEN || layouter.getSurfaceView().getLeft() > 0);
            }

        }

        return canSwipe;
    }

    public static boolean canBounce(float velocity, SwipeViewLayouter.DragDirection direction, SwipeState state) {
        switch (direction) {
            case HORIZONTAL:
                if (state.getState() == SwipeState.DragViewState.LEFT_OPEN) {
                    return velocity > 0;
                } else if (state.getState() == SwipeState.DragViewState.RIGHT_OPEN) {
                    return velocity < 0;
                }
                break;

            case VERTICAL:
                if (state.getState() == SwipeState.DragViewState.TOP_OPEN) {
                    return velocity > 0;
                } else if (state.getState() == SwipeState.DragViewState.BOTTOM_OPEN) {
                    return velocity < 0;
                }
                break;
        }

        return false;
    }


}
