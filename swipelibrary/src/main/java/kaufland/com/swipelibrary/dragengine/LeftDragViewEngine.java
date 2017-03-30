package kaufland.com.swipelibrary.dragengine;

import android.graphics.Rect;
import android.view.View;

import kaufland.com.swipelibrary.DragView;
import kaufland.com.swipelibrary.SurfaceView;
import kaufland.com.swipelibrary.SwipeDirectionDetector;
import kaufland.com.swipelibrary.SwipeLayout;
import kaufland.com.swipelibrary.SwipeResult;
import kaufland.com.swipelibrary.SwipeState;
import kaufland.com.swipelibrary.SwipeViewLayouter;

import static kaufland.com.swipelibrary.SwipeDirectionDetector.SWIPE_DIRECTION_LEFT;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED;

/**
 * Created by sbra0902 on 29.03.17.
 */

public class LeftDragViewEngine implements DraggingEngine {

    private SurfaceView mSurfaceView;
    private DragView mDragView;

    private int mInitialXPos;

    private int mDragDistance;

    private int mIntermmediateDistance;

    public LeftDragViewEngine(DragView dragView, SurfaceView surfaceView) {
        mDragView = dragView;
        mSurfaceView = surfaceView;
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {
        if (!mDragView.equals(changedView)) {
            mDragView.setX(view.getX() - mDragView.getWidth());
        }
    }

    @Override
    public void initializePosition(SwipeViewLayouter.DragDirection orientation) {

        mInitialXPos = (int) (mSurfaceView.getX() - mDragView.getWidth());
        mDragDistance = mDragView.getWidth();
        mIntermmediateDistance = mDragView.getSettlePointResourceId() != -1 ? mDragView.findViewById(mDragView.getSettlePointResourceId()).getRight() : mDragView.getWidth();

        moveToInitial();
    }

    @Override
    public void moveToInitial() {
        mDragView.setX(mInitialXPos);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left) {

        if (mDragView != null && child.equals(mDragView)) {


            if (left > 0 && !mDragView.isBouncePossible()) {
                return 0;
            }

            return left;
        }

        return 0;
    }

    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        switch (state) {
            case LEFT_OPEN:
                moveView(mDragDistance, view, null);
                break;
            case RIGHT_OPEN:

                moveToInitial();

                break;
            case TOP_OPEN:
                //TODO Implementation
                break;
            case BOTTOM_OPEN:
                //TODO Implementation
                break;
            default:

                moveToInitial();
                break;
        }
    }

    @Override
    public int getWidth() {
        return mDragView.getWidth();
    }

    @Override
    public int getDragDistance() {
        return mDragDistance;
    }

    @Override
    public void forceLayout() {
        mDragView.forceLayout();
    }

    @Override
    public boolean isBouncePossible() {
        return mDragView.isBouncePossible();
    }

    @Override
    public int getId() {
        return mDragView.getId();
    }

    @Override
    public boolean isDraggable() {
        return mDragView.isDraggable();
    }

    @Override
    public int getIntermmediateDistance() {
        return mIntermmediateDistance;
    }

    @Override
    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, final SwipeLayout.SwipeListener swipeListener, View releasedChild) {
        if (mDragView.equals(releasedChild) && swipeDirectionDetector.getSwipeDirection() == SWIPE_DIRECTION_LEFT) {
            swipeState.setState(SwipeState.DragViewState.CLOSED);
            return new SwipeResult(-mDragView.getWidth(), new Runnable() {
                @Override
                public void run() {
                    swipeListener.onSwipeClosed(CLOSED);
                }
            });
        }

        return null;
    }

    @Override
    public DragView getDragView() {
        return mDragView;
    }
}
