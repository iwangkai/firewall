package com.firewall.phone;


import com.firewall.R;
import com.firewall.server.PhoneServer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity
{
	private EditText	mEd;
	private Button		mBn;
	private static boolean	BUTTON_STATE = true;

	private String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_main);

		mEd = (EditText) findViewById (R.id.main_ed);
		mBn = (Button) findViewById (R.id.main_bn);

		mBn.setOnClickListener (new OnClickListener () {
			@Override
			public void onClick(View v){
				
				if (BUTTON_STATE) {
					BUTTON_STATE = false;
					mEd.setEnabled (false);
					mBn.setText ("关闭防火墙");
					startMyService();
				}else {
					BUTTON_STATE = true;
					mEd.setEnabled (true);
					mBn.setText ("开启防火墙");
					stopMyService();
				}
				
			}
		});

	}

	public void startMyService(){
		String phoneNumber = mEd.getText ().toString ();
		// 数据保存到 缓存中
		Log.d (TAG, "界面输入的电话号码:" + phoneNumber);

		// 第一种:获取电话号码的方法,先把号码写入缓存,然后在service那边获取
		//PreferenceUtils.putString (getApplicationContext (), "number", phoneNumber);

		Intent intent = new Intent ();
		// 第二种:直接用 put 过去
		intent.putExtra ("number", phoneNumber); // 把电话传过去

		intent.setClass (MainActivity.this, PhoneServer.class);

		// 开启服务
		startService (intent);
	}

	public void stopMyService(){
		stopService (new Intent (MainActivity.this, PhoneServer.class));
	}
}
