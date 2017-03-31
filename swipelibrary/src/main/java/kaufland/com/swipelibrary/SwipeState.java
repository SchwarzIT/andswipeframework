package kaufland.com.swipelibrary;

import android.util.Log;

import org.androidannotations.annotations.EBean;

@EBean
public class SwipeState {

    private boolean isOpen;
    private DragViewState mState = DragViewState.CLOSED;
    private boolean isSettled;
    private int settleState;


    public enum DragViewState{
        LEFT_OPEN, RIGHT_OPEN, TOP_OPEN, BOTTOM_OPEN, CLOSED
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public DragViewState getState() {
        return mState;
    }

    public void setState(DragViewState mState) {
        this.mState = mState;
    }


}
