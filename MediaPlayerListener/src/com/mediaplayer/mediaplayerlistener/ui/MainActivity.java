package com.mediaplayer.mediaplayerlistener.ui;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediaplayer.mediaplayerlistener.R;
import com.mediaplayer.mediaplayerlistener.service.WJMediaPlayerService;
import com.mediaplayer.mediaplayerlistener.utils.Tools;

public class MainActivity extends Activity implements OnClickListener {

	private Context mContext;

	private String getTopActivity() {
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null) {
			return (runningTaskInfos.get(0).topActivity.getClassName())
					.toString();
		} else {
			return null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
		intent.putExtra("control", 1);
		sendBroadcast(intent);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Log.e("liujw","######################onKeyDown : " + event.getKeyCode());
		if (event.getKeyCode() == 22) {// enter key
			Intent intentKey01 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey01.putExtra("control", 5);
			sendBroadcast(intentKey01);
		} else if (event.getKeyCode() == 10009) {// 语音识别键：
			Intent intentKey02 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey02.putExtra("control", 3);
			sendBroadcast(intentKey02);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = MainActivity.this;
		
		String date = Tools.getSystemTime() ;
		if(Integer.valueOf(date) > 20160731){
			 android.os.Process.killProcess(android.os.Process.myPid());  
	         System.exit(1);  
		}
		
		initView();

		Intent intentService = new Intent(mContext,WJMediaPlayerService.class);
		startService(intentService) ;
		 
	}
	
	private Button mBtnPause;

	private Button mBtnNext;

	private Button mBtnProior;

	private void initView() {
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnPause = (Button) findViewById(R.id.btn_pause);
		mBtnProior = (Button) findViewById(R.id.btn_prioro);

		mBtnPause.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
		mBtnProior.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_pause:
			Intent intentKey01 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey01.putExtra("control", 3);
			sendBroadcast(intentKey01);
			break;
		default:
			break;
		/*case R.id.btn_pause:
			Intent intentKey01 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey01.putExtra("control", 5);
			sendBroadcast(intentKey01);
			break;
		case R.id.btn_next:
			Intent intentKey02 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey02.putExtra("control", 4);
			sendBroadcast(intentKey02);
			
			Intent intentKey02 = new Intent(mContext,LocalGrammarActivity.class);
			startActivity(intentKey02) ;
			
			break;
		case R.id.btn_prioro:
			Intent intentKey03 = new Intent(WJMediaPlayerService.CTL_ACTION);
			intentKey03.putExtra("control", 6);
			sendBroadcast(intentKey03);
			break;

		default:
			break;*/
		}
	}

}
