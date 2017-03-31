package kaufland.com.swipelibrary;

import android.graphics.Rect;
import android.view.ViewTreeObserver;

import org.androidannotations.annotations.EBean;

/**
 * Created by sbra0902 on 29.03.17.
 */
@EBean
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
        xUp = (int) x;
        yUp = (int) y;

        upRect = new Rect();
        swipeLayout.getGlobalVisibleRect(upRect);
    }

    public int getDifX() {
        return xDown - xUp;
    }

    public int getDifY() {
        return yDown - yUp;
    }

    public int getSwipeDirection(){
      return   getDifX() < 0 ? SWIPE_DIRECTION_RIGHT : SWIPE_DIRECTION_LEFT;
    }

    public boolean isHorizontalScrollChangedWhileDragging() {
        return downRect == null || upRect == null || downRect.top != upRect.top;
    }
}
