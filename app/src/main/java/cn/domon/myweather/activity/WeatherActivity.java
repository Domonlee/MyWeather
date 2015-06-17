package cn.domon.myweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guomob.banner.GuomobAdView;
import com.guomob.banner.OnBannerAdListener;
import com.guomob.screen.GuomobInScreenAd;
import com.guomob.screen.OnInScreenAdListener;
import com.umeng.analytics.MobclickAgent;

import cn.domon.myweather.R;
import cn.domon.myweather.util.HttpCallbackListener;
import cn.domon.myweather.util.HttpUtil;
import cn.domon.myweather.util.Utility;

/**
 * Created by Domon on 15-2-9.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;

    private RelativeLayout mAdRl;
    private GuomobAdView mAdView;
    private GuomobInScreenAd mInScreenAd;

    private RelativeLayout badyRl;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_data);
        badyRl = (RelativeLayout) findViewById(R.id.body_rl);

        //TST Code
        mAdRl = (RelativeLayout) findViewById(R.id.ad_banner_rl);
        mAdView = new GuomobAdView(this,"j7x32ul108i6940");
        mAdRl.addView(mAdView);

        mInScreenAd = new GuomobInScreenAd(this,"j7x32ul108i6940",true);
        mInScreenAd.LoadInScreenAd(true);
        mInScreenAd.setOnInScreenAdListener(new OnInScreenAdListener() {
            //无网络连接
            public void onNetWorkError() {
                // TODO Auto-generated method stub
                Log.e("GuomobLog", "onNetWorkError");
            }

            //加载广告成功
            public void onLoadAdOk() {
                // TODO Auto-generated method stub
                Log.e("GuomobLog", "onLoadAdOk");
            }

            //加载广告失败 arg0：失败原因
            public void onLoadAdError(String arg0) {
                // TODO Auto-generated method stub
                Log.e("GuomobLog", "onLoadInScreenAdError:" + arg0);
            }

            //用户关闭广告
            public void onClose() {
                // TODO Auto-generated method stub
                Log.e("GuomobLog", "onClose");
            }
        });

        mInScreenAd.ShowInScreenAd();

        mAdView.setOnBannerAdListener(new OnBannerAdListener() {

            //无网络连接
            public void onNetWorkError() {
                Log.e("GuomobLog", "onNetWorkError");
            }

            //加载广告成功
            public void onLoadAdOk() {
                Log.e("GuomobLog", "onLoadAdOk");
            }

            //加载广告失败  arg0：失败原因
            public void onLoadAdError(String arg0) {
                Log.e("GuomobLog", "onLoadAdError" + arg0);
            }
        });


        cityNameText.setOnClickListener(this);


        //TST Code End

        badyRl.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            //没有时，直接显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中得到天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(preferences.getString("city_name", ""));
        temp1Text.setText(preferences.getString("temp1", ""));
        temp2Text.setText(preferences.getString("temp2", ""));
        weatherDespText.setText(preferences.getString("weather_desp", ""));
        publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
        currentDateText.setText(preferences.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.body_rl:
                count++;
                if (count == 10) {
//                    Toast.makeText(this, "Love U,My YoYO~", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Life is Strange!", Toast.LENGTH_LONG).show();
                    count = 0;
                }
                break;
            //TST CODE Start
            case R.id.city_name:
                if(mInScreenAd.IsInScreenAdLoad()){
                    mInScreenAd.ShowInScreenAd();
                }else{
                    mInScreenAd.LoadInScreenAd(false);
                }
                break;
            //TST CODE End
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("WeatherActivity");
        MobclickAgent.onResume(this);
        count = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("WeatherActivity");
        MobclickAgent.onPause(this);
    }
}
