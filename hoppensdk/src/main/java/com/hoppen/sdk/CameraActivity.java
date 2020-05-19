package com.hoppen.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.DeviceType;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.McuControl;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UsbInstructionUtils;
import com.serenegiant.usb.camera.CameraHandler;
import com.serenegiant.usb.camera.widget.UVCCameraTextureView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YangJianHui on 2020/5/19.
 */
public abstract class CameraActivity extends AppCompatActivity {

    private USBMonitor usbMonitor;
    private McuControl mcuControl;
    private UVCCameraTextureView uvc_tv;
    private CameraHandler cameraHandler;
    private Surface mSurface;
    private boolean [] deviceStatus={false,false};//0:mcu 1:camera

    private Timer sysPackageTimer;
    private TimerTask task;


    private IButtonCallback iButtonCallback = new IButtonCallback() {
        @Override
        public void onButton(int button, int state) {
            LogUtils.e("IButtonCallback "+state);
            if (state==1){
                mcuSend(UsbInstructionUtils.USB_CAPTURE_MODE2());
            }
        }
    };

    private McuControl.McuLoadDataListener mcuLoadDataListener = new McuControl.McuLoadDataListener() {
        @Override
        public void loadData(String data) {
            if (data.equals("System_OnLine")){
            }else {
//                if (!captureFinish){
                    if (data.contains("-")){
                        float dataFloat = 0;
                        data=data.replace("-",".");
                        try {
                            dataFloat= Float.parseFloat(data);
                        }catch (Exception e){
                            dataFloat=0;
                        }
                        //sendMsg(captureHandler.USB_CAPTURE,dataFloat);
                    }
//                }
            }
        }
    };

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {}

        @Override
        public void onDettach(UsbDevice device) {}

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew,int deviceType) {
            if (deviceType== DeviceType.DEVICE_TYPE_MCU){
                UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
                mcuControl = new McuControl(usbManager,device);
                mcuControl.setMcuLoadDataListener(mcuLoadDataListener);
                mcuControl.openDevice();
                deviceStatus[0] = true;
                try{
                    if (sysPackageTimer==null) {
                        sysPackageTimer = new Timer();
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                mcuSend(UsbInstructionUtils.USB_SYS_ONLINE());
                            }
                        };
                        sysPackageTimer.schedule(task, 5000, 10000);
                    }
                }catch (Exception e){
                }
            }
            if (deviceType==DeviceType.DEVICE_TYPE_CAMARE){
                if (cameraHandler!=null){
                    if (!cameraHandler.isCameraOpened()){
                        int width = 800;
                        int height = 600;
                        if (device.getProductId()==14433&&device.getVendorId()==1423) {
                            width = 800;
                            height = 600;
                        }else if (device.getProductId()==22136&&device.getVendorId()==2760){
                            width = 1280;//1280
                            height = 720;//720
                        }
                        cameraHandler.openCamera(ctrlBlock, iButtonCallback,width,height);
                    }
                    startPreview();
                    deviceStatus[1] = true;
                }
            }
//            if (checkDevice()){
//                getView().speak(getView().getContext().getString(R.string.tts_capture_start));
//                sendMsg(captureHandler.USB_TIP_DISMISS);
//            }else{
//                sendMsg(captureHandler.USB_TIP_SHOW);
//            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            Log.e("OnDeviceConnectListener","onDisconnect");
            try{
                if (sysPackageTimer!=null){
                    sysPackageTimer.cancel();
                    sysPackageTimer= null;
                }
            }catch (Exception e){
            }
            if (mcuControl!=null)mcuControl.closeDevice();
//            ***
            if (cameraHandler!=null)cameraHandler.closeCamera();
//            clearDeviceStatus();
//            getView().showFindUsbDevice();
        }

        @Override
        public void onCancel(UsbDevice device) {
            Log.e("OnDeviceConnectListener","onCancel");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uvc_tv = findViewById(findUVCTextureViewById());
    }

    public abstract int findUVCTextureViewById();

    @Override
    protected void onStart() {
        super.onStart();
        setCameraConfig();
    }

    private void startPreview() {
        SurfaceTexture st = null;
        if (uvc_tv != null) {
            uvc_tv.onResume();
            st = uvc_tv.getSurfaceTexture();
        }
        if (mSurface != null) {
            mSurface.release();
        }
        if (st != null) {
            mSurface = new Surface(st);
            cameraHandler.startPreview(mSurface);
        }
    }

    private void setCameraConfig() {
        uvc_tv.setAspectRatio(800d/600d);//显示长宽比
        registerUsbMoitor();
        cameraHandler = CameraHandler.createHandler(this, uvc_tv);
    }

    private void registerUsbMoitor(){
        if (usbMonitor==null){
                usbMonitor = new USBMonitor(this,onDeviceConnectListener);
                usbMonitor.setDeviceFilter(DeviceFilter.getDeviceFilters(this, R.xml.usbdevice_filter));
            }
            usbMonitor.register();
    }

    private void releaseUsbMoitor(){
            if (usbMonitor!=null){
                //destroy中已包含unregister
//                usbMonitor.unregister();
                usbMonitor.destroy();
                usbMonitor = null;
            }
    }

    @Override
    protected void onStop() {
        super.onStop();
            try{
                if (sysPackageTimer!=null){
                    sysPackageTimer.cancel();
                    sysPackageTimer= null;
                }
            }catch (Exception e){
            }
            if (cameraHandler != null) {
                cameraHandler.stopPreview();
            }
           // clearDeviceStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraHandler != null) {
            cameraHandler.releaseCamera();
            cameraHandler = null;
        }
        releaseUsbMoitor();
        if (uvc_tv != null)uvc_tv.onPause();
    }

    public boolean mcuSend(byte[] order){
        boolean success = false;
        if (mcuControl!=null){
            success = mcuControl.sendData(order);
        }
        return success;
    }


}
