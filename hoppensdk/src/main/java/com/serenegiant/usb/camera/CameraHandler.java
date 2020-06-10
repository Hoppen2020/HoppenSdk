package com.serenegiant.usb.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.camera.widget.CameraViewInterface;

import java.lang.ref.WeakReference;
import java.util.List;

public final class CameraHandler extends Handler {
	private static final int MSG_OPEN = 0;
	private static final int MSG_CLOSE = 1;
	private static final int MSG_PREVIEW_START = 2;
	private static final int MSG_PREVIEW_STOP = 3;
	private static final int MSG_CAPTURE_STILL = 4;
	private static final int MSG_MEDIA_UPDATE = 7;
	private static final int MSG_SET_IBUTTON_CALLBACK = 8;
	private static final int MSG_RELEASE = 9;
	private static  int PREVIEW_WIDTH = 0;// 1600
	private static  int PREVIEW_HEIGHT = 0;// 1200
	private static final int PREVIEW_MODE = UVCCamera.FRAME_FORMAT_MJPEG;
	public final WeakReference<CameraThread> mWeakThread;
	public boolean destroy = false;

	public boolean isDestroy() {
		return destroy;
	}

	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	public static final CameraHandler createHandler(final Activity parent,
													final CameraViewInterface cameraView) {
		final CameraThread thread = new CameraThread(parent, cameraView);
		thread.start(); 
		return thread.getHandler();
	}

	public static final void setResolution(int width , int height){
		PREVIEW_WIDTH = width;
		PREVIEW_HEIGHT = height;
	}

	private CameraHandler(final CameraThread thread) {
		mWeakThread = new WeakReference<CameraThread>(thread);
	}

	public boolean isCameraOpened() {
		final CameraThread thread = mWeakThread.get();
		return thread != null ? thread.isCameraOpened() : false;
	}

	public List<Size> getSupportedSizeList(){
		List<Size> list = null;
		final CameraThread thread = mWeakThread.get();
		if (thread!=null){
			list=thread.getSupportedSizeList();
		}
		return list;
	}

	public void openCamera(final USBMonitor.UsbControlBlock ctrlBlock, final IButtonCallback buttonCallback) {
//		PREVIEW_WIDTH=width;
//		PREVIEW_HEIGHT = height;
		Message msg=new Message();
		msg.what=MSG_OPEN;
		Object[] objects={ctrlBlock,buttonCallback};
		msg.obj=objects;
		sendMessage(msg);
	}

	public void closeCamera() {
		stopPreview();
		sendEmptyMessage(MSG_CLOSE);
	}

	public void releaseCamera() {
		sendEmptyMessage(MSG_RELEASE);
	}

	public void startPreview(final Surface sureface) {
		if (sureface != null)
			sendMessage(obtainMessage(MSG_PREVIEW_START, sureface));
	}

	public void setButtonCallback(IButtonCallback callback) {
		Message msg = new Message();
		msg.what = MSG_SET_IBUTTON_CALLBACK;
		msg.obj = callback;
		sendMessage(msg);
	}

	public void stopPreview() {
		final CameraThread thread = mWeakThread.get();
		if (thread == null)
			return;
		synchronized (thread.mSync) {
			sendEmptyMessage(MSG_PREVIEW_STOP);
			try {
				thread.mSync.wait();
			} catch (final InterruptedException e) {
			}
		}
	}

	public void captureStill() {
		sendEmptyMessage(MSG_CAPTURE_STILL);
	}

	@Override
	public void handleMessage(final Message msg) {
		final CameraThread thread = mWeakThread.get();
		if (thread == null)
			return;
		switch (msg.what) {
		case MSG_SET_IBUTTON_CALLBACK:
			thread.setButtonCallback((IButtonCallback) msg.obj);
			break;
		case MSG_OPEN:
			Object[] objects=(Object[]) msg.obj;
			thread.handleOpen((USBMonitor.UsbControlBlock) objects[0],(IButtonCallback) objects[1]);
			break;
		case MSG_CLOSE:
			thread.handleClose();
			break;
		case MSG_PREVIEW_START:
			thread.handleStartPreview((Surface) msg.obj);
			break;
		case MSG_PREVIEW_STOP:
			thread.handleStopPreview();
			break;
		case MSG_CAPTURE_STILL:
			thread.handleCaptureStill();
			break;
		case MSG_MEDIA_UPDATE:
			break;
		case MSG_RELEASE:
			thread.handleRelease();
			break;
		default:
			throw new RuntimeException("unsupported message:what=" + msg.what);
		}
	}

	public static final class CameraThread extends Thread {
		private final Object mSync = new Object();
		private final WeakReference<Activity> mWeakParent;
		private final WeakReference<CameraViewInterface> mWeakCameraView;
		private boolean mIsRecording;
		private CameraHandler mHandler;

		private IButtonCallback buttonCallback;
		/**
		 * for accessing UVC camera
		 */
		private UVCCamera mUVCCamera;

		private CameraThread(final Activity parent,
				final CameraViewInterface cameraView) {
			super("CameraThread");
			mWeakParent = new WeakReference<Activity>(parent);
			mWeakCameraView = new WeakReference<CameraViewInterface>(cameraView);
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
		}

		public void setButtonCallback(IButtonCallback callback) {
			this.buttonCallback = callback;
		}

		public CameraHandler getHandler() {
			synchronized (mSync) {
				if (mHandler == null)
					try {
						mSync.wait();
					} catch (final InterruptedException e) {
					}
			}
			return mHandler;
		}

		public boolean isCameraOpened() {
			return mUVCCamera != null;
		}

		public void handleOpen(final USBMonitor.UsbControlBlock ctrlBlock, final IButtonCallback buttonCallback) {
			handleClose();
			mUVCCamera = new UVCCamera();
			mUVCCamera.open(ctrlBlock,PREVIEW_WIDTH,PREVIEW_HEIGHT);//,PREVIEW_WIDTH,PREVIEW_HEIGHT
			mUVCCamera.setButtonCallback(buttonCallback);
		}



		public void handleClose() {
			if (mUVCCamera != null) {
				mUVCCamera.stopPreview();
				mUVCCamera.close();
				mUVCCamera.destroy();
				mUVCCamera = null;
			}
		}

		public void handleStartPreview(final Surface surface) {
			if (mUVCCamera == null)
				return;
			try {
				mUVCCamera.setPreviewSize(PREVIEW_MODE);
			} catch (final IllegalArgumentException e) {
				try {
					// fallback to YUV mode
					mUVCCamera.setPreviewSize(
							UVCCamera.DEFAULT_PREVIEW_MODE);
				} catch (final IllegalArgumentException e1) {
					handleClose();
				}
			}
			if (mUVCCamera != null) {
				mUVCCamera.setPreviewDisplay(surface);
				mUVCCamera.startPreview();
			}
		}

		public List<Size> getSupportedSizeList(){
			if (mUVCCamera!=null){
				return mUVCCamera.getSupportedSizeList();
			}
			return null;
		}

		public void handleStopPreview() {
			if (mUVCCamera != null) {
				mUVCCamera.stopPreview();
				mUVCCamera.destroy();
			}
			synchronized (mSync) {
				mSync.notifyAll();
			}
		}

		@SuppressLint("NewApi")
		public void handleCaptureStill() {
			final Activity parent = mWeakParent.get();
			if (parent == null)
				return;
//			Message msg = Message.obtain();
//			msg.obj = parent;
//			msg.what = WaxHandler.WHAT_CAPTURE;
//			WaxApplication.getWaxApplication().getHandler().sendMessage(msg);
		}

		public void handleRelease() {
			handleClose();
			if (!mIsRecording)
				Looper.myLooper().quit();
		}

		@Override
		public void run() {
			Looper.prepare();
			synchronized (mSync) {
				mHandler = new CameraHandler(this);
				mSync.notifyAll();
			}
			Looper.loop();
			synchronized (mSync) {
				mHandler = null;
				mSync.notifyAll();
			}
		}
	}
}
