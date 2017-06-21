package kaufland.com.swipelibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;

import java.security.InvalidParameterException;

import static kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.LEFT_OPEN;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.RIGHT_OPEN;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.HORIZONTAL;
import static kaufland.com.swipelibrary.SwipeViewLayouter.DragDirection.VERTICAL;

@EViewGroup
public class SwipeLayout extends FrameLayout {

    private static final int SWIPE_CLOSING_POINT = 0;

    public static final int LEFT_DRAG_VIEW = 1;
    public static final int RIGHT_DRAG_VIEW = 2;
    public static final int TOP_DRAG_VIEW = 3;
    public static final int BOTTOM_DRAG_VIEW = 4;
    public static final int SURFACE_VIEW = 5;

    private static final String TAG = SwipeLayout.class.getSimpleName();

    @Bean
    protected SwipeState mSwipeState;

    @Bean
    protected SwipeDirectionDetector mSwipeDirectionDetector;

    @Bean
    protected DraggingProxy mDraggingProxy;

    private KDragViewHelper mDragHelper;

    private SwipeListener mSwipeListener;

    private boolean mSwipeEnabled = true;

    private float mDragHelperTouchSlop;

    public interface SwipeListener {
        void onSwipeOpened(SwipeState.DragViewState openedDragView, boolean isFullSwipe);

        void onSwipeClosed(SwipeState.DragViewState dragViewState);

        void onBounce(SwipeState.DragViewState dragViewState);
    }

    public SwipeLayout(Context context) {
        super(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSwipe();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSwipe();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        if (!mSwipeEnabled) {
            return false;
        }

        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mDragHelper.abort();
                mSwipeDirectionDetector.onActionDown(ev.getX(), ev.getY(), this);
                mDragHelper.processTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                mSwipeDirectionDetector.onAction(ev.getX(), ev.getY());


                boolean isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector.getDifX());

                if (!isClick) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    try {
                        mDragHelper.processTouchEvent(ev);
                    } catch (IllegalArgumentException e) {
                        // https://code.google.com/p/android/issues/detail?id=64553
                        mSwipeDirectionDetector.onAction(mSwipeDirectionDetector.getXDown(), mSwipeDirectionDetector.getYDown());
                        ev.setAction(MotionEvent.ACTION_UP);
                        mDragHelper.processTouchEvent(ev);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:

                mSwipeDirectionDetector.onActionUp(ev.getX(), ev.getY(), this);


                if (ev.getX() < 0 || ev.getY() < 0 || ev.getX() > getMeasuredWidth() || ev.getY() > getMeasuredHeight()) {


                    ev.setAction(MotionEvent.ACTION_UP);
                    mDragHelper.processTouchEvent(ev);
                    return true;
                }


                mDragHelper.processTouchEvent(ev);


                break;

            case MotionEvent.ACTION_CANCEL:

                mDragHelper.processTouchEvent(ev);

                break;

            default:
                mDragHelper.processTouchEvent(ev);
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        if (!mSwipeEnabled) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mSwipeDirectionDetector.onActionDown(ev.getX(), ev.getY(), this);
                mDragHelper.abort();
                mDragHelper.processTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                mSwipeDirectionDetector.onAction(ev.getX(), ev.getY());

                boolean isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector.getDifX());
                if (!isClick) {
                    try {
                        mDragHelper.processTouchEvent(ev);
                    } catch (IllegalArgumentException e) {
                        // https://code.google.com/p/android/issues/detail?id=64553
                        mSwipeDirectionDetector.onAction(mSwipeDirectionDetector.getXDown(), mSwipeDirectionDetector.getYDown());
                        ev.setAction(MotionEvent.ACTION_UP);
                        mDragHelper.processTouchEvent(ev);
                    }
                }


                if (!isClick) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mDragHelper.processTouchEvent(ev);
                break;
        }

        return false;
    }


    public void smoothSlideTo(float slideOffset) {

        int leftOffset = mDraggingProxy.getDragDirection() == HORIZONTAL ? (int) slideOffset : 0;
        int topOffset = mDraggingProxy.getDragDirection() == VERTICAL ? (int) slideOffset : 0;

        if (mDragHelper.smoothSlideViewTo(mDraggingProxy.getSurfaceView(), leftOffset, topOffset)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();


        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        super.dispatchDraw(canvas);

        if (!mDraggingProxy.isInitilized()) {
            mDraggingProxy.init(this);
        }

        if (mRestoreOnDraw) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDraggingProxy.restoreState(mSwipeState.getState());
                    mRestoreOnDraw = false;
                }
            });

        }


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mDraggingProxy.restoreChildrenBound();
    }

    private class SwipeDragViewHelper extends ViewDragHelper.Callback {

        private ViewGroup parent;

        private int xBeforeDrag;

        SwipeDragViewHelper(ViewGroup parent) {
            this.parent = parent;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            xBeforeDrag = (int) child.getX();
            return mDraggingProxy.isCapturedViewDraggable(child);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if(state == 0){
                mDraggingProxy.captureChildrenBound();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {

                mDraggingProxy.moveView(changedView, left);

            } else if (mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                throw new InvalidParameterException("VerticalSwipeNotImplemented");
            }
        }

        @Override
        public void onViewReleased(View releasedChild, final float xvel, float yvel) {

            if (mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                throw new InvalidParameterException("VerticalSwipeNotImplemented");
            } else if (mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {

                SwipeResult swipeResult = new SwipeResult(xBeforeDrag);
                boolean isClick = mDragHelperTouchSlop > Math.abs(mSwipeDirectionDetector.getDifX());

                if (!mSwipeDirectionDetector.isHorizontalScrollChangedWhileDragging() && !isClick) {
                    swipeResult = mDraggingProxy.determineSwipeHorizontalState(xvel, mSwipeDirectionDetector, mSwipeState, mSwipeListener, releasedChild);
                }

                if (mDragHelper.smoothSlideViewTo(releasedChild, swipeResult.getSettleX(), 0)) {
                    ViewCompat.postInvalidateOnAnimation(parent);

                }

                mDraggingProxy.requestLayout();

                if (swipeResult.getNotifyListener() != null && mSwipeListener != null) {
                    swipeResult.getNotifyListener().run();
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mSwipeState.getState() == LEFT_OPEN ||
                    mSwipeState.getState() == RIGHT_OPEN ||
                    mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {
                return 0;
            }

            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mSwipeState.getState() == SwipeState.DragViewState.TOP_OPEN ||
                    mSwipeState.getState() == SwipeState.DragViewState.BOTTOM_OPEN ||
                    !mDraggingProxy.canSwipe(mSwipeDirectionDetector, mSwipeState.getState()) ||
                    mDraggingProxy.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                return 0;
            }

            return mDraggingProxy.clampViewPositionHorizontal(child, left);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 0;
        }

    }


    private boolean mRestoreOnDraw;

    public void markForRestoreOnDraw(SwipeState.DragViewState swipeState) {
        mSwipeState.setState(swipeState);
        mRestoreOnDraw = true;
        if (mDragHelper.getViewDragState() != KDragViewHelper.STATE_IDLE) {
            mDragHelper.abort();
        }
    }

    public void closeSwipeNoAnimation() {
        mSwipeState.setState(CLOSED);
        mDraggingProxy.restoreState(mSwipeState.getState());
    }

    public void openSwipe(final int position, boolean notifyCallback) {

        int mSurfaceOpenOffsetByDragView = mDraggingProxy.getSurfaceOpenOffsetByDragView(position);

        switch (position) {
            case LEFT_DRAG_VIEW:
                mSwipeState.setState(LEFT_OPEN);
                smoothSlideTo(mSurfaceOpenOffsetByDragView);
                break;

            case RIGHT_DRAG_VIEW:
                mSwipeState.setState(RIGHT_OPEN);
                smoothSlideTo(mSurfaceOpenOffsetByDragView);
                break;

            default:
                return;
        }


        if (mSwipeListener != null && notifyCallback) {
            notifySwipeOpened();
        }
    }

    public void openSwipe(final int position) {
        openSwipe(position, true);
    }

    public void closeSwipe() {
        switch (mSwipeState.getState()) {
            case LEFT_OPEN:
            case RIGHT_OPEN:
            case TOP_OPEN:
            case BOTTOM_OPEN:
                smoothSlideTo(SWIPE_CLOSING_POINT);
                mSwipeState.setState(CLOSED);
                break;
        }
    }

    public void setSwipeEnabled(boolean enabled) {
        mSwipeEnabled = enabled;
    }

    public boolean getSwipeEnabled() {
        return mSwipeEnabled;
    }

    public SwipeState getSwipeState() {
        return mSwipeState;
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }

    private void notifySwipeOpened() {
        mSwipeListener.onSwipeOpened(mSwipeState.getState(), true);

    }

    private void initSwipe() {

        mDragHelper = KDragViewHelper.create(this, 1.0f, new SwipeDragViewHelper(this));
        mDragHelperTouchSlop = mDragHelper.getTouchSlop() * 2;
    }

}
