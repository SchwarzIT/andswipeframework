package kaufland.com.swipelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class DragView extends LinearLayout {

    private int mSettlePointResourceId;

    private int mViewPosition;

    private boolean mDraggable = true;

    private boolean mIsInitialized;

    private boolean mBouncePossible;

    public DragView(Context context) {
        super(context);
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyleable(context, attrs);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
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


    public int getSettlePointResourceId() {
        return mSettlePointResourceId;
    }

    public void setDraggable(boolean canDrag) {
        mDraggable = canDrag;
    }

    public boolean isDraggable() {
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
