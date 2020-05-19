package com.serenegiant.usb;

public interface IButtonCallback {

    public static final int ACTION_DOWN=1;
    public static final int ACTION_UP=0;

    void onButton(int button, int state);
}
