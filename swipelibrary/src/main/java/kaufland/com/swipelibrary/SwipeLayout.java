package kaufland.com.swipelibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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

    private boolean mIsDragging;

    private boolean mDragAllowed;

    private boolean mSwipeEnabled = true;

    private float mDownX;

    private float mDownY;

    private float mDragHelperTouchSlop;

    private Rect mSurfaceRectHit;


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
                mSwipeDirectionDetector.onActionDown(ev.getX(), ev.getY());
                mDownX = ev.getX();
                mDownY = ev.getY();
                mDragHelper.processTouchEvent(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();

                boolean canSwipe = mDraggingProxy.canSwipe(mDownX, mDownY, moveX, moveY, mSwipeState.getState());

                mDragAllowed = canSwipe;
                mIsDragging = true;

                if (mIsDragging && mDragAllowed) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mDragHelper.processTouchEvent(ev);
                }

                break;

            case MotionEvent.ACTION_UP:

                mSwipeDirectionDetector.onActionUp(ev.getX(), ev.getY());
                mIsDragging = false;


                if (ev.getX() < 0 || ev.getY() < 0 || ev.getX() > getMeasuredWidth() || ev.getY() > getMeasuredHeight()) {


                    ev.setAction(MotionEvent.ACTION_UP);
                    mDragHelper.processTouchEvent(ev);
                    return false;
                }

                if (mDragAllowed) {
                    mDragHelper.processTouchEvent(ev);
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                mIsDragging = false;
                float lastX = ev.getX();
                float lastY = ev.getY();

//                boolean swipeable = SwipeUtil.canSwipe(mDownX, mDownY, lastX, lastY, mSwipeState.getState(), mViewLayouter);
//                if (swipeable) {
                mDragHelper.processTouchEvent(ev);
//                } else {
//                    mDragHelper.cancel();
//                }
                break;

            default:
                mDragHelper.processTouchEvent(ev);
        }

        return mDragAllowed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        if (!mSwipeEnabled) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragHelper.abort();
                mDragHelper.processTouchEvent(ev);
                mDownX = x;
                mDownY = y;
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                mDragHelper.processTouchEvent(ev);

                float point = mDraggingProxy.getDragDirection() == HORIZONTAL ? x : y;
                float oldPoint = mDraggingProxy.getDragDirection() == HORIZONTAL ? mDownX : mDownY;
                boolean isClick = mDragHelperTouchSlop > Math.abs(point - oldPoint);

                if (!mIsDragging && isClick) {
                    return false;
                }

                if (mIsDragging) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                float diffX = Math.abs(mDownX - ev.getX());
                float diffY = Math.abs(mDownY - ev.getY());

                mDragHelper.processTouchEvent(ev);
                break;
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }


    public void smoothSlideTo(float slideOffset) {
        //TODO Schaffeeee!!!
//        int leftOffset = mDraggingProxy.getDragDirection() == HORIZONTAL ? (int) slideOffset : 0;
//        int topOffset = mDraggingProxy.getDragDirection() == VERTICAL ? (int) slideOffset : 0;
//
//        if (mDragHelper.smoothSlideViewTo(mViewLayouter.getSurfaceView(), leftOffset, topOffset)) {
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
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
            restoreOnDraw();
            mRestoreOnDraw = false;
        }

    }

    private class SwipeDragViewHelper extends ViewDragHelper.Callback {

        private ViewGroup parent;

        SwipeDragViewHelper(ViewGroup parent) {
            this.parent = parent;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mDraggingProxy.isCapturedViewDraggable(child);
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

                final SwipeResult swipeResult = mDraggingProxy.determineSwipeHorizontalState(xvel, mSwipeDirectionDetector, mSwipeState, mSwipeListener, releasedChild);

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
    }

    private void restoreOnDraw() {

        mDraggingProxy.restoreState(mSwipeState.getState());
    }

    public void closeSwipeNoAnimation() {
        mSwipeState.setState(CLOSED);
        mDraggingProxy.restoreState(mSwipeState.getState());
    }

    public void openSwipe(final int position, boolean notifyCallback) {
//
//        DragView mDragViewByPosition = mViewLayouter.getDragViewEngineByPosition(position);
//        mDragViewByPosition.moveView(mDragViewByPosition.getDragDistance());
//
//        switch (position) {
//            case LEFT_DRAG_VIEW:
//                if (mLeftDragView != null) {
//                    smoothSlideTo(mLeftDragView.getDragDistance());
//                    mSwipeState.setState(LEFT_OPEN);
//                }
//                break;
//
//            case RIGHT_DRAG_VIEW:
//                if (mRightDragView != null) {
//                    smoothSlideTo(-mRightDragView.getDragDistance());
//                    mSwipeState.setState(SwipeState.DragViewState.RIGHT_OPEN);
//                }
//                break;
//
//            case TOP_DRAG_VIEW:
//                if (mTopDragView != null) {
//                    smoothSlideTo(mTopDragView.getDragDistance());
//                    mSwipeState.setState(SwipeState.DragViewState.TOP_OPEN);
//                }
//                break;
//
//            case BOTTOM_DRAG_VIEW:
//                if (mBottomDragView != null) {
//                    smoothSlideTo(-mBottomDragView.getDragDistance());
//                    mSwipeState.setState(SwipeState.DragViewState.BOTTOM_OPEN);
//                }
//                break;
//
//            default:
//                return;
//        }
//
//        if (mSwipeListener != null && notifyCallback) {
//            notifySwipeOpened();
//        }
        //TODO Schaffee!!!
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
