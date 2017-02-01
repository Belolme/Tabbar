package com.billin.www.tabbar;

import android.content.Context;
import android.util.AttributeSet;
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

    public static final int ON_DOWN = 1;

    public static final int ON_UP = 2;

    public static final int START_MOVE = 3;

    public static final int ON_MOVE = 4;

    public static final int END_MOVE = 5;

    public static final int SETTING_MOVE = 6;

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
     * 判断视图是否正在移动
     */
    private boolean mIsMove;

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
                mVelocityTracker.clear();

                mLastX = x;
                postInvalidate();

                if (mListener != null)
                    mListener.onStateChange(ON_DOWN);

                break;

            case MotionEvent.ACTION_MOVE:
                mIsMove = true;

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

                // call listener
                if (mListener != null)
                    mListener.onScroll(mOffsetOfPosition);

                break;

            case MotionEvent.ACTION_UP:
                int index;
                index = getScrollX() / mCellWidth;
                index = getScrollX() % mCellWidth > mCellWidth / 2 ? index + 1 : index;

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);

                int xVelocity = (int) -mVelocityTracker.getXVelocity();
                index = xVelocity / 3600 + index;

                moveToPosition(index, true);

                if (mListener != null)
                    mListener.onStateChange(ON_UP);

                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();

            if (mListener != null) {
                mListener.onScroll(mOffsetOfPosition);
            }
        } else {
            if (mListener != null && mIsMove && getScrollX() == mSelectedPosition * mCellWidth) {
                mListener.onStateChange(END_MOVE);
                mIsMove = false;
            }
        }
    }

/*
这一个方法会导致不断的调用dispatchDraw() 估计是因为子视图的重绘又导致了父容器的再一次绘制
    private void showComponent(boolean isShow) {
        if (mIsShow) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setVisibility(VISIBLE);
            }
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setVisibility(INVISIBLE);
            }
            getChildAt(mSelectedPosition).setVisibility(VISIBLE);
        }
    }*/

    /**
     * 移动到指定位置
     *
     * @param position 指定的位置
     * @param smooth   是否流畅移动
     */
    public void moveToPosition(int position, boolean smooth) {
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

        if (mListener != null)
            mListener.onSelected(mSelectedPosition);
    }

    public void setOffsetOfPosition(int position, float offset) {
        // TODO: 2017/2/1 add scroll state
        mIsMove = true;

        if (offset == 0) {
            mSelectedPosition = position;
//            if (mListener != null)
//                mListener.onSelected(position);
        }

        int scrollX = (int) (mCellWidth * position + offset * getWidth() / 200.0);
        setScrollX(scrollX);

        if (mListener != null)
            mListener.onScroll(mOffsetOfPosition);
    }

    public void setListener(TabBarListener listener) {
        this.mListener = listener;
    }


    public void setAdapter(TabBarAdapter adapter) {
        adapter.setTabBar(this);
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

    public int getCurrPosition() {
        return mSelectedPosition;
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
