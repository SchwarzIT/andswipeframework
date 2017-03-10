package kaufland.com.swipelibrary;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;

/**
 * Created by sbra0902 on 06.03.17.
 */
@EBean
public class SwipeViewLayouter {

    protected Map<Integer, DragView> mDragViews = new HashMap<>();

    protected SurfaceView mSurfaceView;

    private DragDirection mDragDirection = DragDirection.NONE;

    public enum DragDirection {
        HORIZONTAL, VERTICAL, NONE
    }

    private boolean isInitilized;

    public void init(ViewGroup parent) {

        isInitilized = true;

        mSurfaceView = null;
        mDragViews.clear();

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof DragView) {
                mDragViews.put(((DragView)child).getViewPosition(), (DragView) child);
                mDragDirection = ((DragView)child).getViewPosition() <= 2 ? HORIZONTAL : VERTICAL;
            } else if (child instanceof SurfaceView) {
                mSurfaceView = (SurfaceView) child;
            } else {
                throw new InvalidParameterException("Only DragView or SurfaceView are supported members of SwipeLayout");
            }
        }

        if (mSurfaceView == null) {
            throw new InvalidParameterException("SurfaceView in mandatory for SwipeLayout");
        }

        if(mDragViews.size() <= 0){
            throw new InvalidParameterException("SwipeLayout needs at least 1 DragView");
        }


    }

    public void initInitialPosition(Rect surfaceRect){

        for (DragView dragView : mDragViews.values()) {
           dragView.initializePosition(surfaceRect, mDragDirection);
        }

        mSurfaceView.initializePosition(surfaceRect, mDragDirection);

    }

    public DragDirection getDragDirection() {
        return mDragDirection;
    }

    public DragView getDragViewByPosition(int position){
        return mDragViews.get(position);
    }


    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void moveView(int positionChanges) {
        for (DragView view: mDragViews.values()) {
            if(mDragDirection == DragDirection.HORIZONTAL){
                if(view.getViewPosition() == LEFT_DRAG_VIEW || view.getViewPosition() == RIGHT_DRAG_VIEW){
                    view.moveView(positionChanges, mSurfaceView);
                }
            }
        }
    }

    public void restoreState(SwipeState.DragViewState state){

        for (DragView view: mDragViews.values()) {
                if(view.getViewPosition() == LEFT_DRAG_VIEW || view.getViewPosition() == RIGHT_DRAG_VIEW){
                    view.restoreState(state, mSurfaceView);
                }
        }
        mSurfaceView.requestLayout();
    }

    public void onStopDraggingToClosed() {
        if (mDragDirection == HORIZONTAL) {

            for (DragView view: mDragViews.values()) {
                if(view.getViewPosition() == LEFT_DRAG_VIEW || view.getViewPosition() == RIGHT_DRAG_VIEW){
                    view.onClose();
                }
            }

        } else if (mDragDirection == VERTICAL) {
            throw new UnsupportedOperationException("VerticalSwipeNotImplemented");
        }
    }

    public int determineDraggingRange(int distance) {

        DragView view = findViewBySwipeDirection(distance);

        return view != null && view.isDraggable() ? distance : 0;
    }

    public DragView findViewBySwipeDirection(int distance){

        if(mDragDirection == HORIZONTAL){
            if(distance < 0){
                return getDragViewByPosition(RIGHT_DRAG_VIEW);
            }else if(distance > 0){
                return getDragViewByPosition(LEFT_DRAG_VIEW);
            }
        }

        return null;
    }

    public boolean isDirectionSwipeable(float startX, float endX, SwipeViewLayouter layouter){
        float diffX = endX - startX;

        DragView view = findViewBySwipeDirection((int) diffX);

        return (view != null && view.isDraggable()) || (diffX <= 0 ? layouter.getSurfaceView().getLeft() > 0 : layouter.getSurfaceView().getLeft() < 0);
    }

    public boolean isInitilized() {
        return isInitilized;
    }
}
