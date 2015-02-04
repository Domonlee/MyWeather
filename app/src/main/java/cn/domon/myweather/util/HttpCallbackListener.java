package cn.domon.myweather.util;

/**
 * Created by Domon on 15-2-4.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
