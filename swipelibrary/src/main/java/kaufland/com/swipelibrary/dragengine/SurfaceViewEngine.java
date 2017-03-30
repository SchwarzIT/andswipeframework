package kaufland.com.swipelibrary.dragengine;

import android.graphics.Rect;
import android.view.View;

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

    private DraggingEngine mLeftDragView;

    private DraggingEngine mRightDragView;

    public SurfaceViewEngine(SwipeViewLayouter layouter) {
        mLayouter = layouter;
    }

    @Override
    public void moveView(float offset, SurfaceView view, View changedView) {

        if(changedView.equals(mSurfaceView)){
            return;
        }

        if(changedView.equals(mLeftDragView.getDragView())){
            mSurfaceView.setX(mLeftDragView.getDragView().getX() + mLeftDragView.getWidth());
        }

        if(changedView.equals(mRightDragView.getDragView())){
            mSurfaceView.setX(mRightDragView.getDragView().getX() - mSurfaceView.getWidth());
        }
    }

    @Override
    public SwipeResult determineSwipeHorizontalState(float velocity, SwipeDirectionDetector swipeDirectionDetector, SwipeState swipeState, final SwipeLayout.SwipeListener mSwipeListener, View releasedChild) {

        if(!mSurfaceView.equals(releasedChild)){
            return null;
        }

        int swipeDirection = swipeDirectionDetector.getSwipeDirection();

        if (swipeState.getState() == SwipeState.DragViewState.CLOSED) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                if (!mLeftDragView.isDraggable()) {
                    return new SwipeResult(0);
                }

                swipeState.setState(SwipeState.DragViewState.LEFT_OPEN);
                if (velocity > LEFT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mLeftDragView.getDragDistance() / 2)) {
                    return new SwipeResult(mLeftDragView.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(LEFT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(mLeftDragView.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(LEFT_OPEN, mLeftDragView.getIntermmediateDistance() == mLeftDragView.getDragDistance());
                        }
                    });
                }

            } else if (swipeDirection == SWIPE_DIRECTION_LEFT) {
                if (!mRightDragView.isDraggable()) {
                    return new SwipeResult(0);
                }

                swipeState.setState(RIGHT_OPEN);

                if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mRightDragView.getDragDistance() / 2)) {
                    return new SwipeResult(-mRightDragView.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(-mRightDragView.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragView.getIntermmediateDistance() == mRightDragView.getDragDistance());
                        }
                    });
                }
            }
        } else if (swipeState.getState() == SwipeState.DragViewState.LEFT_OPEN) {
            if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
                swipeState.setState(LEFT_OPEN);

                return new SwipeResult(mLeftDragView.getIntermmediateDistance(), new Runnable() {
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
                if (velocity < RIGHT_FULL_AUTO_OPEN_TRESHOLD || Math.abs(swipeDirectionDetector.getDifX()) > (mRightDragView.getDragDistance() / 2)) {
                    return new SwipeResult(-mRightDragView.getDragDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, true);
                        }
                    });
                } else {
                    return new SwipeResult(-mRightDragView.getIntermmediateDistance(), new Runnable() {
                        @Override
                        public void run() {
                            mSwipeListener.onSwipeOpened(RIGHT_OPEN, mRightDragView.getIntermmediateDistance() == mRightDragView.getDragDistance());
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

        mLeftDragView = mLayouter.getDragViewEngineByPosition(SwipeLayout.LEFT_DRAG_VIEW);
        mRightDragView = mLayouter.getDragViewEngineByPosition(SwipeLayout.RIGHT_DRAG_VIEW);
    }

    @Override
    public void moveToInitial() {
        mLayouter.getSurfaceView().setX(0);
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left) {
        boolean isOutsideRightRangeAndBounceNotPossible = left < - mRightDragView.getDragDistance() && !mRightDragView.isBouncePossible();
        boolean isOutsideLeftRangeAndBounceNotPossible = left > mRightDragView.getDragDistance() && !mRightDragView.isBouncePossible();


        if (isOutsideLeftRangeAndBounceNotPossible) {
            return mRightDragView.getDragDistance();
        }

        if(isOutsideRightRangeAndBounceNotPossible){
            return mRightDragView.getDragDistance();
        }


        return left;
    }


    @Override
    public void restoreState(SwipeState.DragViewState state, SurfaceView view) {
        //TODO
//        switch (state) {
//            case LEFT_OPEN:
//                moveToInitial();
//                break;
//            case RIGHT_OPEN:
//                moveView(mDragDistance, view, null);
//                break;
//            case TOP_OPEN:
//                //TODO Implementation
//                break;
//            case BOTTOM_OPEN:
//                //TODO Implementation
//                break;
//            default:
//
//                moveToInitial();
//                break;
//        }
    }

    @Override
    public int getWidth() {
        return mSurfaceView.getWidth();
    }

    @Override
    public int getDragDistance() {
        return 0;
    }

    @Override
    public void forceLayout() {
        mSurfaceView.forceLayout();
    }

    @Override
    public boolean isBouncePossible() {
        return false;
    }

    @Override
    public int getId() {
        return mSurfaceView.getId();
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public int getIntermmediateDistance() {
        return 0;
    }


    @Override
    public SurfaceView getDragView() {
        return mSurfaceView;
    }
}
