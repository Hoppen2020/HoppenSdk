package com.hoppen.sdk;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by YangJianHui on 2020/5/13.
 */
public class AsynHttpUtils {
    private volatile static AsynHttpUtils instance = null;
    private final int coreThreadPoolSize = 5;
    private final int maxThreadPoolSize = 5;
    private final long keepAliveTime = 1; //minute
    private final ThreadPoolExecutor threadPoolExecutor;
    private final HttpThreadHandler httpThreadHandler;

    public interface HttpResultListener{
        void onSuccess(String result);
        void onFailed(int code, String errorMessage);
    }

    public void get(final String urlPath , final HttpResultListener httpResultListener){
        if (threadPoolExecutor!=null){
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Message msg = null;
                    if (httpResultListener!=null&&httpThreadHandler!=null){
                        Object [] obj = {httpResultListener,""};
                        msg.obj = obj;
                        msg.what = -1;
                    }
                    HttpURLConnection connection = null;
                    InputStream inputStream = null;
                    try {
                        //获得URL对象
                        URL url = new URL(urlPath);
                        //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        //不使用缓存
                        connection.setUseCaches(false);
                        //设置超时时间
                        connection.setConnectTimeout(5000);
                        //设置读取超时时间
                        connection.setReadTimeout(5000);
                        //设置是否从httpUrlConnection读入，默认情况下是true;
                        connection.setDoInput(true);
                        //相应码是否为200
                        int responseCode =  connection.getResponseCode();
                        msg.what = responseCode;
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            //获得输入流
                            inputStream = connection.getInputStream();
                            //包装字节流为字符流
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            if (msg!=null){
                                ((Object[])msg.obj)[1] = response.toString();
                            }
                        } else {
                        }
                    }catch (Exception e){
                        if (msg!=null){
                            ((Object[])msg.obj)[1] = e.getMessage();
                            msg.what = -1;
                        }
                    }finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        //关闭读写流
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (msg!=null){
                        httpThreadHandler.handleMessage(msg);
                    }
                }
            });
        }
    }

    public void post(final String urlPath, final Map<String,String> params, final HttpResultListener httpResultListener){
        if (threadPoolExecutor!=null){
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Message msg = null;
                    if (httpResultListener!=null&&httpThreadHandler!=null){
                        Object [] obj = {httpResultListener,""};
                        msg.obj = obj;
                        msg.what = -1;
                    }

                    HttpURLConnection connection = null;
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    StringBuffer body = getParamString(params);
                    byte[] data = body.toString().getBytes();
                    try {
                        //获得URL对象
                        URL url = new URL(urlPath);
                        //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
                        connection = (HttpURLConnection) url.openConnection();
                        // 默认为GET
                        connection.setRequestMethod("POST");
                        //不使用缓存
                        connection.setUseCaches(false);
                        //设置超时时间
                        connection.setConnectTimeout(5000);
                        //设置读取超时时间
                        connection.setReadTimeout(5000);
                        //设置是否从httpUrlConnection读入，默认情况下是true;
                        connection.setDoInput(true);
                        //设置为true后才能写入参数
                        connection.setDoOutput(true);
                        //post请求需要设置标头
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Charset", "UTF-8");
                        //表单参数类型标头
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        //获取写入流
                        outputStream=connection.getOutputStream();
                        //写入表单参数
                        outputStream.write(data);
                        //相应码是否为200

                        int responseCode =  connection.getResponseCode();
                        msg.what = responseCode;
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            //获得输入流
                            inputStream = connection.getInputStream();
                            //包装字节流为字符流
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            if (msg!=null){
                                ((Object[])msg.obj)[1] = response.toString();
                            }
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (msg!=null){
                            ((Object[])msg.obj)[1] = e.getMessage();
                            msg.what = -1;
                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        //关闭读写流
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (msg!=null){
                        httpThreadHandler.handleMessage(msg);
                    }
                }
            });
        }
    }

    //post请求参数
    private StringBuffer getParamString(Map<String, String> params){
        StringBuffer result = new StringBuffer();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> param = iterator.next();
            String key = param.getKey();
            String value = param.getValue();
            result.append(key).append('=').append(value);
            if (iterator.hasNext()){
                result.append('&');
            }
        }
        return result;
    }


    private AsynHttpUtils(){
        threadPoolExecutor = new ThreadPoolExecutor(coreThreadPoolSize,
                maxThreadPoolSize,
                keepAliveTime,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>()
                );
        httpThreadHandler = new HttpThreadHandler(this);
    }

    public static AsynHttpUtils getInstance(){
        if (instance==null){
            synchronized (AsynHttpUtils.class){
                if (instance==null){
                    instance = new AsynHttpUtils();
                }
            }
        }
        return instance;
    }

    public class HttpThreadHandler extends Handler {
        WeakReference<AsynHttpUtils> weakReference;
        public HttpThreadHandler(AsynHttpUtils asynHttpUtils){
            weakReference = new WeakReference<>(asynHttpUtils);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (weakReference.get()!=null){
                try {
                    Object[] obj = (Object[]) msg.obj;
                    HttpResultListener httpResultListener = (HttpResultListener) obj[0];
                    String resul = (String) obj[1];
                    int code = msg.what;
                    if (code==HttpURLConnection.HTTP_OK){
                        httpResultListener.onSuccess(resul);
                    }else {
                        httpResultListener.onFailed(code,resul);
                    }
                }catch (Exception e){
                }
            }
        }
    }

}
