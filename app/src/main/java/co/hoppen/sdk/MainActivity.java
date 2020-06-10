package co.hoppen.sdk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.hoppen.sdk.CameraActivity;
import com.hoppen.sdk.LogUtils;
import com.serenegiant.usb.camera.Size;

import java.util.List;
import java.util.Random;

public class MainActivity extends CameraActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public int findUVCTextureViewById() {
        return R.id.uvc_camera;
    }

    @Override
    public int setResolutionWidth() {
        return 640;
    }

    @Override
    public int setResolutionHeight() {
        return 480;
    }

    @Override
    public void onCaptureCallBack(Bitmap bitmap, float resistance) {

    }

    @Override
    public void onDeviceOnline() {

    }

    @Override
    public void onDeviceOffline() {

    }

}
