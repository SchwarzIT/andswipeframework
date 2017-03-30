package kaufland.com.swipelibrary;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import kaufland.com.swipelibrary.dragengine.DraggingEngine;

import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.SURFACE_VIEW;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;

/**
 * Created by sbra0902 on 30.03.17.
 */
@EBean
public class DraggingProxy {

    @Bean
    protected SwipeViewLayouter mSwipeViewLayouter;

    private boolean isInitilized;


    public void init(ViewGroup parent) {
        isInitilized = true;
        mSwipeViewLayouter.init(parent);
        initInitialPosition();
    }


    public void requestLayout() {
        for (DraggingEngine dragView : mSwipeViewLayouter.getViewEngines().values()) {
            dragView.forceLayout();
        }
    }

    public int clampViewPositionHorizontal(View child, int left) {

        for (DraggingEngine dragView : mSwipeViewLayouter.getViewEngines().values()) {
            if (dragView.getDragView().equals(child)) {
                return dragView.clampViewPositionHorizontal(child, left);
            }
        }
        return left;
    }

    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, SwipeLayout.SwipeListener swipeListener, View child) {
        for (DraggingEngine dragView : mSwipeViewLayouter.getViewEngines().values()) {

            SwipeResult result = dragView.determineSwipeHorizontalState(velocity, swipeDirectionDetector, swipeState, swipeListener, child);
            if (result != null) {
                return result;
            }

        }
        return new SwipeResult(0);
    }

    public void initInitialPosition() {

        for (DraggingEngine dragView : mSwipeViewLayouter.getViewEngines().values()) {
            dragView.initializePosition(mSwipeViewLayouter.getDragDirection());
        }
    }

    public void moveView(View changedView, int positionChanges) {

        for (DraggingEngine engine : mSwipeViewLayouter.getViewEngines().values()) {
            engine.moveView(positionChanges, (SurfaceView) mSwipeViewLayouter.getViews().get(SURFACE_VIEW), changedView);
        }

    }

    public void restoreState(SwipeState.DragViewState state) {

        for (DraggingEngine view : mSwipeViewLayouter.getViewEngines().values()) {
            view.restoreState(state, (SurfaceView)  mSwipeViewLayouter.getViews().get(SURFACE_VIEW));
        }
    }

    public SwipeViewLayouter.DragDirection getDragDirection() {
        return mSwipeViewLayouter.getDragDirection();
    }

    public boolean isCapturedViewDraggable(View child) {

        boolean draggable = false;

        for (View view : mSwipeViewLayouter.getViews().values()) {

            draggable = draggable || child.getId() == view.getId();
        }

        return draggable;
    }

    public boolean canSwipe(float x1, float y1, float x2, float y2, SwipeState.DragViewState state) {

        boolean canSwipe = false;

        float absDiffX = Math.abs(x1 - x2);
        float absDiffY = Math.abs(y1 - y2);
        float diffX = x2 - x1;
        float diffY = y2 - y1;
        boolean isLeftDraggable = mSwipeViewLayouter.getDragViewEngineByPosition(LEFT_DRAG_VIEW) != null ? mSwipeViewLayouter.getDragViewEngineByPosition(LEFT_DRAG_VIEW).isDraggable() : false;
        boolean isRightDraggable = mSwipeViewLayouter.getDragViewEngineByPosition(RIGHT_DRAG_VIEW) != null ? mSwipeViewLayouter.getDragViewEngineByPosition(RIGHT_DRAG_VIEW).isDraggable() : false;

        if (mSwipeViewLayouter.getDragDirection() == HORIZONTAL) {
            if (diffX > 0) {
                canSwipe = absDiffX > absDiffY && (isLeftDraggable || state == SwipeState.DragViewState.RIGHT_OPEN || mSwipeViewLayouter.getSurfaceView().getLeft() < 0);
            } else {
                canSwipe = absDiffX > absDiffY && (isRightDraggable || state == SwipeState.DragViewState.LEFT_OPEN || mSwipeViewLayouter.getSurfaceView().getLeft() > 0);
            }

        }

        return canSwipe;
    }

    public boolean isInitilized() {
        return isInitilized;
    }
}
