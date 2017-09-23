package cn.com.tianyudg.pulltorefreshdemo.widget;

/**
 * Author : WaterFlower.
 * Created on 2017/9/22.
 * Desc :
 */

public interface IHeaderView {

    void onPullingDown(int downY);

    void onReleashToRefreshing(int downY);

    void onRecoverHeaderState(int downY);

    void onRefreshing(int downY);

    void onFinishRefreshing();
}
