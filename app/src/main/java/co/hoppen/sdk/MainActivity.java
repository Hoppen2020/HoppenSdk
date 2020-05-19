package co.hoppen.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import com.hoppen.sdk.CameraActivity;
import com.hoppen.sdk.LogUtils;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.DeviceType;
import com.serenegiant.usb.McuControl;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.camera.CameraHandler;
import com.serenegiant.usb.camera.widget.UVCCameraTextureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends CameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int findUVCTextureViewById() {
        return R.id.uvc_camera;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * 保存文件，文件名为当前日期
     */
    public boolean saveBitmap(Bitmap bitmap, String bitName) {
        String fileName;
        File file;
        String brand = Build.BRAND;

        if (brand.equals("xiaomi")) { // 小米手机brand.equals("xiaomi")
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else if (brand.equalsIgnoreCase("Huawei")) {
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else { // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName;
        }
//        fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        if (Build.VERSION.SDK_INT >= 29) {
//            boolean isTrue = saveSignImage(bitName, bitmap);
            saveSignImage(bitName,bitmap);
            return true;
//            file= getPrivateAlbumStorageDir(NewPeoActivity.this, bitName,brand);
//            return isTrue;
        } else {
            Log.v("saveBitmap brand", "" + brand);
            file =new File(fileName);
        }
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
// 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
// 插入图库
                if(Build.VERSION.SDK_INT >= 29){
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA,  file.getAbsolutePath());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }else{
                    MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), bitName, null);

                }

            }
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "FileNotFoundException:" + e.getMessage().toString());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("IOException", "IOException:" + e.getMessage().toString());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e("IOException", "IOException:" + e.getMessage().toString());
            e.printStackTrace();
            return false;

// 发送广播，通知刷新图库的显示

        }
//        if(Build.VERSION.SDK_INT >= 29){
//            copyPrivateToDownload(this,file.getAbsolutePath(),bitName);
//        }
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));

        return true;

    }


    public void saveSignImage(/*String filePath,*/String fileName, Bitmap bitmap) {
        try {
            //设置保存参数到ContentValues中
            ContentValues contentValues = new ContentValues();
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(MediaStore.Images.Media.DATA, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
//                ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "w");
//                FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());

                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (Exception e) {
        }
    }

//    private void setCameraConfig() {
//        uvc_camera.setAspectRatio(800d/600d);//显示长宽比
//        registerUsbMoitor();
//        cameraHandler = CameraHandler.createHandler((Activity) getView().getContext(), uvc_tv);
//
//
//    }

//    private void registerUsbMoitor(){
//        if (getView()!=null){
//            if (usbMonitor==null){
//                usbMonitor = new USBMonitor(this,onDeviceConnectListener);
//                usbMonitor.setDeviceFilter(DeviceFilter.getDeviceFilters(this, R.xml.usbdevice_filter));
//            }
//            usbMonitor.register();
//        }
//    }

//    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
//        @Override
//        public void onAttach(UsbDevice device) {
//            Log.e("OnDeviceConnectListener","onAttach");
//        }
//
//        @Override
//        public void onDettach(UsbDevice device) {
//            Log.e("OnDeviceConnectListener","onDettach");
//        }
//
//        @Override
//        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew,int deviceType) {
//            Log.e("OnDeviceConnectListener","onConnect"+"   "+deviceType);
//            if (deviceType== DeviceType.DEVICE_TYPE_MCU){
//                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//                mcuControl = new McuControl(usbManager,device);
//                mcuControl.setMcuLoadDataListener(mcuLoadDataListener);
//                mcuControl.openDevice();
//                deviceStatus[0] = true;
//                try{
//                    if (sysPackageTimer==null) {
//                        sysPackageTimer = new Timer();
//                        task = new TimerTask() {
//                            @Override
//                            public void run() {
//                                mcuSend(UsbInstructionUtils.USB_SYS_ONLINE());
//                            }
//                        };
//                        sysPackageTimer.schedule(task, 5000, 10000);
//                    }
//                }catch (Exception e){
//                }
//            }
//            if (deviceType==DeviceType.DEVICE_TYPE_CAMARE){
//                if (cameraHandler!=null){
//                    if (!cameraHandler.isCameraOpened()){
//                        int width = 800;
//                        int height = 600;
//                        if (device.getProductId()==14433&&device.getVendorId()==1423) {
//                            width = 800;
//                            height = 600;
//                        }else if (device.getProductId()==22136&&device.getVendorId()==2760){
//                            width = 1280;//1280
//                            height = 720;//720
//                        }
//                        cameraHandler.openCamera(ctrlBlock, iButtonCallback,width,height);
//                    }
//                    startPreview();
//                    deviceStatus[1] = true;
//                }
//            }
//            if (checkDevice()){
//                getView().speak(getView().getContext().getString(R.string.tts_capture_start));
//                sendMsg(captureHandler.USB_TIP_DISMISS);
//            }else{
//                sendMsg(captureHandler.USB_TIP_SHOW);
//            }
//        }
//
//        @Override
//        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
//            Log.e("OnDeviceConnectListener","onDisconnect");
//            try{
//                if (sysPackageTimer!=null){
//                    sysPackageTimer.cancel();
//                    sysPackageTimer= null;
//                }
//            }catch (Exception e){
//            }
//            if (mcuControl!=null)mcuControl.closeDevice();
////            ***
//            if (cameraHandler!=null)cameraHandler.closeCamera();
//            clearDeviceStatus();
//            getView().showFindUsbDevice();
//        }
//
//        @Override
//        public void onCancel(UsbDevice device) {
//            Log.e("OnDeviceConnectListener","onCancel");
//        }
//    };


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
