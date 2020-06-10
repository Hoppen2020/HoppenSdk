package co.hoppen.sdk;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.hoppen.sdk.ErrorInfo;
import com.hoppen.sdk.HoppenSDK;
import com.hoppen.sdk.InitializeCallBack;
import com.hoppen.sdk.LogUtils;

/**
 * Created by YangJianHui on 2020/5/22.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HoppenSDK.initialize(this, new InitializeCallBack() {
            @Override
            public void onInitializeCallBack(int statu) {
                if (statu == ErrorInfo.HP_OK)
                LogUtils.e("onInitializeCallBack: "+statu);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("XX",MODE_PRIVATE);
//        SharedPreferences.Editor edit = sharedPreferences.edit();
//        edit.putString("test","test");
//        edit.commit();
        String string = sharedPreferences.getString("test", "null");
        Log.e("%%%%%%",""+string);
    }
}
