package kaufland.com.swipelibrary;

import android.view.View;

import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_FULL_AUTO_OPEN_TRESHOLD;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_FULL_AUTO_OPEN_TRESHOLD;


public class SwipeUtil {

    private static final int SWIPE_DIRECTION_LEFT = 1;

    private static final int SWIPE_DIRECTION_RIGHT = 2;

    public static boolean determineSwipeSettleStateHorizontal(float xvel, DragRange range) {
        int halfRange = (range.getCheckRange() / 2);

        if (range.getDraggingBorder() > 0) {
            //xvel > AUTO_OPEN_SPEED_TRESHOLD ||
            return range.getDraggingBorder() > halfRange;
        } else if (range.getDraggingBorder() < 0) {
            //xvel > -AUTO_OPEN_SPEED_TRESHOLD ||
            return range.getDraggingBorder() < halfRange;
        }

        return false;
    }


    public static int determineSwipeHorizontalState(float velocity, SwipeState swipeState, DragRange range, SwipeViewLayouter layouter, View releasedChild) {
        int settleX = 0;
        int swipeDirection = range.getDifx() < 0 ? SWIPE_DIRECTION_RIGHT : SWIPE_DIRECTION_LEFT;

        DragView leftDragView = layouter.getDragViewByPosition(LEFT_DRAG_VIEW);
        DragView rightDragView = layouter.getDragViewByPosition(RIGHT_DRAG_VIEW);


        if (releasedChild.equals(leftDragView)) {

            if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                swipeState.setState(SwipeState.DragViewState.CLOSED);
                return -leftDragView.getWidth();
            }

        } else if (releasedChild.equals(rightDragView)) {

            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                swipeState.setState(SwipeState.DragViewState.CLOSED);
                return layouter.getSurfaceView().getWidth();
            }

        } else {
            if (swipeState.getState() == SwipeState.DragViewState.CLOSED) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                    if (!leftDragView.isDraggable()) {
                        return settleX;
                    }

                    swipeState.setState(SwipeState.DragViewState.LEFT_OPEN);
                    return velocity > LEFT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (rightDragView.getDragDistance() / 2) ? range.getHorizontalRange() : leftDragView.getIntermmediateDistance();
                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    if (!rightDragView.isDraggable()) {
                        return settleX;
                    }

                    settleX = velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (rightDragView.getDragDistance() / 2) ? -range.getHorizontalRange() : -rightDragView.getIntermmediateDistance();
                    swipeState.setState(settleX == 0 ? SwipeState.DragViewState.CLOSED : SwipeState.DragViewState.RIGHT_OPEN);
                    return settleX;
                }
            } else if (swipeState.getState() == SwipeState.DragViewState.LEFT_OPEN) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                    swipeState.setState(SwipeState.DragViewState.LEFT_OPEN);
                    return leftDragView.getIntermmediateDistance();
                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    swipeState.setState(SwipeState.DragViewState.CLOSED);
                    return range.getHorizontalMinRange();
                }
            } else if (swipeState.getState() == SwipeState.DragViewState.RIGHT_OPEN) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                    swipeState.setState(SwipeState.DragViewState.CLOSED);
                    return range.getHorizontalMinRange();
                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    settleX = velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (rightDragView.getDragDistance() / 2) ? -range.getHorizontalRange() : -rightDragView.getIntermmediateDistance();
                    swipeState.setState(settleX == 0 ? SwipeState.DragViewState.CLOSED : SwipeState.DragViewState.RIGHT_OPEN);
                }
            }
        }


        return settleX;
    }


    public static boolean canSwipe(float x1, float y1, float x2, float y2, SwipeState.DragViewState state, SwipeViewLayouter layouter) {

        boolean canSwipe = false;

        float absDiffX = Math.abs(x1 - x2);
        float absDiffY = Math.abs(y1 - y2);
        float diffX = x2 - x1;
        float diffY = y2 - y1;
        boolean isLeftDraggable = layouter.getDragViewByPosition(LEFT_DRAG_VIEW) != null ? layouter.getDragViewByPosition(LEFT_DRAG_VIEW).isDraggable() : false;
        boolean isRightDraggable = layouter.getDragViewByPosition(RIGHT_DRAG_VIEW) != null ? layouter.getDragViewByPosition(RIGHT_DRAG_VIEW).isDraggable() : false;

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
