package kaufland.com.swipelibrary;

import android.graphics.Rect;

/**
 * Created by sbra0902 on 29.03.17.
 */

public class SwipeDirectionDetector {

    private int xDown;

    private int xUp;

    private int yDown;

    private int yUp;

    public static final int SWIPE_DIRECTION_LEFT = 1;

    public static final int SWIPE_DIRECTION_RIGHT = 2;

    private Rect downRect;
    private Rect upRect;


    public void onActionDown(float x, float y, final SwipeLayout swipeLayout) {
        xDown = (int) x;
        yDown = (int) y;

        downRect = new Rect();
        swipeLayout.getGlobalVisibleRect(downRect);
    }

    public void onActionUp(float x, float y, final SwipeLayout swipeLayout) {

        onAction(x, y);

        upRect = new Rect();
        swipeLayout.getGlobalVisibleRect(upRect);
    }

    public void onAction(float x, float y){
        xUp = (int) x;
        yUp = (int) y;
    }

    public int getDifX() {
        return xUp - xDown;
    }

    public int getDifY() {
        return yDown - yUp;
    }

    public int getXDown() {
        return xDown;
    }

    public int getYDown() {
        return yDown;
    }

    public int getSwipeDirection(){
      return   getDifX() > 0 ? SWIPE_DIRECTION_RIGHT : SWIPE_DIRECTION_LEFT;
    }

    public boolean isHorizontalScrollChangedWhileDragging() {
        return downRect == null || upRect == null || downRect.top != upRect.top;
    }
}
