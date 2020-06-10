package com.hoppen.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.serenegiant.usb.DeviceType;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.McuControl;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UsbInstructionUtils;
import com.serenegiant.usb.camera.CameraHandler;
import com.serenegiant.usb.camera.Size;
import com.serenegiant.usb.camera.widget.UVCCameraTextureView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YangJianHui on 2020/5/19.
 */
public abstract class CameraActivity extends AppCompatActivity implements CaptureCallBack, DeviceStatusListener {

    private final static int CAPTURE_DEFAULT_WIDTH = 640;
    private final static int CAPTURE_DEFAULT_HEIGHT = 480;

    private USBMonitor usbMonitor;
    private McuControl mcuControl;
    private UVCCameraTextureView uvc_tv;
    private CameraHandler cameraHandler;
    private Surface mSurface;
    private UsbDevice[] deviceStatus = {null, null};//0:mcu 1:camera
    private ActivityHandler activityHandler;

    private Timer sysPackageTimer;
    private TimerTask task;

    private int resolutionWidth = 0;
    private int resolutionHeight = 0;
    private int captureWidth = CAPTURE_DEFAULT_WIDTH;
    private int captureHeight = CAPTURE_DEFAULT_HEIGHT;


    private IButtonCallback iButtonCallback = new IButtonCallback() {
        @Override
        public void onButton(int button, int state) {
            if (state == 1) {
                mcuSend(UsbInstructionUtils.USB_CAMERA_CAPTURE_MODE2());
            }
        }
    };

    private McuControl.McuLoadDataListener mcuLoadDataListener = new McuControl.McuLoadDataListener() {
        @Override
        public void loadData(String data) {
            if (data.equals("System_OnLine")) {
            } else {
                if (data.contains("-")) {
                    float dataFloat = 0;
                    data = data.replace("-", ".");
                    try {
                        dataFloat = Float.parseFloat(data);
                    } catch (Exception e) {
                        dataFloat = 0;
                    }
                    if (uvc_tv != null) {
                        Bitmap captureBitmap = uvc_tv.getBitmap(captureWidth, captureHeight);
                        if (captureBitmap != null) {
                            Message msg = Message.obtain();
                            Object[] objs = new Object[2];
                            msg.obj = objs;
                            objs[0] = captureBitmap;
                            objs[1] = dataFloat;
                            if (activityHandler != null) activityHandler.handleMessage(msg);
                        }
                    }
                }
            }
        }
    };


    public List<Size> getSupportSize() {
        List<Size> list = null;
        if (cameraHandler != null) {
            list = cameraHandler.getSupportedSizeList();
        }
        return list;
    }

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
        }

        @Override
        public void onDettach(UsbDevice device) {
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew, int deviceType) {
//            LogUtils.e("@@@@@@@@@@@@@");
            if (deviceType == DeviceType.DEVICE_TYPE_MCU) {
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                mcuControl = new McuControl(usbManager, device);
                mcuControl.setMcuLoadDataListener(mcuLoadDataListener);
                boolean is =  mcuControl.openDevice(McuControl.DEVICE_TYPE_CAMERA);
//                deviceStatus[0] = true;
                if (is){
                    deviceStatus[0] = device;
                    try {
                        if (sysPackageTimer == null) {
                            sysPackageTimer = new Timer();
                            task = new TimerTask() {
                                @Override
                                public void run() {
                                    mcuSend(UsbInstructionUtils.USB_CAMERA_SYS_ONLINE());
                                }
                            };
                            sysPackageTimer.schedule(task, 5000, 10000);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (deviceType == DeviceType.DEVICE_TYPE_CAMARE) {
                if (cameraHandler != null) {
                    if (!cameraHandler.isCameraOpened()) {
                        cameraHandler.openCamera(ctrlBlock, iButtonCallback);
                    }
                    startPreview();
                    deviceStatus[1] = device;
                }
            }
            if (checkDeviceIsOk()) {
                onDeviceOnline();
            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            Log.e("OnDeviceConnectListener", "onDisconnect");
            if (deviceStatus[0]!=null&&device == deviceStatus[0]) {
                try {
                    if (sysPackageTimer != null) {
                        sysPackageTimer.cancel();
                        sysPackageTimer = null;
                    }
                } catch (Exception e) {
                }
                if (mcuControl != null) mcuControl.closeDevice();
                deviceStatus[0] = null;
            } else if (deviceStatus[1]!=null&&device == deviceStatus[1]) {
                if (cameraHandler != null) cameraHandler.closeCamera();
                deviceStatus[1] = null;
            }
            if (checkDeviceIsDown()) {
                onDeviceOffline();
            }
        }

        @Override
        public void onCancel(UsbDevice device) {
            Log.e("OnDeviceConnectListener", "onCancel");
        }
    };

    public boolean checkDeviceIsOk() {
        return deviceStatus[0] != null && deviceStatus[1] != null;
    }
    public boolean checkDeviceIsDown(){
        return deviceStatus[0] == null && deviceStatus[1] == null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public abstract int findUVCTextureViewById();

    public abstract int setResolutionWidth();

    public abstract int setResolutionHeight();

    @Override
    protected void onStart() {
        super.onStart();
        if (activityHandler == null) activityHandler = new ActivityHandler(this);
        uvc_tv = findViewById(findUVCTextureViewById());
        resolutionWidth = setResolutionWidth();
        resolutionHeight = setResolutionHeight();
        setCameraConfig();
    }

    public void setResolution(int width, int height) {
        if (cameraHandler != null) {
            if (cameraHandler != null) {
                if (width != 0 && height != 0) {
                    cameraHandler.stopPreview();
                    this.resolutionWidth = width;
                    this.resolutionHeight = height;
                    setCameraConfig();
                }
            }
        }
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
        //uvc_tv.setAspectRatio(0.5);//显示长宽比
        registerUsbMoitor();
        cameraHandler = CameraHandler.createHandler(this, uvc_tv);
        cameraHandler.setResolution(resolutionWidth, resolutionHeight);
    }

    private void registerUsbMoitor() {
        usbMonitor = new USBMonitor(this, onDeviceConnectListener);
        usbMonitor.register();
    }

    private void releaseUsbMoitor() {
        if (usbMonitor != null) {
            usbMonitor.destroy();
            usbMonitor = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (sysPackageTimer != null) {
                sysPackageTimer.cancel();
                sysPackageTimer = null;
            }
        } catch (Exception e) {
        }
        if (cameraHandler != null) {
            cameraHandler.stopPreview();
        }
         clearDeviceStatus();
    }

    private void clearDeviceStatus() {
        deviceStatus[0] = null;
        deviceStatus[1] = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraHandler != null) {
            cameraHandler.releaseCamera();
            cameraHandler = null;
        }
        releaseUsbMoitor();
        if (uvc_tv != null) uvc_tv.onPause();
    }

    private boolean mcuSend(byte[] order) {
        boolean success = false;
        if (mcuControl != null) {
            success = mcuControl.sendData(order);
        }
        return success;
    }

    public void setCaptureSize(int width, int height) {
        if (width < 0 && height < 0) {
            this.captureWidth = width;
            this.captureHeight = height;
        }
    }

    private class ActivityHandler extends Handler {
        private WeakReference<CameraActivity> weakReference;

        public ActivityHandler(CameraActivity cameraActivity) {
            weakReference = new WeakReference<>(cameraActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (weakReference.get()!=null){
                Object[] objs = (Object[]) msg.obj;
                onCaptureCallBack((Bitmap) objs[0], (float) objs[1]);
            }
        }
    }

    public boolean cameraLightForClose(){
        return mcuSend(UsbInstructionUtils.USB_CAMERA_LIGHT_CLOSE());
    }
    public boolean cameraLightForRGB(){
        return mcuSend(UsbInstructionUtils.USB_CAMERA_LIGHT_RGB());
    }
    public boolean cameraLightForPolarized(){
        return mcuSend(UsbInstructionUtils.USB_CAMERA_LIGHT_POLARIZED());
    }
    public boolean cameraLightForUV(){
        return mcuSend(UsbInstructionUtils.USB_CAMERA_LIGHT_UV());
    }
}
