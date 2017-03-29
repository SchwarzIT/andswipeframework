package kaufland.com.swipelibrary.dragengine;

import android.graphics.Rect;
import android.view.View;

import kaufland.com.swipelibrary.DragView;
import kaufland.com.swipelibrary.SurfaceView;
import kaufland.com.swipelibrary.SwipeState;
import kaufland.com.swipelibrary.SwipeViewLayouter;

import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;

/**
 * Created by sbra0902 on 29.03.17.
 */

public class RightDragViewEngine implements DragViewEngine {

    private DragView mDragView;

    private int mInitialXPos;

    private int mDragDistance;

    private int mIntermmediateDistance;

    public RightDragViewEngine(DragView dragView) {
        mDragView = dragView;
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {

        if (!mDragView.equals(changedView)) {
            mDragView.setX(view.getX() + mDragView.getWidth());
        } else {
            view.moveView(mDragView.getX() - view.getWidth());
        }
    }

    @Override
    public void initializePosition(Rect surfaceRect, SwipeViewLayouter.DragDirection orientation) {

        mDragDistance = mDragView.getWidth();
        mInitialXPos = surfaceRect.right + mDragView.getWidth();
        mIntermmediateDistance = mDragView.getSettlePointResourceId() != -1 ? mDragView.findViewById(mDragView.getSettlePointResourceId()).getRight() : mDragView.getWidth();

        moveToInitial();
    }

    @Override
    public void moveToInitial() {
        mDragView.setX(mInitialXPos);
    }

    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        switch (state) {
            case LEFT_OPEN:
                moveToInitial();
                break;
            case RIGHT_OPEN:
                moveView(mDragDistance, view, null);
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
}
