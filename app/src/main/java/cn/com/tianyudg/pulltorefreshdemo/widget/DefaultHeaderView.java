package cn.com.tianyudg.pulltorefreshdemo.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tianyudg.pulltorefreshdemo.R;

/**
 * Author : WaterFlower.
 * Created on 2017/9/22.
 * Desc :
 */

public class DefaultHeaderView extends FrameLayout implements IHeaderView {

    private static final String TAG = "DefaultHeaderView";
    private DefaultHeaderView contentView;
    private TextView tv;
    private ImageView iv;
    private ObjectAnimator rotation;
    private float factor = 4;
    private int widthPixels;
    private int tvWidth;
    private int ivWidth;
    private int headerHeight;
    private int ivHeight;
    private int tvTop;

    public DefaultHeaderView(Context context) {
        this(context, null);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        contentView = (DefaultHeaderView) LayoutInflater.from(context).inflate(R.layout.header_default, this, true);
        tv = (TextView) contentView.findViewById(R.id.tv);
        iv = (ImageView) contentView.findViewById(R.id.iv);
        widthPixels = getResources().getDisplayMetrics().widthPixels;
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        headerHeight = getHeight();
        tvWidth = tv.getWidth();
        tvTop = tv.getTop();
        ivWidth = iv.getWidth();
        ivHeight = iv.getHeight();

        setMarginLeft(iv, ivWidth / 2);
        setMarginLeft(tv, tvWidth / 2 + ivWidth / 2);
    }

    private void setMarginLeft(View v, int marginLeft) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
        layoutParams.leftMargin = 0 - marginLeft;
        v.setLayoutParams(layoutParams);
    }


    @Override
    public void onPullingDown(int downY) {
        updateHeaderView(downY);
        tv.setText("下拉刷新");
    }

    @Override
    public void onReleashToRefreshing(int downY) {
        updateHeaderView(downY);
        tv.setText("释放刷新");
    }

    @Override
    public void onRecoverHeaderState(int downY) {
        updateHeaderView(downY);
    }

    private void updateHeaderView(int downY) {
        float ratio = headerHeight / (float) (headerHeight + downY);
//        iv.setTranslationX(ratio * widthPixels);
//        tv.setTranslationX(-ratio * widthPixels);
        Log.e(TAG, "updateHeaderView: downY="+downY );
        iv.setTranslationX(downY);
        tv.setTranslationX(-downY);
    }

    @Override
    public void onRefreshing(int downY) {

        tv.setText("正在刷新");
        iv.clearAnimation();
        rotation = ObjectAnimator.ofFloat(iv, "Rotation", 0, 3600);
        rotation.setDuration(500);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.start();
    }

    @Override
    public void onFinishRefreshing() {
        rotation.cancel();
    }


}
