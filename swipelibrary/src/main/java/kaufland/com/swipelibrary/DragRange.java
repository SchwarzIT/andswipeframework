package kaufland.com.swipelibrary;

import org.androidannotations.annotations.EBean;

@EBean
public class DragRange {

    private static final int MIN_RANGE = 0;

    private int mHorizontalMaxRange;
    private int mHorizontalMinRange = MIN_RANGE;
    private int mHorizontalRange;
    private int mVerticalMaxRange;
    private int mVerticalMinRange = MIN_RANGE;
    private float mDify;
    private float mDifx;



    public int getVerticalMaxRange() {
        return mVerticalMaxRange;
    }

    public void setVerticalMaxRange(int mVerticalMaxRange) {
        this.mVerticalMaxRange = mVerticalMaxRange;
    }

    public int getVerticalMinRange() {
        return mVerticalMinRange;
    }

    public void setVerticalMinRange(int mVerticalMinRange) {
        this.mVerticalMinRange = mVerticalMinRange;
    }

    public int getVerticalRange() {
        return mVerticalRange;
    }

    public void setVerticalRange(int mVerticalRange) {
        this.mVerticalRange = mVerticalRange;
    }

    private int mVerticalRange;
    private int mDraggingBorder;
    private int mCheckRange;

    public int getHorizontalMaxRange() {
        return mHorizontalMaxRange;
    }

    public void setHorizontalMaxRange(int mHorizontalMaxRange) {
        this.mHorizontalMaxRange = mHorizontalMaxRange;
    }

    public int getHorizontalMinRange() {
        return mHorizontalMinRange;
    }

    public void setHorizontalMinRange(int mHorizontalMinRange) {
        this.mHorizontalMinRange = mHorizontalMinRange;
    }

    public int getHorizontalRange() {
        return mHorizontalRange;
    }

    public void setHorizontalRange(int mHorizontalRange) {
        this.mHorizontalRange = mHorizontalRange;
    }

    public int getDraggingBorder() {
        return mDraggingBorder;
    }

    public void setDraggingBorder(int mDraggingBorder) {
        this.mDraggingBorder = mDraggingBorder;
    }

    public int getCheckRange() {
        return mCheckRange;
    }

    public void setCheckRange(int mCheckRange) {
        this.mCheckRange = mCheckRange;
    }

    public float getDify() {
        return mDify;
    }

    public void setDify(float dify) {
        mDify = dify;
    }

    public float getDifx() {
        return mDifx;
    }

    public void setDifx(float difx) {
        mDifx = difx;
    }
}
