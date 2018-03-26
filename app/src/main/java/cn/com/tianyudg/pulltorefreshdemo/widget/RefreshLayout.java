package cn.com.tianyudg.pulltorefreshdemo.widget;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Author : WaterFlower.
 * Created on 2017/9/20.
 * Desc :
 */

public class RefreshLayout extends LinearLayout implements NestedScrollingChild, NestedScrollingParent {

    private static final String TAG = "RefreshLayout";
    private final int mTouchSlop;

    private float mDownY;
    private float mLastY;
    private float mMoveY;

    private int UP_DIRECTION = -1;
    private int DOWN_DIRECTION = 1;
    private int mDiff;

    //阻尼系数,越小阻力越大
    private int headerFactor = 4;
    private int footerFactor = 10;

    private View header;
    private int headerHeight;
    private Scroller mScroller;
    private Status currentStatus = Status.IDLE;
    private View contentView;
    private float mDownX;
    private float mMoveX;
    private int screenHeight;
    private boolean isReadyToPullToRefresh;

    /**
     * 状态值
     */
    public enum Status {
        IDLE, PULLING_DOWN, RELEASE_TO_REFRESH, REFRESHING, PULLING_UP
    }

    public View getHeader() {
        return header;
    }

    public void setHeader(View header) {
        this.header = header;
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

//        因为该控件不需要draw,所以禁用onDraw
        setWillNotDraw(false);
//        设置该方法使该控件onTouchEvent方法起作用
        setClickable(true);
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//        初始化滑动控制器
        mScroller = new Scroller(context);

        header = getHeader();
        if (header == null) {
            header = new DefaultHeaderView(context);
        }
        addView(header);

    }


    /**
     * 检查一下子控件是否足够
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 2) throw new RuntimeException("You can only add one headerView" +
                "(which has a default one if you don't invoke setHeader() method)" +
                " and one contentView to this ViewGroup");
//        header = getChildAt(0);
        contentView = getChildAt(1);

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        headerHeight = this.header.getHeight();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();
//        隐藏HeaderView
        layoutParams.topMargin = -headerHeight;
        header.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                Log.e(TAG, "onInterceptTouchEvent: ---------ACTION_DOWN" );
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onInterceptTouchEvent: ---------ACTION_MOVE" );
                mMoveY = ev.getRawY();
                mDiff = (int) (mLastY - mMoveY);
//                如果不处于正常停止状态全部拦截,让该父控件自己处理事件,不让其子控件处理事件
                if (currentStatus != Status.IDLE) return true;

//                如果下拉并contentView处于其自身的顶部状态下,拦截事件,让该父控件处理下拉刷新事件
//                if (mDiff < 0 && !contentView.canScrollVertically(UP_DIRECTION))//该处判断顶部条件有问题
                if (mDiff < 0 && !canChildScrollUp(contentView))//该处判断顶部条件有问题
                {
                    Log.e(TAG, "onInterceptTouchEvent: mDiff="+mDiff );
                    return true;
                }


//                如果上拉并contentView处于其自身的底部状态下,拦截事件,让该父控件处理上拉加载更多事件
                if (mDiff > 0 && !contentView.canScrollVertically(DOWN_DIRECTION))//该处判断底部条件有问题
                    return true;


                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onInterceptTouchEvent: ---------ACTION_UP" );
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
                return false;
            case MotionEvent.ACTION_MOVE:

                mMoveY = event.getRawY();
//                mMoveX = event.getRawX();
//                boolean isScrollVertical = Math.abs(mLastY - mMoveY) - Math.abs(mDownX - mMoveX) > 0 ? true : false;
//                if (!isScrollVertical) return super.onTouchEvent(event);

//                记录滑动距离
                mDiff = (int) (mLastY - mMoveY);
                mLastY = mMoveY;

//                下滑并当前状态不是正在刷新,下滑距离乘以阻尼系数==滑动距离
                if ((currentStatus == Status.PULLING_DOWN
                        || currentStatus == Status.RELEASE_TO_REFRESH
                        || currentStatus == Status.IDLE)
                        && mDiff < 0) {
                    scrollBy(0, (int) (mDiff * (headerHeight / (headerHeight + mMoveY / headerFactor))));
                    updateHeaderState();
                    return true;
                }


                if (getScrollY() < 0 &&
                        (currentStatus == Status.PULLING_DOWN ||
                                currentStatus == Status.RELEASE_TO_REFRESH)) {
//                    当处于手触摸着屏幕移动的状态时候,向上滑动该Viewgroup的内容View向上移动
                    scrollBy(0, (int) (mDiff * (headerHeight / (headerHeight + mMoveY / headerFactor))));
                    updateHeaderState();
                    return true;

                }

                if (getScrollY() > 0 &&
                        currentStatus == Status.PULLING_UP) {
                    scrollBy(0, mDiff);
                }


                if ((currentStatus == Status.IDLE || currentStatus == Status.PULLING_UP) && mDiff > 0) {

                    scrollBy(0, mDiff);
                    currentStatus = Status.PULLING_UP;
                    return true;

                }


                break;
            case MotionEvent.ACTION_UP:
//                如果处于正常停止状况,事件交给父View处理
                if (currentStatus == Status.IDLE) return super.onTouchEvent(event);

                int scrollDistance;
                if (getScrollY() > 0) {

//                    如果是向上滑动,放手直接恢复原来位置
                    scrollDistance = -getScrollY();
                    currentStatus = Status.IDLE;
                } else if (Math.abs(getScrollY()) >= headerHeight) {
//                    如果是向上滑动超过了HeaderView的高度,放手直接恢复到正在刷新状态,就是HeaderView高度的高度;
                    if (mListener != null) {
                        mListener.onRefresh();
                    }

                    ((IHeaderView) header).onRefreshing(Math.abs(getScrollY()));
                    scrollDistance = Math.abs(getScrollY()) - headerHeight;
                    currentStatus = Status.REFRESHING;

                } else {
//                    向下滑动,但是滑动的距离没到达HeaderView的高度,放手直接恢复原来的位置
                    scrollDistance = Math.abs(getScrollY());
                    currentStatus = Status.IDLE;
                }

//                用滑动控制器Scroller去恢复该ViewGroup中ContentVIew移动了的距离
                mScroller.startScroll(0, getScrollY(), 0, scrollDistance);
                invalidate();
                return true;

        }

        return super.onTouchEvent(event);
    }

    /**
     * 更新HeaderView状态
     */
    private void updateHeaderState() {
        if (Math.abs(getScrollY()) >= headerHeight) {
            ((IHeaderView) header).onReleashToRefreshing(Math.abs(getScrollY()));
            currentStatus = Status.RELEASE_TO_REFRESH;
        } else {
            ((IHeaderView) header).onPullingDown(Math.abs(getScrollY()));
            currentStatus = Status.PULLING_DOWN;
        }
    }


    /**
     * 结束刷新
     */
    public void finishRefreshing() {
        if (!isRefreshing()) return;
        ((IHeaderView) header).onFinishRefreshing();
        currentStatus = Status.IDLE;
        mScroller.startScroll(0, getScrollY(), 0, headerHeight);
        invalidate();

    }


    /**
     * 判断是否正在刷新
     *
     * @return
     */
    public boolean isRefreshing() {
        return currentStatus == Status.REFRESHING;
    }


    /**
     * 该方法主要是为了Scroller服务的
     * ,当当前控件通过Scroller去移动时候,该方法会不断被父控件调用
     * Called by a parent to request that a child update its values for mScrollX
     * and mScrollY if necessary. This will typically be done if the child is
     * animating a scroll using a {@link android.widget.Scroller Scroller}
     * object.
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            ((IHeaderView) header).onRecoverHeaderState(Math.abs(getScrollY()));
            invalidate();
        }
    }


    public OnRefreshListener getListener() {
        return mListener;
    }

    private OnRefreshListener mListener;

    public void setListener(OnRefreshListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }


    public  boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

}
