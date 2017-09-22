package cn.com.tianyudg.pulltorefreshdemo.widget;

/**
 * Author : WaterFlower.
 * Created on 2017/9/22.
 * Desc :
 */

public interface IHeaderView {

    void onPullingDown();

    void onReleashToRefreshing();

    void onRefreshing();

    void onFinishRefreshing();
}
