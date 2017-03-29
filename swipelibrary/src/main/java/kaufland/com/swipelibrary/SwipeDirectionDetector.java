package kaufland.com.swipelibrary;

import org.androidannotations.annotations.EBean;

/**
 * Created by sbra0902 on 29.03.17.
 */
@EBean
public class SwipeDirectionDetector {

    private int xDown;

    private int xUp;

    public static final int SWIPE_DIRECTION_LEFT = 1;

    public static final int SWIPE_DIRECTION_RIGHT = 2;

    public void onActionDown(float x) {
        xDown = (int) x;
    }

    public void onActionUp(float x) {
        xUp = (int) x;
    }

    public int getDifX() {
        return xDown - xUp;
    }

    public int getSwipeDirection(){
      return   getDifX() < 0 ? SWIPE_DIRECTION_RIGHT : SWIPE_DIRECTION_LEFT;
    }


}
