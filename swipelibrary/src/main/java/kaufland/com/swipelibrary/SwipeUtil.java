package kaufland.com.swipelibrary;

import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;


public class SwipeUtil {




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
