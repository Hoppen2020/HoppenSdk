package com.serenegiant.usb;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Administrator on 2018/4/4.
 */

public class UsbInstructionUtils {
    private final static byte[] USB_APP_VER = {(byte) 0xAA, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private final static byte[] USB_CAPTURE_MODE2 = {(byte) 0xAA, (byte) 0x10, (byte) 0x02, (byte) 0x00, (byte) 0x00};//获取水分值截图
    private final static byte[] USB_LIGHT_RGB = {(byte) 0xAA, (byte) 0x10, (byte) 0x03, (byte) 0x00, (byte) 0x00};
    private final static byte[] USB_LIGHT_POLARIZED = {(byte) 0xAA, (byte) 0x10, (byte) 0x04, (byte) 0x00, (byte) 0x00};
    private final static byte[] USB_LIGHT_UV = {(byte) 0xAA, (byte) 0x10, (byte) 0x05, (byte) 0x00, (byte) 0x00};
    private final static byte[] USB_LIGHT_CLOSE = {(byte) 0xAA, (byte) 0x10, (byte) 0x07, (byte) 0x00, (byte) 0x00};
    private final static byte[] USB_SYS_ONLINE = {(byte) 0xAA, (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x00};


    private static byte[] encryption(byte[] data) {
        byte[] returnData = new byte[data.length + 1];
        try {
            byte a = 0;
            for (int i = 0; i < data.length; i++) {
                returnData[i] =data[i];
                if (i!=0){
                    a ^=data[i];
                }
            }
            returnData[data.length] = a;
        } catch (Exception e) {
        }
        return returnData;
    }

    public static byte[] USB_CAPTURE_MODE2() {
        return encryption(USB_CAPTURE_MODE2);
    }
    public static byte[] USB_LIGHT_RGB() {
        return encryption(USB_LIGHT_RGB);
    }

    public static byte[] USB_LIGHT_POLARIZED() {
        return encryption(USB_LIGHT_POLARIZED);
    }

    public static byte[] USB_LIGHT_UV() {
        return encryption(USB_LIGHT_UV);
    }

    public static byte[] USB_LIGHT_CLOSE() {
        return encryption(USB_LIGHT_CLOSE);
    }

    public static byte[] USB_SYS_ONLINE(){
        return encryption(USB_SYS_ONLINE);
    }

    public static byte[] USB_DEVICE_CODE(){
        byte[] linePackage= {(byte) 0xAA, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(linePackage);
    }

    public static byte[] USB_LINE_PACKAGE(){
        byte[] linePackage= {(byte) 0xAA, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(linePackage);
    }

    public static byte[] USB_HANDLE_START(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x10, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_HANDLE_STOP(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x11, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_HANDLE_SET(String typeName){
        byte handle=0;
        if (typeName.equals("WSKT001")){
            handle =(byte) 0x01;
        }else if (typeName.equals("WSKT002")){
            handle =(byte)0x0A;
        }else if (typeName.equals("WSKT003")){
            handle =(byte)0x05;
        }else if (typeName.equals("WSKT004")){
            handle =(byte)0x03;
        }else if (typeName.equals("WSKT005")){
            handle =(byte)0x0F;
        }else if (typeName.equals("WSKT006")){
            handle =(byte)0x04;
        }else if (typeName.equals("WSKT007")){
            handle =(byte)0x06;
        }else if (typeName.equals("WSKT008")){
            handle =(byte)0x07;
        }else if (typeName.equals("WSKT009")){
            handle =(byte)0x11;
        }else if (typeName.equals("WSKT010")){
            handle =(byte)0x09;
        }else if (typeName.equals("WSKT011")){
            handle =(byte)0x0B;
        }else if (typeName.equals("WSKT012")){
            handle =(byte)0x10;
        }else if (typeName.equals("WSKT013")){
            handle =(byte)0x02;
        }else if (typeName.equals("WSKT014")){
            handle =(byte)0x0C;
        }else if (typeName.equals("WSKT015")){
            handle =(byte)0x0D;
        }else if (typeName.equals("WSKT016")){
            handle =(byte)0x0E;
        }else if (typeName.equals("WSKT017")){
            handle =(byte)0x08;
        }
        byte[] config={(byte) 0xAA, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00,  handle};
        return encryption(config);
    }

    public static byte[] USB_ENTER_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_CLOSE_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x03, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_SET_RATE(int data){
        int a = (data >>8 );
        int b = (data & 0xff);
        Log.e("DDDD",""+(byte)a+"   "+(byte)b);
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x02, (byte) 0x00, (byte) a, (byte) b};
        return encryption(config);
    }



    public static byte[] USB_HANDLE_SINGLE_SET(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = time==0?(byte)0:(byte)(time/256);
        byte mtime_2 = time==0?(byte)0:(byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x12, mode, mStrength, mtime_1,  mtime_2};
        byte[] newConfig=encryption(config);
        Log.e("XXX",""+ Arrays.toString(newConfig));
        return encryption(config);
    }
    public static byte[] test(){
        return encryption(USB_APP_VER);
    }

    public static String decodingUsb(byte [] data) {
        String StringData=null;
        try {
            StringData = new String(data);
            StringData = StringData.substring(StringData.indexOf("<[") + 2, StringData.lastIndexOf("]>")).trim();
        }catch (Exception e){
            StringData=null;
        }
        return StringData;
    }

}
