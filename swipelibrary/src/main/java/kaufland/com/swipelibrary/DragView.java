package kaufland.com.swipelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.androidannotations.annotations.EViewGroup;

import static kaufland.com.swipelibrary.SwipeLayout.BOTTOM_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.TOP_DRAG_VIEW;

@EViewGroup
class DragView extends LinearLayout implements SwipeableView {

    private int mViewPosition;

    private int mDragDistance;

    private int mLowerBound;

    private int mUpperBound;

    private int mIntermmediateDistance;

    private int mChildIndex;

    private int[] mDistances;

    private boolean mIsFullyOpened;

    private boolean mDraggable = true;

    private boolean mIsInitialized;

    private int mInitialXPos;

    private int mInitialYPos;

    DragView(Context context) {
        super(context);

    }

    DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragView, 0, 0);
        try{
            mViewPosition = typedArray.getInt(R.styleable.DragView_position, 0);
        } finally {
            typedArray.recycle();
        }
    }

    DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragView, 0, 0);
        try{
            mViewPosition = typedArray.getInt(R.styleable.DragView_position, 0);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void moveView(float offset) {
        int distance = (int) calculatePadding(offset);
        setTranslationX(distance);
    }

    @Override
    public void initializePosition(Rect surfaceRect, SwipeViewLayouter.DragDirection orientation) {
        switch (mViewPosition) {
            case LEFT_DRAG_VIEW:
                mDragDistance = getRight();
                mInitialXPos = surfaceRect.left - getWidth();
                mInitialYPos = surfaceRect.top;
                mIsInitialized = true;
                checkChildrenDistance();
                moveToInitial();
                break;

            case RIGHT_DRAG_VIEW:
                mDragDistance = getWidth();
                mUpperBound = getRight();
                mLowerBound = getLeft();
                mInitialXPos = surfaceRect.right + getWidth();
                mInitialYPos = surfaceRect.top;
                mIsInitialized = true;
                checkChildrenDistance();
                moveToInitial();
                break;

            case TOP_DRAG_VIEW:
                mDragDistance = getBottom();
                setTranslationY(-mDragDistance);
                mIsInitialized = true;
                checkChildrenDistance();
                moveToInitial();
                break;

            case BOTTOM_DRAG_VIEW:
                mDragDistance = getTop();
                setTranslationY(mDragDistance);
                mIsInitialized = true;
                checkChildrenDistance();
                moveToInitial();
                break;

            default:
                mIsInitialized = false;
        }

    }

    @Override
    public void moveToInitial() {
        setTranslationX(mInitialXPos);
        setTranslationY(mInitialYPos);
    }



    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        switch (state) {
            case LEFT_OPEN:

                if (mViewPosition == LEFT_DRAG_VIEW) {
                    view.setSurfaceViewOffsetX(getDragDistance());
                    view.setSurfaceViewOffsetY(0);
                    moveView(getDragDistance());
                }

                if (mViewPosition == RIGHT_DRAG_VIEW) {
                    moveToInitial();
                }
                break;
            case RIGHT_OPEN:

                if (mViewPosition == RIGHT_DRAG_VIEW) {
                    view.setSurfaceViewOffsetX(-getDragDistance());
                    view.setSurfaceViewOffsetY(0);
                    moveView(getDragDistance());
                }

                if (mViewPosition == LEFT_DRAG_VIEW) {
                    moveToInitial();
                }
                break;
            case TOP_OPEN:
                //TODO Implementation
                break;
            case BOTTOM_OPEN:
                //TODO Implementation
                break;
            default:
                view.setSurfaceViewOffsetX(0);
                view.setSurfaceViewOffsetY(0);
                if (mViewPosition == RIGHT_DRAG_VIEW) {
                    moveToInitial();
                }
                if (mViewPosition == LEFT_DRAG_VIEW) {
                    moveToInitial();
                }
                break;
        }
    }

    @Override
    public void onSwipe() {
        mIntermmediateDistance += getNextChildDistance();
    }

    @Override
    public void onClose() {
        mChildIndex = 0;
        mIntermmediateDistance = getNextChildDistance();
        mIsFullyOpened = false;
    }

    @Override
    public void onFullSwipe() {
        mIntermmediateDistance = mDragDistance;
        mChildIndex = mDistances != null ? mDistances.length - 1 : 0;
        mIsFullyOpened = true;
    }

    int getDragDistance() {
        return mDragDistance;
    }

    int getIntermmediateDistance() {
        return mIntermmediateDistance;
    }

    void addViewToDrag(View child) {
        this.addView(child);
        checkChildrenDistance();
    }

    private void checkChildrenDistance() {
        mChildIndex = 0;
        mDistances = new int[this.getChildCount()];

        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            int childDistance;
            if (mViewPosition == LEFT_DRAG_VIEW || mViewPosition == RIGHT_DRAG_VIEW) {
                childDistance = child.getWidth();
            } else {
                childDistance = getHeight();
            }
            mDistances[i] = childDistance;
        }

        mIntermmediateDistance = getNextChildDistance();
    }

    private int getNextChildDistance() {
        int nextDistance = 0;

        if (mDistances == null) {
            return 0;
        }

        if (mDistances.length > 0 && mChildIndex < mDistances.length) {
            nextDistance = mDistances[mChildIndex];
        }

        mChildIndex++;
        return nextDistance;
    }

    private float calculatePadding(float offset) {

        float calculatedDistance = 0;

        if (mViewPosition == LEFT_DRAG_VIEW) {
            if (offset > 0) {
                calculatedDistance = -mDragDistance + offset > 0 ? 0 : -mDragDistance + offset;
            } else {
                calculatedDistance = offset > -mDragDistance ? -mDragDistance : offset;
            }
        } else if (mViewPosition == RIGHT_DRAG_VIEW) {
            if (offset < 0) {
                float distance = mDragDistance - Math.abs(offset);
                calculatedDistance = distance < 0 ? 0 : distance;
            } else {
                calculatedDistance = mLowerBound + offset > mDragDistance ? mDragDistance : mLowerBound + offset;
            }
        }

        return calculatedDistance;
    }

    boolean isAllChildrenVisible() {
        return mChildIndex > mDistances.length;
    }

    boolean isDragFullyOpened() {
        return mIsFullyOpened;
    }

    void setDraggable(boolean canDrag) {
        mDraggable = canDrag;
    }

    boolean isDraggable() {
        return mDraggable;
    }

    boolean isInitialized() {
        return mIsInitialized;
    }

    public int getViewPosition() {
        return mViewPosition;
    }
}
