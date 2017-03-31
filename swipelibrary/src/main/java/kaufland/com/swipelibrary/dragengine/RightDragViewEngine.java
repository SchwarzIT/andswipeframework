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

import static kaufland.com.swipelibrary.SwipeDirectionDetector.SWIPE_DIRECTION_RIGHT;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED;

/**
 * Created by sbra0902 on 29.03.17.
 */

public class RightDragViewEngine implements DraggingEngine {

    private final SwipeViewLayouter mLayouter;
    private DragView mDragView;

    private SurfaceView mSurfaceView;

    private int mInitialXPos;

    private int mDragDistance;

    private int mIntermmediateDistance;

    public RightDragViewEngine(SwipeViewLayouter layouter) {
        mLayouter = layouter;
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {

        if (!mDragView.equals(changedView)) {
            mDragView.setX(view.getX() + mDragView.getWidth());
        }
    }

    @Override
    public void initializePosition(SwipeViewLayouter.DragDirection orientation) {

        mDragView = (DragView) mLayouter.getViews().get(SwipeLayout.RIGHT_DRAG_VIEW);
        mSurfaceView = mLayouter.getSurfaceView();
        mDragDistance = mDragView.getWidth();
        mInitialXPos = (int) (mSurfaceView.getX() + mDragView.getWidth());
        mIntermmediateDistance = mDragView.getSettlePointResourceId() != -1 ? mDragView.findViewById(mDragView.getSettlePointResourceId()).getRight() : mDragView.getWidth();

        moveToInitial();
    }

    private void moveToInitial() {
        mDragView.setX(mInitialXPos);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left) {
        if (left < (mSurfaceView.getWidth() - getDragDistance()) && !mDragView.isBouncePossible()) {
            return mSurfaceView.getWidth() - getDragDistance();
        }

        return left;
    }

    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        switch (state) {
            case LEFT_OPEN:
                mDragView.setX(mLayouter.getLeftDragView().getWidth() + mSurfaceView.getWidth());
                break;
            case RIGHT_OPEN:
                mDragView.setX(0);
                break;
            case TOP_OPEN:
                //TODO Implementation
                break;
            case BOTTOM_OPEN:
                //TODO Implementation
                break;
            default:

                mDragView.setX(mSurfaceView.getWidth());
                break;
        }
    }


    @Override
    public int getDragDistance() {
        return mDragDistance;
    }

    @Override
    public int getIntermmediateDistance() {
        return mIntermmediateDistance;
    }

    @Override
    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, final SwipeLayout.SwipeListener swipeListener, View releasedChild) {
        if (releasedChild.equals(getDragView()) && swipeDirectionDetector.getSwipeDirection() == SWIPE_DIRECTION_RIGHT) {
            swipeState.setState(SwipeState.DragViewState.CLOSED);
            return new SwipeResult(mSurfaceView.getWidth(), new Runnable() {
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

    @Override
    public int getOpenOffset() {
        return -mDragDistance;
    }
}
