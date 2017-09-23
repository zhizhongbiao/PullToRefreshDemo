package cn.com.tianyudg.pulltorefreshdemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Author : WaterFlower.
 * Created on 2017/9/20.
 * Desc :
 */

public class RefreshLayout extends LinearLayout {

    private static final String TAG = "RefreshLayout";
    private final int mTouchSlop;

    private float mDownY;
    private float mLastY;
    private float mMoveY;

    private int UP_DIRECTION = -1;
    private int DOWN_DIRECTION = 1;
    private int mDiff;

    //阻尼系数,越小阻力越大
    private int factor = 2;

    private View header;
    private int headerHeight;
    private Scroller mScroller;
    private Status currentStatus = Status.IDLE;
    private View contentView;
    private float mDownX;
    private float mMoveX;
    private float downYProgress;

    public enum Status {
        IDLE, PULLING_DOWN, RELEASE_TO_REFRESH, REFRESHING
    }


    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setWillNotDraw(false);
        setClickable(true);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
        header = new DefaultHeaderView(context);
        addView(header);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount < 2) return;
//        header = getChildAt(0);
        contentView = getChildAt(1);

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        headerHeight = this.header.getHeight();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();
        layoutParams.topMargin = -headerHeight;
        header.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                mMoveY = ev.getRawY();
                mDiff = (int) (mLastY - mMoveY);
                if (mDiff < 0 && !contentView.canScrollVertically(UP_DIRECTION) || currentStatus != Status.IDLE)
                    return true;

                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = event.getRawY();
//                mDownX = event.getRawX();
                mLastY = this.mDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveY = event.getRawY();
//                mMoveX = event.getRawX();
//                boolean isScrollVertical = Math.abs(mLastY - mMoveY) - Math.abs(mDownX - mMoveX) > 0 ? true : false;
//                if (!isScrollVertical) return super.onTouchEvent(event);

                mDiff = (int) (mLastY - mMoveY);
                mLastY = mMoveY;

                if (currentStatus != Status.REFRESHING && mDiff < 0) {
                    scrollBy(0, (int) (mDiff * (headerHeight / (headerHeight + mMoveY / factor))));
                    updateHeaderState();

                    return true;
                } else if (getScrollY() < 0 && (currentStatus == Status.PULLING_DOWN || currentStatus == Status.RELEASE_TO_REFRESH)) {
                    scrollBy(0, (int) (mDiff * (headerHeight / (headerHeight + mMoveY / factor))));
                    updateHeaderState();
                    return true;

                }

                break;
            case MotionEvent.ACTION_UP:

                if (currentStatus == Status.IDLE) return super.onTouchEvent(event);

                int scrollDistance;
                if (getScrollY() > 0) {
                    scrollDistance = -getScrollY();
                    currentStatus = Status.IDLE;
                } else if (Math.abs(getScrollY()) >= headerHeight) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                     downYProgress = Math.abs(getScrollY()) / (float)headerHeight;
                    ((IHeaderView) header).onRefreshing(downYProgress);
                    scrollDistance = Math.abs(getScrollY()) - headerHeight;
                    currentStatus = Status.REFRESHING;

                } else {
                    scrollDistance = Math.abs(getScrollY());
                    currentStatus = Status.IDLE;
                }

                mScroller.startScroll(0, getScrollY(), 0, scrollDistance);
                invalidate();
                return true;

        }

        return super.onTouchEvent(event);
    }

    private void updateHeaderState() {

        if (Math.abs(getScrollY()) >= headerHeight) {
            downYProgress = Math.abs(getScrollY()) / (float)headerHeight;
            ((IHeaderView) header).onReleashToRefreshing(downYProgress);
            currentStatus = Status.RELEASE_TO_REFRESH;
        } else {
             downYProgress = Math.abs(getScrollY()) / (float)headerHeight;
            ((IHeaderView) header).onPullingDown(downYProgress);
            currentStatus = Status.PULLING_DOWN;
        }
    }


    public void finishRefreshing() {
        if (!isRefreshing()) return;
        ((IHeaderView) header).onFinishRefreshing();
        mScroller.startScroll(0, getScrollY(), 0, headerHeight);
        currentStatus = Status.IDLE;
        invalidate();

    }


    public boolean isRefreshing() {
        return currentStatus == Status.REFRESHING;
    }


    /**
     * 该方法主要是为了Scroller服务的
     */
    @Override
    public void computeScroll() {
        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            if (currentStatus==Status.REFRESHING)
            {
                ((IHeaderView) header).onRecover(downYProgress);
                downYProgress=Math.abs(getScrollY()) / (float)headerHeight;
//                ((IHeaderView) header).onRecover(Math.abs(getScrollY()) / (float)headerHeight);
//                Log.e(TAG, "Math.abs(getScrollY()) / (float)headerHeight=" + Math.abs(getScrollY()) / (float)headerHeight);
            }


            invalidate();
        }
    }


    public OnRefreshListener getmListener() {
        return mListener;
    }

    private OnRefreshListener mListener;

    public void setmListener(OnRefreshListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }


}
