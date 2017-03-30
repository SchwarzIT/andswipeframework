package kaufland.com.swipelibrary;

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

    public void onActionDown(float x, float y) {
        xDown = (int) x;
    }

    public void onActionUp(float x, float y) {
        xUp = (int) x;
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


}
