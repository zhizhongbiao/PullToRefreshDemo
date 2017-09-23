package cn.com.tianyudg.pulltorefreshdemo.widget;

/**
 * Author : WaterFlower.
 * Created on 2017/9/22.
 * Desc :
 */

public interface IHeaderView {

    void onPullingDown(float downYProgress);

    void onReleashToRefreshing(float downYProgress);

    void onRecover(float downYProgress);

    void onRefreshing(float downYProgress);

    void onFinishRefreshing();
}
