/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.mediaplayer.mediaplayerlistener.ui;

import java.io.File;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.IMergeRule;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalGrammarEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AIAuthListener;
import com.aispeech.export.listeners.AILocalGrammarListener;
import com.aispeech.speech.AIAuthEngine;
import com.google.gson.Gson;
import com.mediaplayer.mediaplayerlistener.R;
import com.mediaplayer.mediaplayerlistener.bean.RecogniBean;
import com.mediaplayer.mediaplayerlistener.service.WJMediaPlayerService;
import com.mediaplayer.mediaplayerlistener.utils.AppKey;
import com.mediaplayer.mediaplayerlistener.utils.Constant;
import com.mediaplayer.mediaplayerlistener.utils.GrammarHelper;
import com.mediaplayer.mediaplayerlistener.utils.NetworkUtil;

/**
 * 本示例将演示通过联合使用本地识别引擎和本地语法编译引擎实现定制识别。<br>
 * 
 * 将由本地语法编译引擎根据手机中的联系人和应用列表编译出可供本地识别引擎使用的资源，从而达到离线定制识别的功能。
 */
public class LocalGrammarActivity extends Activity implements OnClickListener {
	
	public static final String TAG = LocalGrammarActivity.class.getName();

	EditText tv;
	Button bt_res;
	Button bt_asr;
	
	AILocalGrammarEngine mGrammarEngine;
	
	//AIMixASREngine mAsrEngine;
	
	//AIAuthEngine mAuthEngine;
	
	private Context mContext ;
	
	private int flag = 0 ;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == 4){
				finish() ;
			}else if(msg.what == 8){
				 android.os.Process.killProcess(android.os.Process.myPid());  
		         System.exit(1);  
			}else if(msg.what == 9){
				Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
				intent.putExtra("control", 10);
				sendBroadcast(intent);
			}else if(msg.what == 11){
				
				Intent intentService = new Intent(mContext,WJMediaPlayerService.class);
				startService(intentService) ;
			}
			
		};
	};
	
	private Button mBtnTest ;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Toast.makeText(LocalGrammarActivity.this, "############event.getKeyCode() : "+event.getKeyCode(), 1).show() ;
		if (event.getKeyCode() == 29) {// enter key
			Intent intentKey01 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey01.putExtra("control", 5);
			sendBroadcast(intentKey01);
		} else if (event.getKeyCode() == 10009) {// 语音识别键
			
			Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
			intent.putExtra("control", 9);
			sendBroadcast(intent);
			
		} else if (event.getKeyCode() == 19) {// 播放上一曲：
			Intent intentKey03 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey03.putExtra("control", 8);
			sendBroadcast(intentKey03);
		} else if (event.getKeyCode() == 20) {// 播放下一曲
			Intent intentKey04 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey04.putExtra("control", 6);
			sendBroadcast(intentKey04);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			mHandler.sendEmptyMessage(11) ;
		}
	};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_grammar);
		mContext = LocalGrammarActivity.this ;
		
		mBtnTest = (Button)findViewById(R.id.btn_test) ;
		tv = (EditText) findViewById(R.id.tv);
		bt_res = (Button) findViewById(R.id.btn_gen);
		bt_asr = (Button) findViewById(R.id.btn_asr);
		bt_res.setEnabled(false);
		bt_asr.setEnabled(false);
		bt_res.setOnClickListener(this);
		bt_asr.setOnClickListener(this);
		mBtnTest.setOnClickListener(this);
		mHandler.postDelayed(runnable, 2000) ;
		
	}

	private void setResBtnEnable(final boolean state) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				bt_res.setEnabled(state);
			}
		});
	}

	/**
	 * 设置识别按钮的状态
	 * 
	 * @param state
	 *            使能状态
	 * @param text
	 *            按钮文本
	 */
	private void setAsrBtnState(final boolean state, final String text) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				bt_asr.setEnabled(state);
				bt_asr.setText(text);
			}
		});
	}

	private void showInfo(final String str) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				tv.setText(str);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onClick(View view) {
		if (view == bt_res) {
			setResBtnEnable(false);
			setAsrBtnState(false, "识别");
			/*new Thread(new Runnable() {

				@Override
				public void run() {
					showInfo("开始生成资源...");
					startResGen();
				}
			}).start();*/
		} else if (view == bt_asr) {
			if ("识别".equals(bt_asr.getText())) {
				
				Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
				intent.putExtra("control", 9);
				sendBroadcast(intent);
				
			} else if ("停止".equals(bt_asr.getText())) {
			}
		}else if(view == mBtnTest){
			//playerLocalTTSEngine(Constant.XiaoHuaOne);
			Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
			intent.putExtra("control", 9);
			sendBroadcast(intent);
		}
	}

}