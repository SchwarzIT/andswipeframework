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

import kaufland.com.swipelibrary.dragengine.DragViewEngine;
import kaufland.com.swipelibrary.dragengine.LeftDragViewEngine;
import kaufland.com.swipelibrary.dragengine.RightDragViewEngine;

import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;

/**
 * Created by sbra0902 on 06.03.17.
 */
@EBean
public class SwipeViewLayouter {

    protected Map<Integer, DragViewEngine> mDragViews = new HashMap<>();

    protected SurfaceView mSurfaceView;

    private DragDirection mDragDirection = DragDirection.NONE;

    public void requestLayout() {
        for (DragViewEngine dragView : mDragViews.values()) {
            dragView.forceLayout();
        }

        mSurfaceView.forceLayout();
    }

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
                mDragViews.put(((DragView) child).getViewPosition(), createDragViewEngine((DragView) child));
                mDragDirection = ((DragView) child).getViewPosition() <= 2 ? HORIZONTAL : VERTICAL;
            } else if (child instanceof SurfaceView) {
                mSurfaceView = (SurfaceView) child;
            } else {
                throw new InvalidParameterException("Only DragView or SurfaceView are supported members of SwipeLayout");
            }
        }

        if (mSurfaceView == null) {
            throw new InvalidParameterException("SurfaceView in mandatory for SwipeLayout");
        }

        if (mDragViews.size() <= 0) {
            throw new InvalidParameterException("SwipeLayout needs at least 1 DragView");
        }


    }

    private DragViewEngine createDragViewEngine(DragView child) {

        switch (child.getViewPosition()){
            case LEFT_DRAG_VIEW:
                return new LeftDragViewEngine(child);
            case RIGHT_DRAG_VIEW:
                return new RightDragViewEngine(child);
        }
        return null;
    }

    public void initInitialPosition(Rect surfaceRect) {

        for (DragViewEngine dragView : mDragViews.values()) {
            dragView.initializePosition(surfaceRect, mDragDirection);
        }

        mSurfaceView.initializePosition(surfaceRect, mDragDirection);

    }

    public DragDirection getDragDirection() {
        return mDragDirection;
    }

    public DragViewEngine getDragViewEngineByPosition(int position) {
        return mDragViews.get(position);
    }


    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void moveView(View changedView, int positionChanges) {

        if (changedView.equals(mSurfaceView)) {
            mSurfaceView.moveView(positionChanges);
        }

        for (DragViewEngine view : mDragViews.values()) {
            if (mDragDirection == DragDirection.HORIZONTAL) {
                view.moveView(positionChanges, mSurfaceView, changedView);
            }
        }
    }

    public void restoreState(SwipeState.DragViewState state) {

        for (DragViewEngine view : mDragViews.values()) {
            view.restoreState(state, mSurfaceView);
        }
        mSurfaceView.requestLayout();
    }

    public boolean isInitilized() {
        return isInitilized;
    }
}
