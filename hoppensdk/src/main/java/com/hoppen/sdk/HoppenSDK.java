package com.hoppen.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YangJianHui on 2020/5/21.
 */
public class HoppenSDK {
    private volatile static HoppenSDK instance = null;
    private static int initStatus = ErrorInfo.HP_NOT_INITIALIZED;
    private final static String SDK_KEY = "com.hoppen.sdk.key";

    private final static String test = "http://data.wesipull.net/API/System/Validate/";

    public static int getInitStatus() {
        return initStatus;
    }

    private HoppenSDK(){

    }


    public static void initialize(Context context){
        initialize(context,null);
    }
    private static void setInitializeCallBack(int status,InitializeCallBack initializeCallBack){
        initStatus = status;
        if (initializeCallBack!=null){
            initializeCallBack.onInitializeCallBack(initStatus);
        }
    }

    public static void initialize(Context context, final InitializeCallBack initializeCallBack){
        if (context==null) {
            setInitializeCallBack(ErrorInfo.HP_ERR_INVALID_PARAM,initializeCallBack);
            return;
        }
        if (instance==null){
            synchronized (HoppenSDK.class){
                if (instance ==null){
                    instance = new HoppenSDK();
                }
            }
        }
        if (initStatus!=ErrorInfo.HP_OK){
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                boolean apiKey = applicationInfo.metaData.containsKey(SDK_KEY);
                if (apiKey){
                    String key = applicationInfo.metaData.getString(SDK_KEY,"");
                    if (key.equals("")){
                        setInitializeCallBack(ErrorInfo.HP_ERR_INVALID_SDKKEY,initializeCallBack);
                        return;
                    }else{
                        ExecutorService executor = Executors.newCachedThreadPool();
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                //请求
                                //获取sdk secret
                                //----------test------------------
                                Map<String,String> map = new HashMap<>();
                                map.put("test","1");
                               Verification verification = HttpUtils.post(test,map);
                               if (verification.getErrorServer()!=0){
                                   setInitializeCallBack(verification.getErrorServer(),initializeCallBack);
                                   return;
                               }else if (verification.getErrorCode()!=0){
                                   setInitializeCallBack(verification.getErrorCode(),initializeCallBack);
                                   return;
                               }else{
                                   //通过
                               }
                            }
                        });
                    }
                }else{
                    setInitializeCallBack(ErrorInfo.HP_ERR_METADATA,initializeCallBack);
                    return;
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                setInitializeCallBack(ErrorInfo.HP_ERR_METADATA,initializeCallBack);
                return;
            }catch (Exception e) {
                e.printStackTrace();
                setInitializeCallBack(ErrorInfo.HP_ERR_UNKNOWN,initializeCallBack);
//                LogUtils.e(""+e.toString());
            }
        }
    }

}
