package kaufland.com.swipelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.EViewGroup;

import static kaufland.com.swipelibrary.SwipeLayout.BOTTOM_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.LEFT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.RIGHT_DRAG_VIEW;
import static kaufland.com.swipelibrary.SwipeLayout.TOP_DRAG_VIEW;

@EViewGroup
class DragView extends LinearLayout implements SwipeableView {

    private int mSettlePointResourceId;

    private int mViewPosition;

    private int mDragDistance;

    private int mIntermmediateDistance;

    private boolean mDraggable = true;

    private boolean mIsInitialized;

    private int mInitialXPos;

    private int mInitialYPos;

    private boolean mBouncePossible;

    DragView(Context context) {
        super(context);

    }

    DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyleable(context, attrs);
    }

    DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyleable(context, attrs);
    }

    private void initStyleable(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragView, 0, 0);
        try{
            mViewPosition = typedArray.getInt(R.styleable.DragView_position, 0);
            mSettlePointResourceId = typedArray.getResourceId(R.styleable.DragView_settleView, -1);
            mBouncePossible = typedArray.getBoolean(R.styleable.DragView_bouncePossible, false);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {

        if(mViewPosition == LEFT_DRAG_VIEW){
            if(!this.equals(changedView)){
                setX(view.getX() - getWidth());
            }else {
                view.moveView(getX() + getWidth());
            }
        }else if(mViewPosition == RIGHT_DRAG_VIEW){
            if(!this.equals(changedView)){
                setX(view.getX() + getWidth());
            }else {
                view.moveView(getX() - view.getWidth());
            }
        }

    }

    @Override
    public void initializePosition(Rect surfaceRect, SwipeViewLayouter.DragDirection orientation) {
        switch (mViewPosition) {
            case LEFT_DRAG_VIEW:
                mDragDistance = getRight();
                mInitialXPos = surfaceRect.left - getWidth();
                mInitialYPos = surfaceRect.top;
                mIsInitialized = true;
                if(mSettlePointResourceId != -1){
                    mIntermmediateDistance = findViewById(mSettlePointResourceId).getRight();
                }else{
                    mIntermmediateDistance = getRight();
                }
                moveToInitial();
                break;

            case RIGHT_DRAG_VIEW:
                mDragDistance = getWidth();
                mInitialXPos = surfaceRect.right + getWidth();
                mInitialYPos = surfaceRect.top;
                mIsInitialized = true;
                if(mSettlePointResourceId != -1){
                    mIntermmediateDistance = findViewById(mSettlePointResourceId).getRight();
                }else{
                    mIntermmediateDistance = getWidth();
                }
                moveToInitial();
                break;

            case TOP_DRAG_VIEW:
                mDragDistance = getBottom();
                setTranslationY(-mDragDistance);
                mIsInitialized = true;
                if(mSettlePointResourceId != -1){
                    mIntermmediateDistance = findViewById(mSettlePointResourceId).getRight();
                }
                moveToInitial();
                break;

            case BOTTOM_DRAG_VIEW:
                mDragDistance = getTop();
                setTranslationY(mDragDistance);
                mIsInitialized = true;
                if(mSettlePointResourceId != -1){
                    mIntermmediateDistance = findViewById(mSettlePointResourceId).getRight();
                }
                moveToInitial();
                break;

            default:
                mIsInitialized = false;
        }

    }

    @Override
    public void moveToInitial() {
        setX(mInitialXPos);
        setY(mInitialYPos);
    }



    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        switch (state) {
            case LEFT_OPEN:

                if (mViewPosition == LEFT_DRAG_VIEW) {
                    view.setSurfaceViewOffsetX(getDragDistance());
                    view.setSurfaceViewOffsetY(0);
                    moveView(getDragDistance(), view, null);
                }

                if (mViewPosition == RIGHT_DRAG_VIEW) {
                    moveToInitial();
                }
                break;
            case RIGHT_OPEN:

                if (mViewPosition == RIGHT_DRAG_VIEW) {
                    view.setSurfaceViewOffsetX(-getDragDistance());
                    view.setSurfaceViewOffsetY(0);
                    moveView(getDragDistance(), view, null);
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

    int getDragDistance() {
        return mDragDistance;
    }

    int getIntermmediateDistance() {
        return mIntermmediateDistance;
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

    public boolean isBouncePossible() {
        return mBouncePossible;
    }

    public void setBouncePossible(boolean bouncePossible) {
        mBouncePossible = bouncePossible;
    }
}
