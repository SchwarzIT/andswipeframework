package kaufland.com.swipelibrary;

/**
 * Created by rram2303 on 11/21/16.
 */

public interface SwipeableView {

    void moveView(float offset);
    void moveToInitial();
    void restoreState(SwipeState.DragViewState state, SurfaceView view);
    void onSwipe();
    void onClose();
    void onFullSwipe();
}
