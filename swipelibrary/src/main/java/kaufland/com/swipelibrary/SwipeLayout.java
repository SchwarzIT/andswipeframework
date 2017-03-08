package kaufland.com.swipelibrary;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import static kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.LEFT_OPEN;

@EViewGroup
public class SwipeLayout extends LinearLayout {

    private static final int SWIPE_CLOSING_POINT = 0;

    public static final double AUTO_OPEN_SPEED_TRESHOLD = 800.0;
    public static final double LEFT_FULL_AUTO_OPEN_TRESHOLD = AUTO_OPEN_SPEED_TRESHOLD * 2;
    public static final double RIGHT_FULL_AUTO_OPEN_TRESHOLD = -LEFT_FULL_AUTO_OPEN_TRESHOLD;

    public static final int LEFT_DRAG_VIEW = 1;
    public static final int RIGHT_DRAG_VIEW = 2;
    public static final int TOP_DRAG_VIEW = 3;
    public static final int BOTTOM_DRAG_VIEW = 4;

    private static final String TAG = SwipeLayout.class.getSimpleName();


    @Bean
    protected SwipeState mSwipeState;

    @Bean
    protected DragRange mDragRange;

    @Bean
    protected SwipeViewLayouter mViewLayouter;

    private ViewDragHelper mDragHelper;

    private SwipeListener mSwipeListener;

    private boolean mIsDragging;

    private boolean mDragAllowed;

    private boolean mSwipeEnabled = true;

    private boolean mShouldSettle;

    private float mDownX;

    private float mDownY;

    private float mSwipeSettlingPoint;

    private float mXvelocity;

    private float mYvelocity;

    private int mDragHelperState;

    private float mDragHelperTouchSlop;

    private Rect mSurfaceRectHit;

    public enum DragDirection {
        HORIZONTAL, VERTICAL, NONE
    }

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
                mDownX = ev.getX();
                mDownY = ev.getY();
                mDragHelper.processTouchEvent(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();

                boolean canSwipe = SwipeUtil.canSwipe(mDownX, mDownY, moveX, moveY, mSwipeState.getState(), getOrientation(), mViewLayouter);

                if (!mViewLayouter.isDirectionSwipeable(mDownX, moveX, mViewLayouter)) {
                    return false;
                }

                mDragAllowed = canSwipe;
                mIsDragging = true;

                if (mIsDragging && mDragAllowed) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mDragHelper.processTouchEvent(ev);
                }

                break;

            case MotionEvent.ACTION_UP:
                float diffX = Math.abs(mDownX - ev.getX());
                float diffY = Math.abs(mDownY - ev.getY());
                mDragRange.setDifx(mDownX - ev.getX());
                mDragRange.setDify(mDownY - ev.getY());
                float distance = getOrientation() == HORIZONTAL ? diffX : diffY;
                boolean clicked = mDragHelperTouchSlop > distance;

                mIsDragging = false;

                if (!mViewLayouter.isDirectionSwipeable(mDownX, ev.getX(), mViewLayouter)) {
                    mDragHelper.cancel();
                }

                if (ev.getX() < 0 || ev.getY() < 0 || ev.getX() > getMeasuredWidth() || ev.getY() > getMeasuredHeight()) {

                    smoothSlideTo(SWIPE_CLOSING_POINT);
                    mSwipeState.setState(CLOSED);
                    return false;
                }

                if (!clicked && mDragAllowed) {
                    mDragHelper.processTouchEvent(ev);
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                mIsDragging = false;
                float lastX = ev.getX();
                float lastY = ev.getY();

                boolean swipeable = SwipeUtil.canSwipe(mDownX, mDownY, lastX, lastY, mSwipeState.getState(), getOrientation(), mViewLayouter);
                if (swipeable) {
                    mDragHelper.processTouchEvent(ev);
                } else {
                    mDragHelper.cancel();
                }
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
                if (!isDragViewOpened()) {
                    return false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                mDragHelper.processTouchEvent(ev);

                float point = getOrientation() == HORIZONTAL ? x : y;
                float oldPoint = getOrientation() == HORIZONTAL ? mDownX : mDownY;
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
                float distance = getOrientation() == HORIZONTAL ? diffX : diffY;

                boolean clicked = mDragHelperTouchSlop > distance;
                if (!clicked) {
                    mDragHelper.processTouchEvent(ev);
                } else {
                    mDragHelper.abort();
                }
                break;
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }


    public void smoothSlideTo(float slideOffset) {

        int leftOffset = getOrientation() == HORIZONTAL ? (int) slideOffset : 0;
        int topOffset = getOrientation() == VERTICAL ? (int) slideOffset : 0;

        if (mDragHelper.smoothSlideViewTo(mViewLayouter.getSurfaceView(), leftOffset, topOffset)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();



        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            mViewLayouter.getSurfaceView().setSurfaceViewOffsetX(mSwipeState.getState() == CLOSED ? 0 : mViewLayouter.getSurfaceView().getLeft());
            mViewLayouter.getSurfaceView().setSurfaceViewOffsetY(mSwipeState.getState() == CLOSED ? 0 : mViewLayouter.getSurfaceView().getTop());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mViewLayouter.init(this);
        if (mRestoreOnDraw) {
            restoreOnDraw();
            mRestoreOnDraw = false;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if(!mViewLayouter.isInitilized()){
            mViewLayouter.init(this);
            mSurfaceRectHit = new Rect();
            mViewLayouter.getSurfaceView().getDrawingRect(mSurfaceRectHit);
          //  super.onLayout(true, mSurfaceRectHit.left, mSurfaceRectHit.top, mSurfaceRectHit.right, mSurfaceRectHit.bottom);
        }else{
            //super.onLayout(changed, mSurfaceRectHit.left, mSurfaceRectHit.top, mSurfaceRectHit.right, mSurfaceRectHit.bottom);
        }

        super.onLayout(changed, left, top, right, bottom);


        if (mSwipeState.getState() != CLOSED) {
            mViewLayouter.getSurfaceView().offsetLeftAndRight(mViewLayouter.getSurfaceView().getSurfaceViewOffsetX());
            mViewLayouter.getSurfaceView().offsetTopAndBottom(mViewLayouter.getSurfaceView().getSurfaceViewOffsetY());
        }
    }

    private class SwipeDragViewHelper extends ViewDragHelper.Callback {

        private ViewGroup parent;

        SwipeDragViewHelper(ViewGroup parent) {
            this.parent = parent;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child.getId() == mViewLayouter.getSurfaceView().getId();
        }

        @Override
        public void onViewDragStateChanged(int state) {

            if (!mSwipeEnabled || mDragHelperState == state) {
                return;
            }

            mDragHelperState = state;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (getOrientation() == HORIZONTAL) {

                mDragRange.setDraggingBorder(left);

                mViewLayouter.moveView(getOrientation(), left);

                mDragRange.setHorizontalRange(checkDragDistance(left, top));


            } else if (getOrientation() == VERTICAL) {
                throw new UnsupportedOperationException("VerticalSwipeNotImplemented");
            }
        }

        @Override
        public void onViewReleased(View releasedChild, final float xvel, float yvel) {

            if (getOrientation() == VERTICAL) {
                throw new UnsupportedOperationException("VerticalSwipeNotImplemented");
            } else if (getOrientation() == HORIZONTAL) {
                mDragRange.setCheckRange(mDragRange.getHorizontalRange());

                mSwipeState.setSettled(SwipeUtil.determineSwipeSettleStateHorizontal(xvel, mDragRange));

                final int settleDestX = SwipeUtil.determineSwipeHorizontalState(xvel, mSwipeState, mDragRange, mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW), mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW));

                if (mDragHelper.settleCapturedViewAt(settleDestX, 0)) {
                    ViewCompat.postInvalidateOnAnimation(parent);
                }

                mShouldSettle = true;
                mSwipeSettlingPoint = settleDestX;
                mXvelocity = xvel;


                if (mShouldSettle) {
                    notifySwipeStateChangedOverX(mSwipeSettlingPoint, mXvelocity);
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mSwipeState.getState() == LEFT_OPEN ||
                    mSwipeState.getState() == SwipeState.DragViewState.RIGHT_OPEN ||
                    getOrientation() == HORIZONTAL) {
                return 0;
            }

            int draggingRange;
            if (mSwipeState.getState() == SwipeState.DragViewState.TOP_OPEN) {
                draggingRange = top > 0 ? top : 0;
            } else if (mSwipeState.getState() == SwipeState.DragViewState.BOTTOM_OPEN) {
                draggingRange = top > 0 ? 0 : top;
            } else {
                draggingRange = mDragAllowed ? determineDraggingRange(top) : 0;
            }

            return mDragAllowed ? draggingRange : 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mSwipeState.getState() == SwipeState.DragViewState.TOP_OPEN ||
                    mSwipeState.getState() == SwipeState.DragViewState.BOTTOM_OPEN ||
                    getOrientation() == VERTICAL) {
                return 0;
            }

            int draggingRange;
            if (mSwipeState.getState() == LEFT_OPEN) {
                draggingRange = left > 0 ? left : 0;
            } else if (mSwipeState.getState() == SwipeState.DragViewState.RIGHT_OPEN) {
                draggingRange = left > 0 ? 0 : left;
            } else {
                draggingRange = mDragAllowed ? determineDraggingRange(left) : 0;
            }

            return draggingRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragRange.getHorizontalRange();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange.getVerticalRange();
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (getOrientation() == HORIZONTAL) {
            int horizontal = (int) (w * 0.33);
            mDragRange.setHorizontalRange(horizontal);
            mDragRange.setHorizontalMaxRange(w);
            mDragRange.setVerticalRange(0);
        } else if (getOrientation() == VERTICAL) {
            int verticalRange = (int) (h * 0.33);
            mDragRange.setHorizontalRange(0);
            mDragRange.setVerticalMaxRange(h);
            mDragRange.setVerticalRange(verticalRange);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private boolean mRestoreOnDraw;

    public void markForRestoreOnDraw(SwipeState.DragViewState swipeState) {
        mSwipeState.setState(swipeState);
        mRestoreOnDraw = true;
    }

    private void restoreOnDraw() {

        mViewLayouter.restoreState(mSwipeState.getState());
    }

    public void closeSwipeNoAnimation() {
        mSwipeState.setState(CLOSED);
        mViewLayouter.restoreState(mSwipeState.getState());
    }

    public void openSwipe(final int position, boolean notifyCallback) {
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

    private void onStopDraggingToClosed() {
        mViewLayouter.onStopDraggingToClosed(getOrientation());
    }

    private int checkDragDistance(int left, int top) {
        int distance = 0;
//        int leftDragDistance = mLeftDragView != null ? mLeftDragView.getDragDistance() : 0;
//        int rightDragDistance = mRightDragView != null ? mRightDragView.getDragDistance() : 0;
//
//        if (mSwipeState.getState() == LEFT_OPEN) {
//            distance = leftDragDistance;
//        } else if (mSwipeState.getState() == SwipeState.DragViewState.RIGHT_OPEN) {
//            distance = rightDragDistance;
//        } else if (mSwipeState.getState() == SwipeState.DragViewState.TOP_OPEN) {
//            distance = mTopDragView.getDragDistance();
//        } else if (mSwipeState.getState() == SwipeState.DragViewState.BOTTOM_OPEN) {
//            distance = mBottomDragView.getDragDistance();
//        } else if (mSwipeState.getState() == CLOSED) {
//            if (mDragDirection == DragDirection.HORIZONTAL) {
//                distance = left >= 0 ? leftDragDistance : rightDragDistance;
//            } else if (mDragDirection == DragDirection.VERTICAL) {
//                distance = top >= 0 ? mTopDragView.getDragDistance() : mBottomDragView.getDragDistance();
//            }
//        }

        return distance;
    }


    private void notifySwipeOpened() {
        mSwipeListener.onSwipeOpened(mSwipeState.getState(), true);

    }

    private void notifySwipeStateChangedOverX(float settleDestX, float xVelocity) {
        final boolean fullSwipe = Math.abs(settleDestX) == mDragRange.getHorizontalRange();

//        if (fullSwipe) {
//            boolean canBounce = SwipeUtil.canBounce(xVelocity, mDragDirection, mSwipeState);
//
//            if (mSwipeSettlingPoint == settleDestX && !mActiveDrag.isDragFullyOpened()) {
//                if (mSwipeListener != null) {
//                    mSwipeListener.onSwipeOpened(mSwipeState.getState(), fullSwipe);
//
//                }
//                mShouldSettle = false;
//            } else if (mActiveDrag.isDragFullyOpened() && canBounce) {
//                if (mSwipeListener != null) {
//                    mSwipeListener.onBounce(mSwipeState.getState());
//                }
//                mShouldSettle = false;
//            }
//
//            mActiveDrag.onFullSwipe();
//        } else if (settleDestX == 0 || settleDestX == this.getRight()) {
//            onStopDraggingToClosed();
//            if (mSwipeListener != null) {
//                mSwipeListener.onSwipeClosed(mSwipeState.getState());
//            }
//            mShouldSettle = false;
//        } else if (mActiveDrag.getIntermmediateDistance() == settleDestX) {
//            mActiveDrag.onSwipe();
//
//            if (mSwipeListener != null) {
//                mSwipeListener.onSwipeOpened(mSwipeState.getState(), mActiveDrag.isAllChildrenVisible());
//            }
//
//            mShouldSettle = false;
//        }
    }

    private void notifySwipeStateChangedOverY(float settleDestY, float yVelocity) {
        boolean fullSwipe = Math.abs(settleDestY) == mDragRange.getVerticalRange();

//        if (fullSwipe) {
//            if (mActiveDrag.isDragFullyOpened() && SwipeUtil.canBounce(yVelocity, mDragDirection, mSwipeState)) {
//                if (mSwipeListener != null) {
//                    mSwipeListener.onBounce(mSwipeState.getState());
//                }
//            }
//            mActiveDrag.onFullSwipe();
//        } else if (settleDestY == 0 || settleDestY == this.getBottom()) {
//            onStopDraggingToClosed();
//            if (mSwipeListener != null) {
//                mSwipeListener.onSwipeClosed(mSwipeState.getState());
//            }
//        } else {
//            mActiveDrag.onSwipe();
//            if (mSwipeListener != null) {
//                mSwipeListener.onSwipeOpened(mSwipeState.getState(), mActiveDrag.isAllChildrenVisible());
//            }
//        }
    }

    private int determineDraggingRange(int distance) {
        return mViewLayouter.determineDraggingRange(getOrientation(), distance);
    }

    private void initSwipe() {

        mDragHelper = ViewDragHelper.create(this, 1.0f, new SwipeDragViewHelper(this));
        mDragHelperTouchSlop = mDragHelper.getTouchSlop() * 2;
    }

    public boolean isDragViewOpened() {
        int swipeLeft = mViewLayouter.getSurfaceView().getLeft();
        return swipeLeft != 0;
    }

}
