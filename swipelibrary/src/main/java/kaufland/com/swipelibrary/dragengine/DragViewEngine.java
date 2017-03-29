package kaufland.com.swipelibrary.dragengine;

import android.graphics.Rect;
import android.view.View;

import kaufland.com.swipelibrary.SurfaceView;
import kaufland.com.swipelibrary.SwipeState;
import kaufland.com.swipelibrary.SwipeViewLayouter;

/**
 * Created by sbra0902 on 29.03.17.
 */

public interface DragViewEngine {

    void moveView(float offset, SurfaceView view, View changedView);

    void initializePosition(Rect surfaceRect, SwipeViewLayouter.DragDirection orientation);
    void moveToInitial();
    void restoreState(SwipeState.DragViewState state, SurfaceView view);


}
