package kaufland.com.swipelibrary;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by rram2303 on 11/21/16.
 */

public interface SwipeableView {

    void moveView(float offset, SurfaceView view);

    void initializePosition(Rect surfaceRect, SwipeViewLayouter.DragDirection orientation);
    void moveToInitial();
    void restoreState(SwipeState.DragViewState state, SurfaceView view);
    void onSwipe();
    void onClose();
    void onFullSwipe();
}
