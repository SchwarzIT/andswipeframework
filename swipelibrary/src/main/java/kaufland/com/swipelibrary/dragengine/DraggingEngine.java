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

/**
 * Created by sbra0902 on 29.03.17.
 */

public interface DraggingEngine<T extends View> {

    void moveView(float offset, SurfaceView view, View changedView);

    void initializePosition(SwipeViewLayouter.DragDirection orientation);

    int clampViewPositionHorizontal(View child, int left);

    void restoreState(SwipeState.DragViewState state, SurfaceView view);

    T getDragView();

    int getDragDistance();

    int getIntermmediateDistance();

    SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, SwipeLayout.SwipeListener swipeListener, View releasedChild);
}
