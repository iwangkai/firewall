package com.firewall.server;

import java.lang.reflect.Method;
import com.android.internal.telephony.ITelephony;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneServer extends Service
{
	private TelephonyManager	mTm;
	private String				TAG	= "PhoneServer";
	private String				phoneNumber;

	@Override
	public void onCreate(){
		Log.d (TAG, "服务开启");
		super.onCreate ();
		// 缓存的方法把号码弄过来
		// 也可以用数据库等形式把数据搞过来
		// phoneNumber = PreferenceUtils.getString (getApplicationContext (),
		// "number");

		Log.d (TAG, "缓存中的电话号码:" + phoneNumber);
		// // 搞一个电话管理者
		mTm = (TelephonyManager) getSystemService (Context.TELEPHONY_SERVICE);

		// 1.拦截电话监听
		mTm.listen (mListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private PhoneStateListener mListener = new PhoneStateListener () {
		public void onCallStateChanged(int state , final String incomingNumber){
			//Log.d (TAG, "电话状态改变!!!");
			// state：电话的状态
			// * @see TelephonyManager#CALL_STATE_IDLE:闲置状态
			// * @see TelephonyManager#CALL_STATE_RINGING:响铃状态
			// * @see TelephonyManager#CALL_STATE_OFFHOOK:摘机--》接听状态

			// incomingNumber：拨入的电话号码 对比 你在界面上面输入的电话

			//Log.d (TAG, "incomingNumber-->" + phoneNumber);
			//Log.d (TAG, "setNumber-->" + phoneNumber);

			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:

					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Log.d (TAG, "响铃状态!!!");
					// 响铃状态--> 判断是否是黑名单---》挂掉电话
					if (incomingNumber.equals (phoneNumber)) {
						// 需要拦截
						// 挂掉电话
						try {
							Class<?> clazz = Class
									.forName ("android.os.ServiceManager");
							Method method = clazz.getDeclaredMethod ("getService",
									String.class);
							IBinder binder = (IBinder) method.invoke (null,
									Context.TELEPHONY_SERVICE);
							ITelephony telephony = ITelephony.Stub.asInterface (binder);

							telephony.endCall (); // 挂断电话阻止电话响
							Thread.sleep (200); // 稍微延时

							// 删除通话记录
							final ContentResolver cr = getContentResolver ();
							final Uri url = Uri.parse ("content://call_log/calls");

							cr.registerContentObserver (url, true,
									new ContentObserver (new Handler ()) {

								public void onChange(boolean selfChange){

									String where = "number=?";
									String[] selectionArgs = new String[] { incomingNumber };
									cr.delete (url, where, selectionArgs);
								};
							});
							
							Log.d (TAG, "成功拦截电话成功-----拦截的号码为----->:" + phoneNumber);
							Toast toast=Toast.makeText(getApplicationContext(), "成功拦截电话成功-----拦截的号码为----->:" + phoneNumber, Toast.LENGTH_SHORT);  
							//显示toast信息  
							toast.show(); 
						} catch (Exception e) {
							Log.d (TAG, "拦截出错!!!");
							e.printStackTrace ();
						}
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:

					break;
				default:
					break;
			}
		}
	};

	public void onDestroy(){
		Log.d (TAG, "解除服务");
		// 注销监听
		mTm.listen (mListener, PhoneStateListener.LISTEN_NONE);
	};

	// 在启动的时候把号码保存到内存
	@Override
	public int onStartCommand(Intent intent , int flags , int startId){

		phoneNumber = intent.getStringExtra ("number");

		Log.d (TAG, "intent电话号码是:" + phoneNumber);

		return super.onStartCommand (intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent){

		return null;
	}

}
