package kaufland.com.swipelibrary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;

/**
 * Created by sbra0902 on 06.03.17.
 */
@EBean
public class SwipeViewLayouter {

    protected Map<Integer, DragView> mDragViews = new HashMap<>();

    protected SurfaceView mSurfaceView;

    private boolean isInitilized;

    public void init(ViewGroup parent) {

        isInitilized = true;

        mSurfaceView = null;
        mDragViews.clear();
        List<DragView> dragViews = new ArrayList<>();

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof DragView) {
                dragViews.add((DragView) child);
            } else if (child instanceof SurfaceView) {
                mSurfaceView = (SurfaceView) child;
            } else {
                throw new InvalidParameterException("Only DragView or SurfaceView are supported members of SwipeLayout");
            }
        }

        if (mSurfaceView == null) {
            throw new InvalidParameterException("SurfaceView in mandatory for SwipeLayout");
        }

        for (DragView dragView : dragViews) {
            if (mSurfaceView.getLeft() > dragView.getLeft()) {
                dragView.setViewPosition(LEFT_DRAG_VIEW);
            } else if (mSurfaceView.getRight() < dragView.getLeft()) {
                dragView.setViewPosition(RIGHT_DRAG_VIEW);
            }

            if (!dragView.isInitialized()) {
                dragView.moveToInitial();
            }

            mDragViews.put(dragView.getViewPosition(), dragView);
        }


    }

    public DragView getDragViewByPosition(int position){
        return mDragViews.get(position);
    }


    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void moveView(int dragDirection, int positionChanges) {
        for (DragView view: mDragViews.values()) {
            if(dragDirection == HORIZONTAL){
                if(view.getViewPosition() == LEFT_DRAG_VIEW || view.getViewPosition() == RIGHT_DRAG_VIEW){
                    view.moveView(positionChanges);
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

    public void onStopDraggingToClosed(int mDragDirection) {
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

    public int determineDraggingRange(int mDragDirection, int distance) {

        DragView view = findViewBySwipeDirection(mDragDirection, distance);

        return view != null && view.isDraggable() ? distance : 0;
    }

    private DragView findViewBySwipeDirection(int mDragDirection, int distance){
        switch (mDragDirection) {
            case HORIZONTAL:

                for (DragView view: mDragViews.values()) {
                    if(view.getViewPosition() == LEFT_DRAG_VIEW && distance > 0){
                        return view;
                    } else if(view.getViewPosition() == RIGHT_DRAG_VIEW && distance < 0){
                        return view;
                    }
                    return null;
                }
                break;

            case VERTICAL:

                break;
        }
        throw new UnsupportedOperationException("VerticalSwipeNotImplemented");
    }

    public boolean isDirectionSwipeable(float startX, float endX, SwipeViewLayouter layouter){
        boolean swipeable = false;
        float diffX = endX - startX;

        DragView view = findViewBySwipeDirection(HORIZONTAL, (int) diffX);

        return view == null || view.isDraggable() || layouter.getSurfaceView().getLeft() < 0;
    }

    public boolean isInitilized() {
        return isInitilized;
    }
}
