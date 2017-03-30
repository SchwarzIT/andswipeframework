package kaufland.com.swipelibrary;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EBean;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import kaufland.com.swipelibrary.dragengine.DraggingEngine;
import kaufland.com.swipelibrary.dragengine.LeftDragViewEngine;
import kaufland.com.swipelibrary.dragengine.RightDragViewEngine;
import kaufland.com.swipelibrary.dragengine.SurfaceViewEngine;

import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.SURFACE_VIEW;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL;


@EBean
public class SwipeViewLayouter {

    private Map<Integer, DraggingEngine> mViewEngines = new HashMap<>();

    private Map<Integer, View> mViews = new HashMap<>();

    private DragDirection mDragDirection = DragDirection.NONE;

    public enum DragDirection {
        HORIZONTAL, VERTICAL, NONE
    }


    public void init(ViewGroup parent) {

        mViewEngines.clear();

        mViews = new HashMap<>();

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof DragView) {
                mViews.put(((DragView) child).getViewPosition(), child);
                mDragDirection = ((DragView) child).getViewPosition() <= 2 ? HORIZONTAL : VERTICAL;
            } else if (child instanceof SurfaceView) {
                mViews.put(SURFACE_VIEW, child);
            } else {
                throw new InvalidParameterException("Only DragView or SurfaceView are supported members of SwipeLayout");
            }
        }



        for (Integer key : mViews.keySet()) {

            switch (key){
                case LEFT_DRAG_VIEW:
                    mViewEngines.put(LEFT_DRAG_VIEW, new LeftDragViewEngine(this));
                case RIGHT_DRAG_VIEW:
                    mViewEngines.put(RIGHT_DRAG_VIEW, new RightDragViewEngine(this));
                case SURFACE_VIEW:
                    mViewEngines.put(SURFACE_VIEW, new SurfaceViewEngine(this));
            }
        }


        if (mViews.size() <= 1) {
            throw new InvalidParameterException("SwipeLayout needs at least 1 DragView");
        }

        if (!mViews.containsKey(SURFACE_VIEW)) {
            throw new InvalidParameterException("SwipeLayout needs a SurfaceView");
        }


    }

    public DragDirection getDragDirection() {
        return mDragDirection;
    }

    public DraggingEngine getDragViewEngineByPosition(int position) {
        return mViewEngines.get(position);
    }

    public Map<Integer, DraggingEngine> getViewEngines() {
        return mViewEngines;
    }

    public Map<Integer, View> getViews() {
        return mViews;
    }

    public SurfaceView getSurfaceView() {
        return (SurfaceView) mViews.get(SURFACE_VIEW);
    }

    public DragView getLeftDragView() {
        return (DragView) mViews.get(LEFT_DRAG_VIEW);
    }

    public DragView getRightDragView() {
        return (DragView) mViews.get(RIGHT_DRAG_VIEW);
    }

}
