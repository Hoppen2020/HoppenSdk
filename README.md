# HoppenSdk
## 准备工作

step 1 ：添加依赖，ndk配置可根据需求调整
```
defaultconifg{
	...
 	ndk {
            abiFilters "armeabi", "armeabi-v7a", "x86" 
        }
}

```
```
allprojects {
    repositories {
	       ...
	       maven { url 'https://jitpack.io' }
		 }
	}

```
```
dependencies {
            implementation 'com.github.Hoppen2020:HoppenSdk:1.0.0'
	     }
```
step 2 ：添加权限
```
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> 
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET"/>
```
step 3 ：填写申请的**key**在项目中
```
<application>
    <meta-data android:name="com.hoppen.sdk.key" android:value="your key" />
</application>
```
step 4 : 建议在Application中初始化hoppensdk，不需监听初始化回调的**InitializeCallBack**可以为**null**
```
  public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HoppenSDK.initialize(this, new InitializeCallBack() {
            @Override
            public void onInitializeCallBack(int statu) {
                if(statu==ErrorInfo.HP_OK){
			//do something
		}
            }
        });
    }
}	

```  
## API调用关键代码
### 摄像头部分： 
在使用的activity中继承**CameraAcitvity**，重写几个重要的方法即可  

**findUVCTextureViewById**用来显示摄像头画面的控件**UVCCameraTextureView**  

**setResolutionWidth**、**setResolutionHeight**，填入初始分辨率的宽高（默认是0为最低分辨率，仅可以使用当前设备中支持的分辨率，获取当前设备支持的分辨率可以调用**getSupportSize**，使用**setResolution**来设置需要的分辨率）  

**onCaptureCallBack**，截图的回调，**resistance**为电阻值，**0**为没接触皮肤（默认截图大小为**640*480**，可以在截图前调用**setCaptureSize**来设置截图大小）

**onDeviceOnline**、**onDeviceOffline**，设备在线和掉线的回调提醒

设置灯光：  
调用**cameraLightForClose**：关闭灯光  
调用**cameraLightForRGB**：RGB灯光  
调用**cameraLightForPolarized**：偏振灯关  
调用**cameraLightForUV**：UV灯关  


```
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
```  
### 功能头部分： 
在使用的activity中继承**ProbeAcitvity**  

**onDeviceOnline**、**onDeviceOffline**，设备在线和掉线的回调提醒


### 初始化错误信息  
|错误码名|错误代码|错误信息说明|
|---|:---:|---|
|HP_NOT_INITIALIZED|-1|未进行初始化|
|HP_OK|0|初始化成功|
|HP_ERR_UNKNOWN|3001|未知错误|
|HP_ERR_INVALID_PARAM|3002|无效参数|
|HP_ERR_METADATA|3003|metadata配置错误|
|HP_ERR_INVALID_SDKKEY|3004|sdk key 错误|
|HP_ERR_NETWORK_SERVER|3005|http：500类型|
|HP_ERR_NETWORK_REQUEST|3006|http：400类型|
|HP_ERR_NETWORK_CONNECT_SERVER|3007|http：连接错误|
|HP_ERR_NETWORK_RESOLVE_HOST|3008|http：未知主机异常|
|HP_ERR_NETWORK_SOCKET|3009|http：socket异常|
|HP_ERR_NETWORK_UNKNOWN|3010|http：未知错误|
|HP_ERR_NETWORK_PARSING|3011|http：内容解析错误|

