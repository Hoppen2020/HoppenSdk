package com.hoppen.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.serenegiant.usb.DeviceType;
import com.serenegiant.usb.McuControl;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UsbInstructionUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 暂时关闭
 * Created by YangJianHui on 2020/6/2.
 */
public final class ProbeActivity extends AppCompatActivity implements DeviceStatusListener {

    private USBMonitor usbMonitor;
    private McuControl mcuControl;
    private UsbDevice usbDevice;

    private Timer sysPackageTimer;
    private TimerTask task;

    private McuControl.McuLoadDataListener mcuLoadDataListener = new McuControl.McuLoadDataListener() {
        @Override
        public void loadData(String data) {
            if (data.equals("System_OnLine")) {
            } else {
                //单片机监听返回
            }
        }
    };

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {

        }

        @Override
        public void onDettach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew, int deviceType) {
            if (deviceType == DeviceType.DEVICE_TYPE_MCU) {
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                mcuControl = new McuControl(usbManager, device);
                mcuControl.setMcuLoadDataListener(mcuLoadDataListener);
                boolean is =  mcuControl.openDevice(McuControl.DEVICE_TYPE_PROBE);
                if (is){
                    usbDevice = device;
                    try {
                        if (sysPackageTimer == null) {
                            sysPackageTimer = new Timer();
                            task = new TimerTask() {
                                @Override
                                public void run() {
                                    mcuSend(UsbInstructionUtils.USB_PROBE_SYS_ONLINE());
                                }
                            };
                            sysPackageTimer.schedule(task, 5000, 10000);
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (usbDevice!=null) onDeviceOnline();
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (usbDevice!=null&&usbDevice==device){
                try {
                    if (sysPackageTimer != null) {
                        sysPackageTimer.cancel();
                        sysPackageTimer = null;
                    }
                } catch (Exception e) {
                }
                if (mcuControl != null) mcuControl.closeDevice();
                usbDevice = null;
                onDeviceOffline();
            }
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerUsbMoitor();
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
        usbDevice=null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseUsbMoitor();
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

    private boolean mcuSend(byte[] order) {
        boolean success = false;
        if (mcuControl != null) {
            success = mcuControl.sendData(order);
        }
        return success;
    }


    @Override
    public void onDeviceOnline() {

    }

    @Override
    public void onDeviceOffline() {

    }
}
