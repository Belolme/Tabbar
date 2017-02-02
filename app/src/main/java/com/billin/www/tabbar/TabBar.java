package com.billin.www.tabbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * IndicatorView
 * <p/>
 * Created by Billin on 2016/12/26.
 */
public class TabBar extends ViewGroup {

    private static final String TAG = "TabBar";

    final static int START = 0;

    final static int ON_DOWN = 1;

    final static int ON_MOVE = 2;

    final static int ON_UP = 3;

    final static int SETTING = 4;

    final static int SCROLLING = 5;

    final static int STOP_SCROLLING = 6;

    final static int ON_MANUAL_MOVE = 7;

    final static int END = 8;

    final static int ERROR = 9;

    private OffsetOfPosition mOffsetOfPosition = new OffsetOfPosition() {
        @Override
        public int get(int position) {
            int distance = Math.abs(getScrollX() - position * mCellWidth);
            return (int) ((double) distance / getWidth() * 200.0);
        }
    };

    /**
     * 用于纪录最后一个触控点的变量
     */
    private float mLastX;

    /**
     * 当前选中子控件的位置
     */
    private int mSelectedPosition;

    /**
     * 显示出来控件的个数
     */
    private int mNotGoneCount;

    /**
     * 分配给子控件的宽度
     */
    private int mCellWidth;

    /**
     * 分配给子控件的高度
     */
    private int mCellHeight;

    private StatusController mStatusController = new StatusController();

    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();

    private Scroller mScroller = new Scroller(getContext());

    private TabBarListener mListener;

    public TabBar(Context context) {
        super(context);
    }

    public TabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        mNotGoneCount = 0;

        int cellWidth = MeasureSpec.getSize(widthMeasureSpec) / 3;
        int cellHeight = MeasureSpec.getSize(heightMeasureSpec);
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;

        for (int i = 0; i < count; i++) {

            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                mNotGoneCount++;
                measureChild(child,
                        MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            }
        }

        setMeasuredDimension(cellWidth * 3, cellHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int childLeft = mCellWidth;
        int childTop;

        for (int i = 0; i < count; i++) {

            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();

                int interval = (mCellWidth - width) / 2;
                childLeft = interval + childLeft;
                childTop = (mCellHeight - height) / 2;

                child.layout(childLeft, childTop, childLeft + width, childTop + height);

                childLeft += width + interval;
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStatusController.put(StatusController.DOWN);
                mVelocityTracker.clear();

                mLastX = x;
                postInvalidate();

                break;

            case MotionEvent.ACTION_MOVE:
                mStatusController.put(StatusController.MOVE);

                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                mVelocityTracker.addMovement(event);

                float dx = mLastX - x;

                if (getScrollX() < -mCellWidth && dx < 0)
                    dx = 0;

                if (getScrollX() > mNotGoneCount * mCellWidth && dx > 0)
                    dx = 0;

                scrollBy((int) dx, 0);
                mLastX = x;

                break;

            case MotionEvent.ACTION_UP:
                mStatusController.put(StatusController.UP);

                int index = getMostRecentPosition();

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);

                int xVelocity = (int) -mVelocityTracker.getXVelocity();
                index = xVelocity / 3600 + index;

                moveToPosition(index, true);

                break;
        }

        return true;
    }

    private int getMostRecentPosition() {
        int index = getScrollX() / mCellWidth;
        return getScrollX() % mCellWidth > mCellWidth / 2 ? index + 1 : index;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {

            mStatusController.put(StatusController.MANUAL_MOVE);

            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    /**
     * 移动到指定位置
     *
     * @param position 指定的位置
     * @param smooth   是否流畅移动
     */
    private void moveToPosition(int position, boolean smooth) {
        // check the position is invalid or not
        position = position < 0 ? 0 : position;
        position = position >= mNotGoneCount ? mNotGoneCount - 1 : position;

        // scroll
        int dx = position * mCellWidth - getScrollX();

        if (smooth) {
            mScroller.startScroll(getScrollX(), 0, dx, 0);
        } else {
            if (!mScroller.isFinished())
                mScroller.abortAnimation();

            scrollBy(dx, 0);
        }
        postInvalidate();

        // update selected index and call listener
        if (position == mSelectedPosition) {
            return;
        }
        mSelectedPosition = position;
    }

    public void setOffsetOfPosition(int position, float offset) {
        // update selected position
        if (offset == 0) {
            mSelectedPosition = position;
        }

        mStatusController.put(StatusController.MOVE);

        int scrollX = (int) (mCellWidth * position + offset * getWidth() / 200.0);
        setScrollX(scrollX);
    }

    public void setListener(TabBarListener listener) {
        this.mListener = listener;
    }


    public void setAdapter(TabBarAdapter adapter) {
        adapter.setTabBar(this);
    }

    public int getCurrPosition() {
        return mSelectedPosition;
    }


    private class StatusController {

        final static int MOVE = 0;

        final static int DOWN = 1;

        final static int UP = 2;

        final static int MANUAL_MOVE = 3;

        int status;

        int position = mSelectedPosition;

        private boolean isConform() {
            return getScrollX() == mSelectedPosition * mCellWidth;
        }

        void checkAndInform(int newStatus) {
            if (newStatus == status) {
                return;
            }

            status = newStatus;
            mListener.onStateChange(newStatus);
        }

        void start(int op) {
            if (DOWN == op) {
                checkAndInform(ON_DOWN);
            } else if (op == MANUAL_MOVE) {
                checkAndInform(ON_MOVE);
            } else if (op == UP) {
                checkAndInform(ON_UP);
            } else//noinspection StatementWithEmptyBody
                if (op == MOVE) {

                } else {
                    error(op);
                }
        }

        void onDown(int op) {
            if (op == MOVE) {
                checkAndInform(ON_MOVE);
            } else if (op == UP) {
                checkAndInform(ON_UP);
            } else {
                error(op);
            }
        }

        void onMove(int op) {
            if (UP == op) {
                checkAndInform(ON_UP);
            } else if (MOVE == op) {
                // nothing to do
            } else {
                error(op);
            }
        }

        void onManualMove(int op) {
            if (op == MANUAL_MOVE && isConform()) {
                checkAndInform(END);
            } else if (op == MANUAL_MOVE) {
                // nothing to do
            } else {
                error(op);
            }
        }

        void onUp(int op) {
            checkAndInform(SETTING);
            setting(op);
        }

        void setting(int op) {
            if (mSelectedPosition != position) {
                position = mSelectedPosition;
                mListener.onSelected(mSelectedPosition);
            }

            if (op == MOVE) {
                checkAndInform(SCROLLING);
            } else {
                error(op);
            }
        }

        void scrolling(int op) {
            if (op == DOWN) {
                checkAndInform(STOP_SCROLLING);
            } else if (op == MOVE && isConform()) {
                checkAndInform(END);
            } else if (op == MOVE) {
                // nothing to do
            } else {
                error(op);
            }
        }

        void stopScrolling(int op) {
            if (op == MOVE) {
                checkAndInform(ON_MOVE);
            } else if (op == UP) {
                checkAndInform(SETTING);
            } else {
                error(op);
            }
        }

        void end(int op) {
            checkAndInform(START);
        }

        void put(int op) {
            if (mListener == null)
                return;

            if (op == MOVE) {
                mListener.onScroll(mOffsetOfPosition);
            }

            switch (status) {
                case START:
                    start(op);
                    break;

                case ON_DOWN:
                    onDown(op);
                    break;

                case ON_MOVE:
                    onMove(op);
                    break;

                case ON_UP:
                    onUp(op);
                    break;

                case SETTING:
                    setting(op);
                    break;

                case SCROLLING:
                    scrolling(op);
                    break;

                case STOP_SCROLLING:
                    stopScrolling(op);
                    break;

                case ON_MANUAL_MOVE:
                    onManualMove(op);
                    break;

                case END:
                    end(op);
                    break;

                case ERROR:
                    errorOp(op);
                    break;
            }
        }

        void errorOp(int op) {
            if (isConform()) {
                checkAndInform(END);
            }
        }

        /**
         * 使TabBar回到正常状态且重置TabBar状态
         */
        void error(int op) {
            Log.d(TAG, "error: origin status" + status + " op: " + op);
            status = ERROR;
            position = getMostRecentPosition();
            if (position != mSelectedPosition) {
                mSelectedPosition = position;
                mListener.onSelected(mSelectedPosition);
            }
            moveToPosition(mSelectedPosition, true);
        }
    }

    public abstract static class TabBarAdapter {

        private TabBar mTabBar;

        public abstract int getCount();

        public abstract View getView(int position);

        public void notifyDataSetChanged() {
            if (mTabBar == null)
                return;

            mTabBar.mSelectedPosition = 0;

            mTabBar.removeAllViews();
            for (int i = 0; i < getCount(); i++) {
                mTabBar.addView(getView(i));
            }

            mTabBar.postInvalidate();
        }

        private void setTabBar(TabBar tabBar) {
            mTabBar = tabBar;
            notifyDataSetChanged();
        }
    }

    public interface TabBarListener {
        void onScroll(OffsetOfPosition offsetOfPosition);

        void onSelected(int position);

        void onStateChange(int state);
    }

    public interface OffsetOfPosition {

        /**
         * 获取任意一个位置距离中心位置的权值。<br>
         * 0 代表控件在中心位置
         *
         * @param position The position of view
         * @return 0 ~ ...
         */
        int get(int position);
    }
}
