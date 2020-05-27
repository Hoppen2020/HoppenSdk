# HoppenSdk
## Usage

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
step 3 ：填写申请的key在项目中
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

