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

    private KDragViewHelper mDragHelper;

    private SwipeListener mSwipeListener;

    private boolean mIsDragging;

    private boolean mDragAllowed;

    private boolean mSwipeEnabled = true;

    private float mDownX;

    private float mDownY;

    private float mXvelocity;

    private float mYvelocity;

    private float mDragHelperTouchSlop;

    private Rect mSurfaceRectHit;

    private static final int SWIPE_DIRECTION_LEFT = 1;

    private static final int SWIPE_DIRECTION_RIGHT = 2;


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
                mDownX = ev.getX();
                mDownY = ev.getY();
                mDragHelper.processTouchEvent(ev);
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();

                boolean canSwipe = SwipeUtil.canSwipe(mDownX, mDownY, moveX, moveY, mSwipeState.getState(), mViewLayouter);

//                if (!mViewLayouter.isDirectionSwipeable(mDownX, moveX, mViewLayouter)) {
//                    return false;
//                }

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
                float distance = mViewLayouter.getDragDirection() == HORIZONTAL ? diffX : diffY;
                boolean clicked = mDragHelperTouchSlop > distance;

                mIsDragging = false;

                if (!mViewLayouter.isDirectionSwipeable(mDownX, ev.getX(), mViewLayouter)) {
                    mDragHelper.cancel();
                }

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

                float point = mViewLayouter.getDragDirection() == HORIZONTAL ? x : y;
                float oldPoint = mViewLayouter.getDragDirection() == HORIZONTAL ? mDownX : mDownY;
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
                float distance = mViewLayouter.getDragDirection() == HORIZONTAL ? diffX : diffY;

                mDragHelper.processTouchEvent(ev);
                break;
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }


    public void smoothSlideTo(float slideOffset) {

        int leftOffset = mViewLayouter.getDragDirection() == HORIZONTAL ? (int) slideOffset : 0;
        int topOffset = mViewLayouter.getDragDirection() == VERTICAL ? (int) slideOffset : 0;

        if (mDragHelper.smoothSlideViewTo(mViewLayouter.getSurfaceView(), leftOffset, topOffset)) {
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

        if (!mViewLayouter.isInitilized()) {
            mViewLayouter.init(this);
            mSurfaceRectHit = new Rect(0, 0, mViewLayouter.getSurfaceView().getMeasuredWidth(), mViewLayouter.getSurfaceView().getMeasuredHeight());
            mViewLayouter.initInitialPosition(mSurfaceRectHit);
            initDistances();
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

            return child.getId() == mViewLayouter.getSurfaceView().getId() || child.getId() == mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW).getId() || child.getId() == mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW).getId();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {

                mViewLayouter.moveView(changedView, left);

                mDragRange.setDraggingBorder((int) mViewLayouter.getSurfaceView().getX());
                mDragRange.setHorizontalRange(checkDragDistance((int) mViewLayouter.getSurfaceView().getX(), (int) mViewLayouter.getSurfaceView().getY()));


            } else if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                throw new InvalidParameterException("VerticalSwipeNotImplemented");
            }
        }

        @Override
        public void onViewReleased(View releasedChild, final float xvel, float yvel) {

            if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                throw new InvalidParameterException("VerticalSwipeNotImplemented");
            } else if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {
                mDragRange.setCheckRange(mDragRange.getHorizontalRange());

                final SwipeResult swipeResult = determineSwipeHorizontalState(xvel, mSwipeState, mDragRange, mViewLayouter, releasedChild);

                if (mDragHelper.smoothSlideViewTo(releasedChild, swipeResult.getSettleX(), 0)) {
                    ViewCompat.postInvalidateOnAnimation(parent);

                }

                mViewLayouter.requestLayout();
                mXvelocity = xvel;

                if (swipeResult.getNotifyListener() != null && mSwipeListener != null) {
                    swipeResult.getNotifyListener().run();
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mSwipeState.getState() == LEFT_OPEN ||
                    mSwipeState.getState() == RIGHT_OPEN ||
                    mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {
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
                    mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
                return 0;
            }

            DragView leftDragView = mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW);

            DragView rightDragView = mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW);

            if (leftDragView != null && child.equals(leftDragView)) {


                if (left > 0 && !leftDragView.isBouncePossible()) {
                    return 0;
                }

                return left;
            }

            if (rightDragView != null && child.equals(rightDragView)) {

                if (left < (mViewLayouter.getSurfaceView().getWidth() - rightDragView.getDragDistance()) && !rightDragView.isBouncePossible()) {
                   return mViewLayouter.getSurfaceView().getWidth() - rightDragView.getDragDistance();
                }

                return left;
            }


            boolean isOutsideRightRangeAndBounceNotPossible = left < -rightDragView.getDragDistance() && !rightDragView.isBouncePossible();
            boolean isOutsideLeftRangeAndBounceNotPossible = left > leftDragView.getDragDistance() && !leftDragView.isBouncePossible();


            if (isOutsideLeftRangeAndBounceNotPossible) {
                return leftDragView.getDragDistance();
            }

            if(isOutsideRightRangeAndBounceNotPossible){
                return rightDragView.getDragDistance();
            }


            return left;

//            int draggingRange;
//            if (mSwipeState.getState() == LEFT_OPEN || child.equals(mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW))) {
//                draggingRange = left >  ? left : 0;
//            } else if (mSwipeState.getState() == RIGHT_OPEN || child.equals(mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW))) {
//                draggingRange = left > 0 ? 0 : left;
//            } else {
//                draggingRange = mDragAllowed ? determineDraggingRange(left) : 0;
//            }

//            Log.e("test", String.valueOf(draggingRange));
//
//            return draggingRange;
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

    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeState swipeState, DragRange range, SwipeViewLayouter layouter, View releasedChild) {

        int swipeDirection = range.getDifx() < 0 ? SWIPE_DIRECTION_RIGHT : SWIPE_DIRECTION_LEFT;

        final DragView leftDragView = layouter.getDragViewByPosition(LEFT_DRAG_VIEW);
        final DragView rightDragView = layouter.getDragViewByPosition(RIGHT_DRAG_VIEW);


        if (releasedChild.equals(leftDragView)) {

            if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                swipeState.setState(SwipeState.DragViewState.CLOSED);
                return new SwipeResult(-leftDragView.getWidth(), new Runnable() {
                    @Override
                    public void run() {
                        mSwipeListener.onSwipeClosed(CLOSED);
                    }
                });
            }

        } else if (releasedChild.equals(rightDragView)) {

            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                swipeState.setState(SwipeState.DragViewState.CLOSED);
                return new SwipeResult(layouter.getSurfaceView().getWidth(), new Runnable() {
                    @Override
                    public void run() {
                        mSwipeListener.onSwipeClosed(CLOSED);
                    }
                });
            }

        } else {
            if (swipeState.getState() == SwipeState.DragViewState.CLOSED) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                    if (!leftDragView.isDraggable()) {
                        return new SwipeResult(0);
                    }

                    swipeState.setState(SwipeState.DragViewState.LEFT_OPEN);
                    if (velocity > LEFT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (leftDragView.getDragDistance() / 2)) {
                        return new SwipeResult(range.getHorizontalRange(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(LEFT_OPEN, true);
                            }
                        });
                    } else {
                        return new SwipeResult(leftDragView.getIntermmediateDistance(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(LEFT_OPEN, leftDragView.getIntermmediateDistance() == leftDragView.getDragDistance());
                            }
                        });
                    }

                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    if (!rightDragView.isDraggable()) {
                        return new SwipeResult(0);
                    }

                    swipeState.setState(RIGHT_OPEN);

                    if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (rightDragView.getDragDistance() / 2)) {
                        return new SwipeResult(-range.getHorizontalRange(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                            }
                        });
                    } else {
                        return new SwipeResult(-rightDragView.getIntermmediateDistance(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(RIGHT_OPEN, rightDragView.getIntermmediateDistance() == rightDragView.getDragDistance());
                            }
                        });
                    }
                }
            } else if (swipeState.getState() == SwipeState.DragViewState.LEFT_OPEN) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                    swipeState.setState(LEFT_OPEN);

                    return new SwipeResult(leftDragView.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onBounce(LEFT_OPEN);
                        }
                    });

                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    swipeState.setState(CLOSED);
                    return new SwipeResult(range.getHorizontalMinRange(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeClosed(CLOSED);
                        }
                    });
                }
            } else if (swipeState.getState() == RIGHT_OPEN) {
                if (swipeDirection == SWIPE_DIRECTION_RIGHT) {

                    swipeState.setState(SwipeState.DragViewState.CLOSED);
                    return new SwipeResult(range.getHorizontalMinRange(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeClosed(CLOSED);
                        }
                    });

                } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                    swipeState.setState(RIGHT_OPEN);
                    if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(range.getDifx()) > (rightDragView.getDragDistance() / 2)) {
                        return new SwipeResult(-range.getHorizontalRange(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                            }
                        });
                    } else {
                        return new SwipeResult(-rightDragView.getIntermmediateDistance(), new Runnable() {
                            @Override
                            public void run() {
                                mSwipeListener.onSwipeOpened(RIGHT_OPEN, rightDragView.getIntermmediateDistance() == rightDragView.getDragDistance());
                            }
                        });
                    }
                }
            }
        }


        return new SwipeResult(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (mViewLayouter.getDragDirection() == HORIZONTAL) {
            int horizontal = (int) (w * 0.33);
            mDragRange.setHorizontalRange(horizontal);
            mDragRange.setHorizontalMaxRange(w);
            mDragRange.setVerticalRange(0);
        } else if (mViewLayouter.getDragDirection() == VERTICAL) {
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
//
//        DragView mDragViewByPosition = mViewLayouter.getDragViewByPosition(position);
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

    private int checkDragDistance(int left, int top) {
        int distance = 0;
        int leftDragDistance = mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW) != null ? mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW).getDragDistance() : 0;
        int rightDragDistance = mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW) != null ? mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW).getDragDistance() : 0;

        if (mSwipeState.getState() == LEFT_OPEN) {
            distance = leftDragDistance;
        } else if (mSwipeState.getState() == RIGHT_OPEN) {
            distance = rightDragDistance;
        } else if (mSwipeState.getState() == CLOSED) {
            if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {
                distance = left >= 0 ? leftDragDistance : rightDragDistance;
            }
//            else if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
//                distance = top >= 0 ? mTopDragView.getDragDistance() : mBottomDragView.getDragDistance();
//            }
        }

        return distance;
    }


    private void notifySwipeOpened() {
        mSwipeListener.onSwipeOpened(mSwipeState.getState(), true);

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

    private void initDistances() {
        if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.HORIZONTAL) {
            int leftDistance = 0;
            int rightDistance = 0;

            if (mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW) != null && mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW).getWidth() > 0) {
                leftDistance = mViewLayouter.getDragViewByPosition(LEFT_DRAG_VIEW).getIntermmediateDistance();
            }

            if (mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW) != null && mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW).getWidth() > 0) {
                rightDistance = mViewLayouter.getDragViewByPosition(RIGHT_DRAG_VIEW).getWidth();
            }

            mDragRange.setHorizontalRange(leftDistance < rightDistance ? leftDistance : rightDistance);
            mDragRange.setHorizontalMaxRange(mViewLayouter.getSurfaceView().getWidth());
            mDragRange.setVerticalRange(0);
        } else if (mViewLayouter.getDragDirection() == SwipeViewLayouter.DragDirection.VERTICAL) {
            throw new InvalidParameterException("VerticalSwipeNotImplemented");
        }
    }

    private int determineDraggingRange(int distance) {
        return mViewLayouter.determineDraggingRange(distance);
    }

    private void initSwipe() {

        mDragHelper = KDragViewHelper.create(this, 1.0f, new SwipeDragViewHelper(this));
        mDragHelperTouchSlop = mDragHelper.getTouchSlop() * 2;
    }

}
