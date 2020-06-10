package com.serenegiant.usb;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Administrator on 2018/4/4.
 */

public class UsbInstructionUtils {
    /**
     *  指令：产品码
     * @return
     */
    public static byte[] USB_CAMERA_PRODUCT_CODE() {
        byte [] data = {(byte) 0xAA, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：截图
     * @return
     */
    public static byte[] USB_CAMERA_CAPTURE_MODE2() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }
    /**
     * 指令：rgb灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_RGB() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x03, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }
    /**
     * 指令：偏振灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_POLARIZED() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x04, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：uv灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_UV() {
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x05, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：关闭灯关
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_CLOSE() {
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x07, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：心跳包
     * @return
     */
    public static byte[] USB_CAMERA_SYS_ONLINE(){
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

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


    public static byte[] USB_PROBE_PRODUCT_CODE(){
        byte[] data= {(byte) 0xAA, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    public static byte[] USB_PROBE_SYS_ONLINE(){
        byte[] data= {(byte) 0xAA, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    public static byte[] USB_PROBE_START(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x10, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_PROBE_STOP(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = (byte)(time/256);
        byte mtime_2 = (byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x11, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
    }

    public static byte[] USB_PROBE_SET(String typeName){
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

    public static byte[] USB_PROBE_ENTER_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_PROBE_CLOSE_RATE(){
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x03, (byte) 0x00, (byte) 0x00,  (byte) 0x00};
        return encryption(config);
    }

    public static byte[] USB_PROBE_SET_RATE(int data){
        int a = (data >>8 );
        int b = (data & 0xff);
        byte[] config={(byte) 0xAA, (byte) 0x13, (byte) 0x02, (byte) 0x00, (byte) a, (byte) b};
        return encryption(config);
    }



    public static byte[] USB_PROBE_SINGLE_SET(int modePosition,int strength,int time){
        byte mode = 0;
        byte mStrength=(byte)strength;
        byte mtime_1 = time==0?(byte)0:(byte)(time/256);
        byte mtime_2 = time==0?(byte)0:(byte)(time%256);
        mode = (byte) modePosition;
        byte[] config={(byte) 0xAA, (byte) 0x12, mode, mStrength, mtime_1,  mtime_2};
        return encryption(config);
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
