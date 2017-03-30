package kaufland.com.swipelibrary.dragengine;

import android.view.View;

import kaufland.com.swipelibrary.DragView;
import kaufland.com.swipelibrary.SurfaceView;
import kaufland.com.swipelibrary.SwipeDirectionDetector;
import kaufland.com.swipelibrary.SwipeLayout;
import kaufland.com.swipelibrary.SwipeResult;
import kaufland.com.swipelibrary.SwipeState;
import kaufland.com.swipelibrary.SwipeViewLayouter;

import static kaufland.com.swipelibrary.SwipeDirectionDetector.SWIPE_DIRECTION_LEFT;
import static kaufland.com.swipelibrary.SwipeDirectionDetector.SWIPE_DIRECTION_RIGHT;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.CLOSED;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.LEFT_OPEN;
import static kaufland.com.swipelibrary.SwipeState.DragViewState.RIGHT_OPEN;

/**
 * Created by sbra0902 on 29.03.17.
 */

public class SurfaceViewEngine implements DraggingEngine<SurfaceView> {

    public static final double AUTO_OPEN_SPEED_TRESHOLD = 800.0;
    public static final double LEFT_FULL_AUTO_OPEN_TRESHOLD = AUTO_OPEN_SPEED_TRESHOLD * 2;
    public static final double RIGHT_FULL_AUTO_OPEN_TRESHOLD = -LEFT_FULL_AUTO_OPEN_TRESHOLD;

    private SwipeViewLayouter mLayouter;

    private SurfaceView mSurfaceView;

    private DraggingEngine mLeftDragViewEngine;

    private DraggingEngine mRightDragViewEngine;

    public SurfaceViewEngine(SwipeViewLayouter layouter) {
        mLayouter = layouter;
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {

        if(changedView.equals(mSurfaceView)){
            return;
        }

        if(changedView.equals(mLeftDragViewEngine.getDragView())){
            mSurfaceView.setX(mLeftDragViewEngine.getDragView().getX() + mLeftDragViewEngine.getDragView().getWidth());
        }

        if(changedView.equals(mRightDragViewEngine.getDragView())){
            mSurfaceView.setX(mRightDragViewEngine.getDragView().getX() - mSurfaceView.getWidth());
        }
    }

    private boolean isMinimumDifReached(int distanceToNextState, SwipeDirectionDetector detector){
        return Math.abs(distanceToNextState / 2) <= Math.abs(detector.getDifX()) && Math.abs(detector.getDifX()) > Math.abs(detector.getDifY()) && !detector.isHorizontalScrollChangedWhileDragging();
    }

    @Override
    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, final SwipeLayout.SwipeListener mSwipeListener, View releasedChild) {

        if(!mSurfaceView.equals(releasedChild)){
            return null;
        }

        int swipeDirection = swipeDirectionDetector.getSwipeDirection();

        if (swipeState.getState() == SwipeState.DragViewState.CLOSED) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                if (!getDragViewForEngine(mLeftDragViewEngine).isDraggable() || !isMinimumDifReached(mLeftDragViewEngine.getIntermmediateDistance(), swipeDirectionDetector)) {
                    return new SwipeResult(0);
                }

                swipeState.setState(SwipeState.DragViewState.LEFT_OPEN);
                if (velocity > LEFT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mLeftDragViewEngine.getDragDistance() / 2)) {
                    return new SwipeResult(mLeftDragViewEngine.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(LEFT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(mLeftDragViewEngine.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(LEFT_OPEN, mLeftDragViewEngine.getIntermmediateDistance() == mLeftDragViewEngine.getDragDistance());
                        }
                    });
                }

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                if (!getDragViewForEngine(mRightDragViewEngine).isDraggable() || !isMinimumDifReached(mRightDragViewEngine.getIntermmediateDistance(), swipeDirectionDetector)) {
                    return new SwipeResult(0);
                }

                swipeState.setState(RIGHT_OPEN);

                if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mRightDragViewEngine.getDragDistance() / 2)) {
                    return new SwipeResult(-mRightDragViewEngine.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(-mRightDragViewEngine.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragViewEngine.getIntermmediateDistance() == mRightDragViewEngine.getDragDistance());
                        }
                    });
                }
            }
        } else if (swipeState.getState() == SwipeState.DragViewState.LEFT_OPEN) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                swipeState.setState(LEFT_OPEN);

                return new SwipeResult(mLeftDragViewEngine.getIntermmediateDistance(), new Runnable() {
                    @Override
                    public void run() {
                        mSwipeListener.onBounce(LEFT_OPEN);
                    }
                });

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {

                swipeState.setState(CLOSED);
                return new SwipeResult(0, new Runnable() {
                    @Override
                    public void run() {
                        mSwipeListener.onSwipeClosed(CLOSED);
                    }
                });
            }
        } else if (swipeState.getState() == RIGHT_OPEN) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {

                swipeState.setState(SwipeState.DragViewState.CLOSED);
                return new SwipeResult(0, new Runnable() {
                    @Override
                    public void run() {
                        mSwipeListener.onSwipeClosed(CLOSED);
                    }
                });

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                swipeState.setState(RIGHT_OPEN);
                if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mRightDragViewEngine.getDragDistance() / 2)) {
                    return new SwipeResult(-mRightDragViewEngine.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(-mRightDragViewEngine.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragViewEngine.getIntermmediateDistance() == mRightDragViewEngine.getDragDistance());
                        }
                    });
                }
            }

        }

        return new SwipeResult(0);
    }

    @Override
    public void initializePosition(SwipeViewLayouter.DragDirection orientation) {

        mSurfaceView = mLayouter.getSurfaceView();

        mLeftDragViewEngine = mLayouter.getDragViewEngineByPosition(SwipeLayout.LEFT_DRAG_VIEW);
        mRightDragViewEngine = mLayouter.getDragViewEngineByPosition(SwipeLayout.RIGHT_DRAG_VIEW);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left) {
        boolean isOutsideRightRangeAndBounceNotPossible = left < - mRightDragViewEngine.getDragDistance() && !getDragViewForEngine(mRightDragViewEngine).isBouncePossible();
        boolean isOutsideLeftRangeAndBounceNotPossible = left > mLeftDragViewEngine.getDragDistance() && !getDragViewForEngine(mLeftDragViewEngine).isBouncePossible();


        if (isOutsideLeftRangeAndBounceNotPossible) {
            return mLeftDragViewEngine.getDragDistance();
        }

        if(isOutsideRightRangeAndBounceNotPossible){
            return mRightDragViewEngine.getDragDistance();
        }


        return left;
    }


    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {

        switch (state) {
            case LEFT_OPEN:
                mSurfaceView.offsetLeftAndRight(mLeftDragViewEngine.getDragDistance());
                break;
            case RIGHT_OPEN:
                mSurfaceView.offsetLeftAndRight(-mRightDragViewEngine.getDragDistance());
                break;
            case TOP_OPEN:
                //TODO Implementation
                break;
            case BOTTOM_OPEN:
                //TODO Implementation
                break;
            default:
                mSurfaceView.offsetLeftAndRight(0);
                break;
        }
    }

    private DragView getDragViewForEngine(DraggingEngine engine){
        return (DragView) engine.getDragView();
    }


    @Override
    public int getDragDistance() {
        return 0;
    }

    @Override
    public int getIntermmediateDistance() {
        return 0;
    }

    @Override
    public int getOpenOffset() {
        return 0;
    }


    @Override
    public SurfaceView getDragView() {
        return mSurfaceView;
    }
}
