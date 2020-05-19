package com.serenegiant.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;


/**
 * Created by YangJianHui on 2020/3/27.
 */
public class McuControl {

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private boolean opened = false;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint epOut, epIn;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            String decodingData =  UsbInstructionUtils.decodingUsb((byte[]) msg.obj);
            Log.e("AAA","111   "+decodingData);
            if (decodingData!=null)mcuLoadDataListener.loadData(decodingData);
        }
    };
    private Thread loadDataThread;

    private McuLoadDataListener mcuLoadDataListener;

    private Object tag;

    private Runnable loadDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                int timeout = 2000;
                while (opened){
                    byte[] data = new byte[1024];
                    int length = data.length;
                    int cnt = usbDeviceConnection.bulkTransfer(epIn, data, length, timeout);
                    if (cnt!=-1){
                        if (mcuLoadDataListener!=null){
                            Message msg = Message.obtain();
                            msg.obj = data;
                            handler.sendMessage(msg);
                            //data = new byte[1024];
                        }
                    }
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("loadDataRunnable",""+e.toString());
            }
        }
    };

    public void setMcuLoadDataListener(McuLoadDataListener mcuLoadDataListener){
        this.mcuLoadDataListener = mcuLoadDataListener;
    }

    private McuControl(){}

    public McuControl(UsbManager usbManager, UsbDevice usbDevice){
        this.usbManager = usbManager;
        this.usbDevice = usbDevice;
    }

    public boolean openDevice(){
        if (opened)return true;
        if (usbDevice==null||usbManager==null)return false;
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        if (usbDeviceConnection == null || usbDevice.getInterfaceCount() != 1)return false;
        usbInterface = usbDevice.getInterface(0); //通常第一个端口
        boolean claimInterface = usbDeviceConnection.claimInterface(usbInterface, false);//是否找到设备口
        if (!claimInterface) return false;
        SetConfig(usbDeviceConnection, 9600, (byte) 8, (byte) 1, (byte) 0, (byte) 0); //设置波特率 只限CH430

        int cnt = usbInterface.getEndpointCount();
        if (cnt < 1)return false;

        for (int index = 0; index < cnt; index++) {
            UsbEndpoint ep = usbInterface.getEndpoint(index);
            if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                    && (ep.getDirection() == UsbConstants.USB_DIR_OUT)) {
                epOut = ep;
                if (epOut == null) {
                    return false;
                }
            }
            if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                    && (ep.getDirection() == UsbConstants.USB_DIR_IN)) {
                epIn = ep;
            }
        }
        opened = true;
        loadDataThread = new Thread(loadDataRunnable);
        loadDataThread.start();
        return opened;
    }

    public void closeDevice(){
            if (usbDeviceConnection!=null){
                try {
                    opened = false;
//                    handler.removeCallbacks(readDataRunnable);
                    usbDeviceConnection.releaseInterface(usbInterface);
                    usbDeviceConnection.close();
                    usbDeviceConnection=null;
                    usbInterface=null;
                    epOut = null;
                    epIn = null;
                }catch (Exception e){
                    Log.e("closeDevice",""+e.toString());
                }
            }
    }

    public boolean sendData(byte [] data){
        boolean success = false;
        if (opened){
            if (usbDeviceConnection!=null&&epOut!=null&&data!=null){
                int timeOut = 1000;
                int i= usbDeviceConnection.bulkTransfer(epOut,data,data.length,timeOut);
                success = i>0;
            }
        }
        return  success;
    }

    public boolean isOpened() {
        return opened;
    }

    public interface McuLoadDataListener{
        void loadData(String data);
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    private static boolean SetConfig(UsbDeviceConnection connection, int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        int value = 0;
        int index = 0;
        char valueHigh = 0, valueLow = 0, indexHigh = 0, indexLow = 0;
        switch (parity) {
            case 0: /* NONE */
                valueHigh = 0x00;
                break;
            case 1: /* ODD */
                valueHigh |= 0x08;
                break;
            case 2: /* Even */
                valueHigh |= 0x18;
                break;
            case 3: /* Mark */
                valueHigh |= 0x28;
                break;
            case 4: /* Space */
                valueHigh |= 0x38;
                break;
            default: /* None */
                valueHigh = 0x00;
                break;
        }

        if (stopBit == 2) {
            valueHigh |= 0x04;
        }

        switch (dataBit) {
            case 5:
                valueHigh |= 0x00;
                break;
            case 6:
                valueHigh |= 0x01;
                break;
            case 7:
                valueHigh |= 0x02;
                break;
            case 8:
                valueHigh |= 0x03;
                break;
            default:
                valueHigh |= 0x03;
                break;
        }

        valueHigh |= 0xc0;
        valueLow = 0x9c;

        value |= valueLow;
        value |= (int) (valueHigh << 8);

        switch (baudRate) {
            case 50:
                indexLow = 0;
                indexHigh = 0x16;
                break;
            case 75:
                indexLow = 0;
                indexHigh = 0x64;
                break;
            case 110:
                indexLow = 0;
                indexHigh = 0x96;
                break;
            case 135:
                indexLow = 0;
                indexHigh = 0xa9;
                break;
            case 150:
                indexLow = 0;
                indexHigh = 0xb2;
                break;
            case 300:
                indexLow = 0;
                indexHigh = 0xd9;
                break;
            case 600:
                indexLow = 1;
                indexHigh = 0x64;
                break;
            case 1200:
                indexLow = 1;
                indexHigh = 0xb2;
                break;
            case 1800:
                indexLow = 1;
                indexHigh = 0xcc;
                break;
            case 2400:
                indexLow = 1;
                indexHigh = 0xd9;
                break;
            case 4800:
                indexLow = 2;
                indexHigh = 0x64;
                break;
            case 9600:
                indexLow = 2;
                indexHigh = 0xb2;
                break;
            case 19200:
                indexLow = 2;
                indexHigh = 0xd9;
                break;
            case 38400:
                indexLow = 3;
                indexHigh = 0x64;
                break;
            case 57600:
                indexLow = 3;
                indexHigh = 0x98;
                break;
            case 115200:
                indexLow = 3;
                indexHigh = 0xcc;
                break;
            case 230400:
                indexLow = 3;
                indexHigh = 0xe6;
                break;
            case 460800:
                indexLow = 3;
                indexHigh = 0xf3;
                break;
            case 500000:
                indexLow = 3;
                indexHigh = 0xf4;
                break;
            case 921600:
                indexLow = 7;
                indexHigh = 0xf3;
                break;
            case 1000000:
                indexLow = 3;
                indexHigh = 0xfa;
                break;
            case 2000000:
                indexLow = 3;
                indexHigh = 0xfd;
                break;
            case 3000000:
                indexLow = 3;
                indexHigh = 0xfe;
                break;
            default: // default baudRate "9600"
                indexLow = 2;
                indexHigh = 0xb2;
                break;
        }

        index |= 0x88 | indexLow;
        index |= (int) (indexHigh << 8);

        Uart_Control_Out(connection, CH340AndroidDriver.UartCmd.VENDOR_SERIAL_INIT, value, index);
        return true;
    }

    private static int Uart_Control_Out(UsbDeviceConnection connection, int request, int value, int index) {
        int retval = 0;
        retval = connection.controlTransfer(
                CH340AndroidDriver.UsbType.USB_TYPE_VENDOR | CH340AndroidDriver.UsbType.USB_RECIP_DEVICE | CH340AndroidDriver.UsbType.USB_DIR_OUT, request, value, index, null,
                0, 2000);

        return retval;
    }


}
