package kaufland.com.swipelibrary;

/**
 * Created by sbra0902 on 17.03.17.
 */

public class SwipeResult {

    private int settleX;

    private Runnable notifyListener;

    public SwipeResult(int settleX, Runnable notifyListener) {
        this.settleX = settleX;
        this.notifyListener = notifyListener;
    }

    public SwipeResult(int settleX) {
        this.settleX = settleX;
    }

    public int getSettleX() {
        return settleX;
    }

    public Runnable getNotifyListener() {
        return notifyListener;
    }
}
