package com.hoppen.sdk;

/**
 * Created by YangJianHui on 2020/5/22.
 */
public class Verification {
    private long serial;
    private String data="";
    private int errorServer = 0;
    private int errorCode = 0;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getErrorServer() {
        return errorServer;
    }

    public void setErrorServer(int errorServer) {
        this.errorServer = errorServer;
        LogUtils.e("Verification setError: "+ errorServer);
    }

    @Override
    public String toString() {
        return "Verification{" +
                "serial=" + serial +
                ", data='" + data + '\'' +
                ", error=" + errorServer +
                '}';
    }
}
