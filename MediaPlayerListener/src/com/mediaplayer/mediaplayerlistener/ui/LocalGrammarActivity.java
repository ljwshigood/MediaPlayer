/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.mediaplayer.mediaplayerlistener.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

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
import android.os.Environment;
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
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalGrammarEngine;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AIAuthListener;
import com.aispeech.export.listeners.AILocalGrammarListener;
import com.aispeech.export.listeners.AITTSListener;
import com.aispeech.speech.AIAuthEngine;
import com.google.gson.Gson;
import com.mediaplayer.mediaplayerlistener.R;
import com.mediaplayer.mediaplayerlistener.bean.RecogniBean;
import com.mediaplayer.mediaplayerlistener.service.WJMediaPlayerService;
import com.mediaplayer.mediaplayerlistener.utils.AppKey;
import com.mediaplayer.mediaplayerlistener.utils.Constant;
import com.mediaplayer.mediaplayerlistener.utils.GrammarHelper;
import com.mediaplayer.mediaplayerlistener.utils.NetworkUtil;
import com.mediaplayer.mediaplayerlistener.utils.SharePerfenceUtil;

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
	AIMixASREngine mAsrEngine;
	
	AIAuthEngine mAuthEngine;
	
	private Context mContext ;
	
	private AILocalTTSEngine mLocalTTSEngine;
	
	private void initEngine(String modelName) {
        if (mLocalTTSEngine != null) {
            mLocalTTSEngine.destory();
        }
        mLocalTTSEngine = AILocalTTSEngine.createInstance();//创建实例
        mLocalTTSEngine.setResource(modelName);//设置合成引擎的资源
        mLocalTTSEngine.setRealBack(true);//设置本地合成使用实时反馈
        mLocalTTSEngine.init(this, new AILocalTTSListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);//初始化合成引擎
        mLocalTTSEngine.setSpeechRate(0.25f);//设置语速
        mLocalTTSEngine.setDeviceId(Util.getIMEI(this));
    }
	
	private class AILocalTTSListenerImpl implements AITTSListener {

        @Override
        public void onInit(int status) {
            Log.i(TAG, "初始化完成，返回值：" + status);
            Log.i(TAG, "onInit");
            if (status == AIConstant.OPT_SUCCESS) {
                //tip.setText("初始化成功!");
                //btnStart.setEnabled(true);
            } else {
                //tip.setText("初始化失败!code:" + status);
            }
        }

        @Override
        public void onProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
        	//showTip("当前:" + currentTime + "ms, 总计:" + totalTime + "ms, 可信度:" + isRefTextTTSFinished);
        }

        @Override
        public void onError(String utteranceId, AIError error) {
            //tip.setText("检测到错误");
            //content.setText(content.getText() + "\nError:\n" + error.toString());
        }

        @Override
        public void onReady(String utteranceId) {
            LocalGrammarActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                   // tip.setText("开始播放");
                    Log.i(TAG, "onReady");
                }
            });
        }

        @Override
        public void onCompletion(String utteranceId) {
        	LocalGrammarActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                   // tip.setText("合成完成");
                	//finish() ;
                	if(flag == 1){
                		Message msssage = new Message() ;
           	            msssage.what = 4 ;
           	            mHandler.sendMessageDelayed(msssage, 1000) ;
                		//finish() ;
                	}
                   Log.e(TAG, "onCompletion");
                }
            });
        }
    }
	
	private int flag = 0 ;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == 0){
				initGrammarEngine();
				// 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
		        if (new File(Util.getResourceDir(LocalGrammarActivity.this) + File.separator + AILocalGrammarEngine.OUTPUT_NAME).exists()) {
		            initAsrEngine();
		            Message msssage = new Message() ;
		            msssage.what = 1 ;
		            mHandler.sendMessageDelayed(msssage, 500) ;
		        }
		        
		        Message msssage = new Message() ;
	            msssage.what = 1 ;
	            mHandler.sendMessageDelayed(msssage, 500) ;
			}else if(msg.what == 1){
				
				setResBtnEnable(false);
				setAsrBtnState(false, "识别");
				new Thread(new Runnable() {

					@Override
					public void run() {
						showInfo("开始生成资源...");
						startResGen();
					    Message msssage = new Message() ;
			            msssage.what = 2 ;
			            mHandler.sendMessageDelayed(msssage, 5000) ;
						
					}
				}).start();
			}else if(msg.what == 2){
				if ("识别".equals(bt_asr.getText())) {
					if (mAsrEngine != null) {
						setAsrBtnState(true, "停止");
						mAsrEngine.start();
					} else {
						showTip("请先生成资源");
					}
				} else if ("停止".equals(bt_asr.getText())) {
					if (mAsrEngine != null) {
						setAsrBtnState(true, "识别");
						mAsrEngine.stopRecording();
					}
				}
			}else if(msg.what == 3){
				String recoginString = (String) msg.obj ;
				Toast.makeText(mContext, recoginString, 1).show() ;
				if(Constant.RECOGINZEONE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILONE);
					flag = 1 ;
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		           // mHandler.sendMessageDelayed(msssage, 10000) ;
				}else if(Constant.RECOGINZETWO.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILTWO);
					flag = 1 ;
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            //mHandler.sendMessageDelayed(msssage, 20000) ;
				}else if(Constant.RECOGINZETHREE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILTHREE);
					flag = 1 ;
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            //mHandler.sendMessageDelayed(msssage, 5000) ;
				}else if(Constant.RECOGINZEFOUR.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFOUR);
					flag = 1 ;
					
					Message msssage = new Message() ;
		            msssage.what = 9 ;
		            mHandler.sendMessageDelayed(msssage, 2000) ;
					
				/*	Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mHandler.sendMessageDelayed(msssage, 5000) ;*/
				}else if(Constant.RECOGINZEFIVE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFIVE);
					flag = 1 ;
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            //mHandler.sendMessageDelayed(msssage, 9000) ;
				}else if(Constant.RECOGINZESIX.contains(recoginString)){
					flag = 1 ;
					Random rand = new Random();
					int randNum = rand.nextInt(4);
					playerLocalTTSEngine(mXiaoHua[randNum]);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		           // mHandler.sendMessageDelayed(msssage, 25000) ;
				}else if(Constant.RECOGINZESEVEN.equals(recoginString)){
					flag = 1 ;
					playerLocalTTSEngine(recoginString);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            //mHandler.sendMessageDelayed(msssage, 2000) ;
				}else{
					
					String fileName = recoginString ;
					Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
					intent.putExtra("control", 7) ;
					intent.putExtra("file_name", fileName) ;
					sendBroadcast(intent) ;
					
					
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mHandler.sendMessageDelayed(msssage, 1000) ;
				}
				
				//finish() ;
			}else if(msg.what == 4){
				finish() ;
			}else if(msg.what == 8){
				 android.os.Process.killProcess(android.os.Process.myPid());  
		         System.exit(1);  
			}else if(msg.what == 9){
				Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
				intent.putExtra("control", 10);
				sendBroadcast(intent);
			}
			
		};
	};
	
	private Gson mGson ;
	
	private Button mBtnTest ;
	
	private String [] mXiaoHua = new String[]{Constant.XiaoHuaOne,Constant.XiaoHuaTwo,Constant.XiaoThree,Constant.XiaoFour} ;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_grammar);
		
		/*Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
		intent.putExtra("control", 9);
		sendBroadcast(intent);*/
		
		/*int count = (Integer) SharePerfenceUtil.getParam(LocalGrammarActivity.this, "count", 0) ;
		
		if(count > 500){
			playerLocalTTSEngine("软件已经过期");
			Message msg = new Message() ;
			msg.what = 8 ;
			mHandler.sendMessageDelayed(msg, 2000) ;
		}else{
			count++ ;
			SharePerfenceUtil.setParam(LocalGrammarActivity.this, "count", count) ;
		}*/
		
		//Log.e("liujw","############################count : "+count);
		
		mContext = LocalGrammarActivity.this ;
		mGson = new Gson() ;
		mBtnTest = (Button)findViewById(R.id.btn_test) ;
		tv = (EditText) findViewById(R.id.tv);
		bt_res = (Button) findViewById(R.id.btn_gen);
		bt_asr = (Button) findViewById(R.id.btn_asr);
		bt_res.setEnabled(false);
		bt_asr.setEnabled(false);
		bt_res.setOnClickListener(this);
		bt_asr.setOnClickListener(this);
		mBtnTest.setOnClickListener(this);

        mAuthEngine = AIAuthEngine.getInstance(getApplicationContext());
//        mEngine.setResStoragePath("/system/vender/aispeech");//设置自定义路径，请将相关文件预先放到该目录下
        try {
            mAuthEngine.init(AppKey.APPKEY, AppKey.SECRETKEY,"");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mAuthEngine.setOnAuthListener(new AIAuthListener() {
			
			@Override
			public void onAuthSuccess() {
				runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                         Toast.makeText(mContext, "恭喜，已完成授权，您可以自由使用其它功能",1).show();
                    }
                });
			}
			
			@Override
			public void onAuthFailed(final String result) {
				runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                    	Toast.makeText(mContext, result,1).show();
                    }
                });
			}
		});
		
		  new Thread(){
	      	
	      	public void run() {
	      		
	      		if(!mAuthEngine.isAuthed()){
	      			mAuthEngine.doAuth() ;
	      			//mHandler.sendEmptyMessage(0) ;
	      		}/*else {
	      			mHandler.sendEmptyMessage(1) ;
	      		}*/
	      		mHandler.sendEmptyMessage(0) ;
	      	};
	      }.start() ;
        
        initEngine("zhilingf.v0.4.20s.bin") ;
        
       
	}
	
	/**
	 * 初始化资源编译引擎
	 */
	private void initGrammarEngine() {
		if (mGrammarEngine != null) {
			mGrammarEngine.destroy();
		}
		Log.i(TAG, "grammar create");
		mGrammarEngine = AILocalGrammarEngine.createInstance();
		// mGrammarEngine.setResStoragePath("/system/vender/aispeech");//设置自定义路径，请将相关文件预先放到该目录下	
		mGrammarEngine.setResFileName(Constant.ebnfc_res);
		mGrammarEngine.init(this, new AILocalGrammarListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
		mGrammarEngine.setDeviceId(Util.getIMEI(this));
	}

	/**
	 * 初始化混合引擎
	 */
	@SuppressLint("NewApi")
	private void initAsrEngine() {
		if (mAsrEngine != null) {
			mAsrEngine.destroy();
		}
		Log.i(TAG, "asr create");
		/*mAsrEngine = AIMixASREngine.createInstance();
		// mAsrEngine.setResStoragePath("/system/vender/aispeech");//设置自定义路径，请将相关文件预先放到该目录下
		mAsrEngine.setResBin(SampleConstants.ebnfr_res);
		mAsrEngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);

		mAsrEngine.setVadResource(SampleConstants.vad_res);
		mAsrEngine.setServer("ws://s-test.api.aispeech.com:10000");
		mAsrEngine.setRes("aihome");
		mAsrEngine.setUseXbnfRec(true);
		mAsrEngine.setUsePinyin(true);
		mAsrEngine.setUseForceout(false);
		mAsrEngine.setAthThreshold(0.6f);
		mAsrEngine.setIsRelyOnLocalConf(true);
		mAsrEngine.setIsPreferCloud(true);
		mAsrEngine.setLocalBetterDomains(
				new String[] { "aihomeopen", "aihomegoods", "aihomeplay", "aihomenum", "aihomenextup", "aihomehello" });
		// mAsrEngine.setCloudNotGoodAtDomains(new String[] { "phonecall",
		// "weixin" });
		// mAsrEngine.putCloudLocalDomainMap("weixin", "wechat");
		// mAsrEngine.putCloudLocalDomainMap("phonecall", "phone");
		// mAsrEngine.putCloudLocalDomainMap("kaolafm", "music");
		mAsrEngine.setWaitCloudTimeout(5000);
		mAsrEngine.setPauseTime(800);
		mAsrEngine.setUseConf(true);
		// mAsrEngine.setVersion("1.0.4"); //设置资源的版本号
		mAsrEngine.setNoSpeechTimeOut(0);
		mAsrEngine.setCloudVadEnable(false);
		mAsrEngine.init(this, new AIASRListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
		mAsrEngine.setUseCloud(true);// 该方法必须在init之后
*/	
		
		mAsrEngine = AIMixASREngine.createInstance();
		mAsrEngine.setResBin(Constant.ebnfr_res);
		mAsrEngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);

		mAsrEngine.setVadResource(Constant.vad_res);
		mAsrEngine.setServer("ws://s-test.api.aispeech.com:10000");
		mAsrEngine.setRes("aihome");
		mAsrEngine.setUseXbnfRec(true);
		mAsrEngine.setUsePinyin(true);
		mAsrEngine.setUseForceout(false);
		mAsrEngine.setAthThreshold(0.6f);
		mAsrEngine.setIsRelyOnLocalConf(true);
		mAsrEngine.setIsPreferCloud(true);
		mAsrEngine.setLocalBetterDomains(
				new String[] { "aihomeopen", "aihomegoods", "aihomeplay", "aihomenum", "aihomenextup", "aihomehello" });
		// mAsrEngine.setCloudNotGoodAtDomains(new String[] { "phonecall",
		// "weixin" });
		// mAsrEngine.putCloudLocalDomainMap("weixin", "wechat");
		// mAsrEngine.putCloudLocalDomainMap("phonecall", "phone");
		// mAsrEngine.putCloudLocalDomainMap("kaolafm", "music");
		mAsrEngine.setWaitCloudTimeout(5000);
		mAsrEngine.setPauseTime(800);
		mAsrEngine.setUseConf(true);
		mAsrEngine.setNoSpeechTimeOut(0);
		mAsrEngine.setCloudVadEnable(false);

		// 自行设置合并规则:
		// 1. 如果无云端结果,则直接返回本地结果
		// 2. 如果有云端结果,当本地结果置信度大于阈值时,返回本地结果,否则返回云端结果
		mAsrEngine.setMergeRule(new IMergeRule() {

			@Override
			public AIResult mergeResult(AIResult localResult, AIResult cloudResult) {

				AIResult result = null;
				try {
					if (cloudResult == null) {
						// 为结果增加标记,以标示来源于云端还是本地
						JSONObject localJsonObject = new JSONObject(localResult.getResultObject().toString());
						localJsonObject.put("src", "native");
						localResult.setResultObject(localJsonObject);
						result = localResult;
					} else {
						JSONObject cloudJsonObject = new JSONObject(cloudResult.getResultObject().toString());
						cloudJsonObject.put("src", "cloud");
						cloudResult.setResultObject(cloudJsonObject);
						result = cloudResult;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return result;

			}
		});
		mAsrEngine.init(this, new AIASRListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);
		mAsrEngine.setUseCloud(true);
	
	}

	/**
	 * 开始生成识别资源
	 * 
	 */

	private void startResGen() {
		// 生成ebnf语法
		GrammarHelper gh = new GrammarHelper(this);
		String contactString = gh.getConatcts();
		String appString = gh.getApps();
		// 如果手机通讯录没有联系人
		if (TextUtils.isEmpty(contactString)) {
			contactString = "无联系人";
		}
		String ebnf = gh.importAssets(contactString, appString, "grammar.xbnf");
		Log.i(TAG, ebnf);
		// 设置ebnf语法
		mGrammarEngine.setEbnf(ebnf);
		// 启动语法编译引擎，更新资源
		mGrammarEngine.update();
	}

	/**
	 * 语法编译引擎回调接口，用以接收相关事件
	 */
	public class AILocalGrammarListenerImpl implements AILocalGrammarListener {

		@Override
		public void onError(AIError error) {
			showInfo("资源生成发生错误");
			showTip(error.getError());
			setResBtnEnable(true);
		}

		@Override
		public void onUpdateCompleted(String recordId, String path) {
			showInfo("资源生成/更新成功\npath=" + path + "\n重新加载识别引擎...");
			Log.i(TAG, "资源生成/更新成功\npath=" + path + "\n重新加载识别引擎...");
			initAsrEngine();
		}

		@Override
		public void onInit(int status) {
			if (status == 0) {
				showInfo("资源定制引擎加载成功");
				if (mAsrEngine == null) {
					setResBtnEnable(true);
				}
			} else {
				showInfo("资源定制引擎加载失败");
			}
		}
	}

	private MediaPlayer mediaPlayer ;
	
	private void checkMusicAudio(int id){
		mediaPlayer = MediaPlayer.create(getBaseContext(), id);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setVolume(10f,10f);
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mediaPlayer != null) {
					mediaPlayer.release();
					mediaPlayer = null;
				}	
			}
		});
	}
	
	/**
	 * 本地识别引擎回调接口，用以接收相关事件
	 */
	public class AIASRListenerImpl implements AIASRListener {

		@Override
		public void onBeginningOfSpeech() {
			showInfo("检测到说话");
		}

		@Override
		public void onEndOfSpeech() {
			showInfo("检测到语音停止，开始识别...");
			//TODO 
			/*Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
			intent.putExtra("control", 11);
			sendBroadcast(intent);*/
			checkMusicAudio(R.raw.shibie_after);
			//playerLocalTTSEngine("检测到语音停止，开始识别...");
		}

		@Override
		public void onReadyForSpeech() {
			showInfo("请说话...");
			//TODO 加载播放音乐
			
			checkMusicAudio(R.raw.shibie_before);
			
			/*Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
			intent.putExtra("control", 10);
			sendBroadcast(intent);*/
			
			//playerLocalTTSEngine("请说话...");
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			//showTip("RmsDB = " + rmsdB);
		}

		@Override
		public void onError(AIError error) {
			showInfo("识别发生错误");
			playerLocalTTSEngine("识别发生错误");
			showTip(error.getErrId() + "");
			setAsrBtnState(true, "识别");
		}

		@Override
		public void onResults(AIResult results) {
			Log.i(TAG, results.getResultObject().toString());
			try {
				showInfo(new JSONObject(results.getResultObject().toString()).toString(4));
				String json = new JSONObject(results.getResultObject().toString()).toString(4) ;
				String recoginString = "" ;
				RecogniBean recogin = mGson.fromJson(json, RecogniBean.class) ;
				
				if(recogin != null && recogin.getResult() != null && recogin.getResult().getPost() != null && recogin.getResult().getPost().getSem() != null){
					if(recogin.getResult().getPost().getSem().getFun() != null && !recogin.getResult().getPost().getSem().getFun().equals("")){
						recoginString = recogin.getResult().getPost().getSem().getFun() ;
					}else if(recogin.getResult().getPost().getSem().getFunA() != null && !recogin.getResult().getPost().getSem().getFunA().equals("")){
						recoginString = recogin.getResult().getPost().getSem().getFunA() ;
					}else{
						recoginString = "无法识别" ;
					}
				}else{
					recoginString = "无法识别" ;
				}
				
				Message msg = new Message() ;
				msg.what = 3 ;
				msg.obj = recoginString ;
				mHandler.sendMessageDelayed(msg, 3000) ;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			setAsrBtnState(true, "识别");
		}

		@Override
		public void onInit(int status) {
			if (status == 0) {
				Log.i(TAG, "end of init asr engine");
				showInfo("本地识别引擎加载成功");
				//playerLocalTTSEngine("本地识别引擎加载成功") ;
				setResBtnEnable(true);
				setAsrBtnState(true, "识别");
				if (NetworkUtil.isWifiConnected(LocalGrammarActivity.this)) {
					if (mAsrEngine != null) {
						mAsrEngine.setNetWorkState("WIFI");
					}
				}
			} else {
				showInfo("本地识别引擎加载失败");
			}
		}

		@Override
		public void onRecorderReleased() {
			// showInfo("检测到录音机停止");
		}
	}
	

	/**
	 * 设置资源按钮的状态
	 * 
	 * @param state
	 *            使能状态
	 */
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

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mContext, str, 1).show();
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mGrammarEngine != null) {
			Log.i(TAG, "grammar cancel");
			mGrammarEngine.cancel();
		}
		if (mAsrEngine != null) {
			Log.i(TAG, "asr cancel");
			mAsrEngine.cancel();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mGrammarEngine != null) {
			Log.i(TAG, "grammar destroy");
			mGrammarEngine.destroy();
			mGrammarEngine = null;
		}
		if (mAsrEngine != null) {
			Log.i(TAG, "asr destroy");
			mAsrEngine.destroy();
			mAsrEngine = null;
		}
		
		 if (mLocalTTSEngine != null) {
            Log.i(TAG, "release in LocalTTS");
            mLocalTTSEngine.destory();
            mLocalTTSEngine = null;
	     }
		 
		 
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
	private void playerLocalTTSEngine(String string){
		  if (mLocalTTSEngine != null) {
			  mLocalTTSEngine.setSavePath(Environment.getExternalStorageDirectory() + "/linzhilin/"+ System.currentTimeMillis() + ".wav");
			  mLocalTTSEngine.speak(string, "1024");
          }
	}
	
	
	@Override
	public void onClick(View view) {
		if (view == bt_res) {
			setResBtnEnable(false);
			setAsrBtnState(false, "识别");
			new Thread(new Runnable() {

				@Override
				public void run() {
					showInfo("开始生成资源...");
					startResGen();
				}
			}).start();
		} else if (view == bt_asr) {
			if ("识别".equals(bt_asr.getText())) {
				if (mAsrEngine != null) {
					setAsrBtnState(true, "停止");
					mAsrEngine.start();
				} else {
					showTip("请先生成资源");
				}
			} else if ("停止".equals(bt_asr.getText())) {
				if (mAsrEngine != null) {
					setAsrBtnState(true, "识别");
					mAsrEngine.stopRecording();
				}
			}
		}else if(view == mBtnTest){
			playerLocalTTSEngine(Constant.XiaoFour);
		}
	}

}