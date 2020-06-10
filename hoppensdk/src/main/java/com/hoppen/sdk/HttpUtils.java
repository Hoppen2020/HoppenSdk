package com.hoppen.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by YangJianHui on 2020/5/13.
 */
public class HttpUtils {
//    public void get(final String urlPath , final HttpResultListener httpResultListener){
//        if (threadPoolExecutor!=null){
//            threadPoolExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    Message msg = null;
//                    if (httpResultListener!=null&&httpThreadHandler!=null){
//                        Object [] obj = {httpResultListener,""};
//                        msg.obj = obj;
//                        msg.what = -1;
//                    }
//                    HttpURLConnection connection = null;
//                    InputStream inputStream = null;
//                    try {
//                        //获得URL对象
//                        URL url = new URL(urlPath);
//                        //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
//                        connection = (HttpURLConnection) url.openConnection();
//                        connection.setRequestMethod("GET");
//                        //不使用缓存
//                        connection.setUseCaches(false);
//                        //设置超时时间
//                        connection.setConnectTimeout(5000);
//                        //设置读取超时时间
//                        connection.setReadTimeout(5000);
//                        //设置是否从httpUrlConnection读入，默认情况下是true;
//                        connection.setDoInput(true);
//                        //相应码是否为200
//                        int responseCode =  connection.getResponseCode();
//                        msg.what = responseCode;
//                        if (responseCode == HttpURLConnection.HTTP_OK) {
//                            //获得输入流
//                            inputStream = connection.getInputStream();
//                            //包装字节流为字符流
//                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                            StringBuilder response = new StringBuilder();
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                response.append(line);
//                            }
//                            if (msg!=null){
//                                ((Object[])msg.obj)[1] = response.toString();
//                            }
//                        } else {
//                        }
//                    }catch (ConnectException connectException){
//
//                    }catch (UnknownHostException unknownHostException){
//
//                    }catch (SocketException socketException){
//
//                    }catch (SocketTimeoutException socketTimeoutException){
//
//                    } catch (Exception e){
//
//                    }finally {
//                        if (connection != null) {
//                            connection.disconnect();
//                        }
//                        //关闭读写流
//                        if (inputStream != null) {
//                            try {
//                                inputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    if (msg!=null){
//                        httpThreadHandler.handleMessage(msg);
//                    }
//                }
//            });
//        }
//    }

    public static Verification post(final String urlPath, final Map<String,String> params){
        Verification verification = new Verification();
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
                JSONObject jsonObject = new JSONObject(response.toString());
                int code =  jsonObject.getInt("code");
                if (code!=1000){
                    verification.setErrorCode(code);
                }else{
                    ///****************************///

                }
                verification.setData(response.toString()+"");
            } else if (responseCode>=500){
                verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_SERVER);
            }else if (responseCode>=400){
                verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_REQUEST);
            }
        }catch (ConnectException connectException){
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_CONNECT_SERVER);
        }catch (UnknownHostException unknownHostException){
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_RESOLVE_HOST);
        }catch (SocketException socketException){
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_SOCKET);
        }catch (SocketTimeoutException socketTimeoutException){
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_SOCKET);
        }catch (JSONException e) {
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_PARSING);
            e.printStackTrace();
        } catch (Exception e) {
            verification.setErrorServer(ErrorInfo.HP_ERR_NETWORK_UNKNOWN);
            e.printStackTrace();
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
        return verification;
    }

    //post请求参数
    private static StringBuffer getParamString(Map<String, String> params){
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

}
