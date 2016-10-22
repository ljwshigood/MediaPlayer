package com.mediaplayer.mediaplayerlistener.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import com.mediaplayer.mediaplayerlistener.ui.LocalGrammarActivity;
import com.mediaplayer.mediaplayerlistener.utils.AppKey;
import com.mediaplayer.mediaplayerlistener.utils.Constant;
import com.mediaplayer.mediaplayerlistener.utils.FileUtils;
import com.mediaplayer.mediaplayerlistener.utils.GrammarHelper;

public class WJMediaPlayerService extends Service {

	
	private AIAuthEngine mAuthEngine ;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		
		
		if (mLocalTTSEngine != null) {
            Log.i(TAG, "release in LocalTTS");
            mLocalTTSEngine.destroy() ;
            mLocalTTSEngine = null;
	     }
		
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
		 
	}

	private Context mContext;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private List<File> mNativeMusicFileList = new ArrayList<File>();
	
	private List<File> mRecoginMusicFileList = new ArrayList<File>();
	
	private List<File> mSingleMusicFileList = new ArrayList<File>() ;

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if(msg.what == 0){
				playMusic(mNativeMusicFileList.get(0).getAbsolutePath()) ;
			}else if(msg.what == 9){
				Random rand = new Random();
				int randNum = rand.nextInt(mNativeMusicFileList.size());
				playMusic(mNativeMusicFileList.get(randNum).getAbsolutePath()) ;	
			}else if(msg.what == 4){
				resumeMusic() ;
			}
					
		};
	};
	

	private int mCurrentPlayerPositon;

	private void playMusic(String path) {
		
		try {
			Log.e("liujw","#############path : "+path);
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
			mMediaPlayer.start();

			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {

					if (mCurrentPlayerPositon == mNativeMusicFileList.size() - 1) {
						mCurrentPlayerPositon = 0;
					} else {
						mCurrentPlayerPositon++;
					}
					
					playMusic(mNativeMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath());
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void creaseStreamVolume() {
		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume + 5, 0);
	}

	private void decreaseStreamVolume() {
		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume + 5, 0);
	}

	private void resumeMusic() {
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}

	private void pauseMusic() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}
	

	public boolean checkMusicItem() {
		if (mNativeMusicFileList == null || mNativeMusicFileList.size() == 0) {
			return false;
		}
		return true;
	}
	
	private void previousMusic() {
		if (!checkMusicItem()) {
			return;
		}
		mCurrentPlayerPositon = mCurrentPlayerPositon - 1 < 0 ? mNativeMusicFileList.size() - 1 : mCurrentPlayerPositon - 1 ;
		String filePath = mNativeMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath();
		Log.e("liujw","#################previousMusic #filePath : "+filePath);
		playMusic(filePath);	
	}
	
	private void nextMusic() {
		mCurrentPlayerPositon = (mCurrentPlayerPositon + 1) % mNativeMusicFileList.size();
		String filePath = mNativeMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath();
		Log.e("liujw","#################nextMusic #filePath : "+filePath);
		playMusic(filePath);
	}
	
	private String getTopActivity(){
	     ActivityManager manager = (ActivityManager)mContext.getSystemService(ACTIVITY_SERVICE) ;
	     List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
	     if(runningTaskInfos != null){
	       return (runningTaskInfos.get(0).topActivity.getClassName()).toString() ;
	     } else{
	       return null ; 
	     }
	}
	
	private String [] mXiaoHua = new String[]{Constant.XiaoHuaOne,Constant.XiaoHuaTwo,Constant.XiaoThree,Constant.XiaoFour} ;
	
	public class mMusicReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {

			int control = intent.getIntExtra("control", -1);
			switch (control) {
			case 0:
				decreaseStreamVolume();
				break ;
			case 1:
				resumeMusic() ;
				Log.e("liujw","##################### resumeMusic: ");
				break;
			case 2:
				creaseStreamVolume();
				break;
			case 3:
				if(!getTopActivity().equals("com.mediaplayer.mediaplayerlistener.ui.LocalGrammarActivity")){
					Intent intentGrammer = new Intent(mContext,LocalGrammarActivity.class);
					Log.e("liujw","#####################start LocalGrammarActivity: ");
					intentGrammer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
					mContext.startActivity(intentGrammer) ;
				}
				break;
			case 4:
				
				break;
			case 5:
				if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
					Log.e("liujw","######################mMediaPlayer pause: ");
					mMediaPlayer.pause();
				}else{
					Log.e("liujw","######################mMediaPlayer start: ");
					mMediaPlayer.start();
				}
				
				break;
			case 6:
				Log.e("liujw","######################mMediaPlayer nextMusic: ");
				nextMusic() ;
				break;
			case 8:
				Log.e("liujw","######################mMediaPlayer previousMusic: ");
				previousMusic();
				break ;
			case 9:
				pauseMusic() ;
				if (mLocalTTSEngine != null) {
					mLocalTTSEngine.stop() ;
				}
				
				if (mAsrEngine != null) {
					//setAsrBtnState(true, "停止");
					mAsrEngine.start();
				} else {
					//showTip("请先生成资源");
				}
				
				
				break ;
			case 10:
				Random rand = new Random();
				int randNum = rand.nextInt(mNativeMusicFileList.size());
				playMusic(mNativeMusicFileList.get(randNum).getAbsolutePath()) ;		
				break ;
			case 11 :
				
				Random rand1 = new Random();
				String recoginString = intent.getStringExtra("string");
				Toast.makeText(mContext, recoginString, 1).show() ;
				if(Constant.SINGER.contains(recoginString)){
					playMusic(mSingleMusicFileList.get(rand1.nextInt(mSingleMusicFileList.size())).getAbsolutePath()) ;		
				}else if(Constant.RECOGINZEONE.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILONE);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZEONE_1.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILONE);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZETWO.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILTWO);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZETWO_1.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILTWO);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZETWO_2.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILTWO);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZETHREE.contains(recoginString)){
					mFlag = 1 ;
					playerLocalTTSEngine(Constant.DETAILTHREE);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZEFOUR.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFOUR);
					
					Message msssage = new Message() ;
		            msssage.what = 9 ;
		            mHandler.sendMessageDelayed(msssage, 2000) ;
					
				}else if(Constant.RECOGINZEFOUR_ONE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFOUR);
					
					Message msssage = new Message() ;
		            msssage.what = 9 ;
		            mHandler.sendMessageDelayed(msssage, 2000) ;
				}else if(Constant.RECOGINZEFOUR_TWO.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFOUR);
					Message msssage = new Message() ;
		            msssage.what = 9 ;
		            mHandler.sendMessageDelayed(msssage, 2000) ;
				
				}else if(Constant.RECOGINZEFOUR_THREE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFOUR);
					Message msssage = new Message() ;
		            msssage.what = 9 ;
		            mHandler.sendMessageDelayed(msssage, 2000) ;
				
				}else if(Constant.RECOGINZEFIVE.contains(recoginString)){
					playerLocalTTSEngine(Constant.DETAILFIVE);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mFlag = 1 ;
				}else if(Constant.RECOGINZESIX.contains(recoginString)){
					playerLocalTTSEngine(mXiaoHua[rand1.nextInt(4)]);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
				}else if(Constant.RECOGINZESIX_ONE.contains(recoginString)){
					playerLocalTTSEngine(mXiaoHua[rand1.nextInt(4)]);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mFlag = 1 ;
				}else if(Constant.RECOGINZESIX_TWO.contains(recoginString)){
					playerLocalTTSEngine(mXiaoHua[rand1.nextInt(4)]);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mFlag = 1 ;
				}else if(Constant.RECOGINZESIX_THREE.contains(recoginString)){
					playerLocalTTSEngine(mXiaoHua[rand1.nextInt(4)]);
					Message msssage = new Message() ;
		            msssage.what = 4 ;
		            mFlag = 1 ;
				}else if(Constant.RECOGINZESEVEN.equals(recoginString)){
					playerLocalTTSEngine(recoginString);
					resumeMusic() ;
				}else {
					File file = FileUtils.getInstance().findFileByName(mContext, mRecoginMusicFileList, recoginString) ;
					if(file != null){
						FileUtils.getInstance().currentPosition(mContext, mRecoginMusicFileList, recoginString) ;
						playMusic(file.getAbsolutePath()) ;		
						handler.post(updateSeekbar) ;
					}else{
						resumeMusic() ;
					}
				}	
			}
		}
	}
	
	public int getMusicDuration() {
		return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
	}
	
	private Handler handler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			if(msg.what == 0){
				playMusic(mNativeMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath()) ;
				handler.removeCallbacks(updateSeekbar) ;	
			}else{
				Random rand = new Random();
				int randNum = rand.nextInt(mNativeMusicFileList.size());
				playMusic(mNativeMusicFileList.get(randNum).getAbsolutePath()) ;	
			}
			
		};
	};
	
	Runnable updateSeekbar = new Runnable() {

		@Override
		public void run() {
			if (mMediaPlayer == null) {
				return;
			}
			
			if(getMusicDuration() / 1000 >= 30){
				handler.sendEmptyMessage(0) ;
			}
			
			handler.postDelayed(updateSeekbar, 1000);
		}
	};
	

	public static final String CTL_ACTION = "com.android.iwit.IWITARTIS.CTL_ACTION";

	private mMusicReceiver mMusicServiceReceiver;

	private MediaPlayer mMediaPlayer;

	private AudioManager mAudioManager;

	private AILocalTTSEngine mLocalTTSEngine ;
	
	private int mFlag = 0 ;
	
	private class AILocalTTSListenerImpl implements AITTSListener {

        @Override
        public void onInit(int status) {
            Log.i(TAG, "初始化完成，返回值：" + status);
            Log.i(TAG, "onInit");
            if (status == AIConstant.OPT_SUCCESS) {
                //tip.setText("初始化成功!");
                //btnStart.setEnabled(true);
            	Log.e("liujw","##################初始化成功! ");
            } else {
            	Log.e("liujw","##################初始化失败!code: ");
            	
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
           /*LocalGrammarActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                   // tip.setText("开始播放");
                    Log.i(TAG, "onReady");
                }
            });*/
        }

        @Override
        public void onCompletion(String utteranceId) {
        	
        	if(mFlag == 1){
        		Message msssage = new Message() ;
    	        msssage.what = 4 ;
    	        mHandler.sendMessageDelayed(msssage, 1000) ;
        	}
        
        	/*WJMediaPlayerService.this.runOnUiThread(new Runnable() {

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
            });*/
        }
    }
	
	private void playerLocalTTSEngine(String string){
		  if (mLocalTTSEngine != null) {
			  mLocalTTSEngine.setSavePath(Environment.getExternalStorageDirectory() + "/linzhilin/"+ System.currentTimeMillis() + ".wav");
			  mLocalTTSEngine.speak(string, "1024");
        }
	}
	
	private void initEngine(String modelName) {
		
		/*private void initEngine() {*/
	        if (mLocalTTSEngine != null) {
	        	mLocalTTSEngine.destroy();
	        }
	        mLocalTTSEngine = AILocalTTSEngine.createInstance();//创建实例
	        mLocalTTSEngine.setResource("zhilingf.v0.5.5.bin");
	        mLocalTTSEngine.setDictDbName("aitts_sent_dict_v3.5.db");
	        mLocalTTSEngine.setRealBack(true);//设置本地合成使用实时反馈
	        mLocalTTSEngine.init(this, new AILocalTTSListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);//初始化合成引擎
	        mLocalTTSEngine.setSpeechRate(0.25f);//设置语速
	    /*}*/
		
      /*  if (mLocalTTSEngine != null) {
        	mLocalTTSEngine.destroy() ;
        }
        mLocalTTSEngine = AILocalTTSEngine.createInstance();//创建实例
        mLocalTTSEngine.setResource(modelName);//设置合成引擎的资源
        mLocalTTSEngine.setRealBack(true);//设置本地合成使用实时反馈
        mLocalTTSEngine.init(this, new AILocalTTSListenerImpl(), AppKey.APPKEY, AppKey.SECRETKEY);//初始化合成引擎
        mLocalTTSEngine.setSpeechRate(0.25f);//设置语速
        mLocalTTSEngine.setDeviceId(Util.getIMEI(this));*/
    }

	private AILocalGrammarEngine mGrammarEngine ;
	
	private AIMixASREngine mAsrEngine;
	
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
		mAsrEngine.setPauseTime(2000);
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
			//showInfo("检测到说话");
		}

		@Override
		public void onEndOfSpeech() {
			//showInfo("检测到语音停止，开始识别...");
			checkMusicAudio(R.raw.shibie_after);
		}

		@Override
		public void onReadyForSpeech() {
			//showInfo("请说话...");
			checkMusicAudio(R.raw.shibie_before);
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			//showTip("RmsDB = " + rmsdB);
		}

		@Override
		public void onError(AIError error) {
			//showInfo("识别发生错误");
			//showTip(error.getErrId() + "");
			//setAsrBtnState(true, "识别");
		}

		@Override
		public void onResults(AIResult results) {
			Log.i(TAG, results.getResultObject().toString());
			try {
				//showInfo(new JSONObject(results.getResultObject().toString()).toString(4));
				String json = new JSONObject(results.getResultObject().toString()).toString(4) ;
				String recoginString = "" ;
				RecogniBean recogin = mGson.fromJson(json, RecogniBean.class) ;
				
				if(recogin != null && recogin.getResult() != null && recogin.getResult().getConf() < 0.3){
					
					Log.e("liujw","################################recogin.getResult().getConf() : "+recogin.getResult().getConf());
					recoginString = "无法识别" ;
				}else{	
				
					if(recogin != null && recogin.getResult() != null && recogin.getResult().getPost() != null && recogin.getResult().getPost().getSem() != null){
						
						if(recogin.getResult().getPost().getSem().getFun() != null && !recogin.getResult().getPost().getSem().getFun().equals("")){
							recoginString = recogin.getResult().getPost().getSem().getFun() ;
						}else if(recogin.getResult().getPost().getSem().getFunA() != null && !recogin.getResult().getPost().getSem().getFunA().equals("")){
							recoginString = recogin.getResult().getPost().getSem().getFunA() ;
						}else if(recogin.getResult().getPost().getSem().getFunB() != null && !recogin.getResult().getPost().getSem().getFunB().equals("")){
							recoginString = recogin.getResult().getPost().getSem().getFunB() ;
						}else {
							recoginString = "无法识别" ;
						}
					}else{
						recoginString = "无法识别" ;
					}
				}
				
				Intent intent = new Intent(WJMediaPlayerService.CTL_ACTION);
				intent.putExtra("control", 11) ;
				intent.putExtra("string", recoginString) ;
				sendBroadcast(intent) ;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onInit(int status) {
			if (status == 0) {
				Log.i(TAG, "end of init asr engine");
				//showInfo("本地识别引擎加载成功");
				//playerLocalTTSEngine("本地识别引擎加载成功") ;
			//	setResBtnEnable(true);
				//setAsrBtnState(true, "识别");
				/*if (NetworkUtil.isWifiConnected(mContext)) {
					if (mAsrEngine != null) {
						mAsrEngine.setNetWorkState("WIFI");
					}
				}*/
			} else {
				//showInfo("本地识别引擎加载失败");
			}
		}

		@Override
		public void onRecorderReleased() {
			// showInfo("检测到录音机停止");
		}

		@Override
		public void onBufferReceived(byte[] arg0) {
			
		}
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
	 * 语法编译引擎回调接口，用以接收相关事件
	 */
	public class AILocalGrammarListenerImpl implements AILocalGrammarListener {

		@Override
		public void onError(AIError error) {
			Toast.makeText(mContext, "资源生成发生错误", 1).show();
			//showTip(error.getError());
			//setResBtnEnable(true);
		}

		@Override
		public void onUpdateCompleted(String recordId, String path) {
			//showInfo("资源生成/更新成功\npath=" + path + "\n重新加载识别引擎...");
			//Log.i(TAG, "资源生成/更新成功\npath=" + path + "\n重新加载识别引擎...");
			initAsrEngine();
		}

		@Override
		public void onInit(int status) {
			if (status == 0) {
				
				Toast.makeText(mContext, "资源定制引擎加载成功",1).show();
				//showInfo("资源定制引擎加载成功");
				//if (mAsrEngine == null) {
					//setResBtnEnable(true);
				//}
			} else {
				Toast.makeText(mContext, "资源定制引擎加载失败",1).show();
				//showInfo("资源定制引擎加载失败");
			}
		}
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
	
	private Gson mGson ;
	
	private Handler mAuthorHandler  = new Handler(){
		
		public void handleMessage(Message msg) {
			
			if(!mAuthEngine.isAuthed()){
	 			mAuthEngine.doAuth() ;
	 		}else{
	 			Toast.makeText(mContext, "此设备已经授权通过，请放心使用", 1).show() ;
	 			mAuthorHandler.removeCallbacks(mRunnablAuthor) ;
	 			mProcessHandler.sendEmptyMessage(0);
	 		}
			
		};
	} ;
	
	private Handler mProcessHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			 initGrammarEngine();
				// 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
			 if (new File(Util.getResourceDir(mContext) + File.separator + AILocalGrammarEngine.OUTPUT_NAME).exists()) {
	           initAsrEngine();
	           Message msssage = new Message() ;
	           msssage.what = 2 ;
	           mHandler.sendMessageDelayed(msssage, 500) ;
			 }else{
	    	   new Thread(new Runnable() {

	   			@Override
	   			public void run() {
	   				//showInfo("开始生成资源...");
	   				//Toast.makeText(mContext, "开始生成资源...",1).show();
	   				startResGen();
	   				
	   			}
	   			}).start();
			 }
			 
			initEngine("zhilingf.v0.4.20s.bin") ;
			
		};
		
	} ;
	
	
	private Runnable mRunnablAuthor = new Runnable() {
		
		@Override
		public void run() {
			
			Log.e("liujw","##################mRunnablAuthor...");
			
			mAuthorHandler.sendEmptyMessage(0) ;
			
			mAuthorHandler.postDelayed(this, 2000) ;
		}
	};
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMusicServiceReceiver = new mMusicReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		registerReceiver(mMusicServiceReceiver, filter);
		mContext = WJMediaPlayerService.this;

		mGson = new Gson() ;
		
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		
	   mAuthEngine = AIAuthEngine.getInstance(getApplicationContext());
	   
       try {
           mAuthEngine.init(AppKey.APPKEY, AppKey.SECRETKEY,"");
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }

       mAuthEngine.setOnAuthListener(new AIAuthListener() {
			
			@Override
			public void onAuthSuccess() {
				
				Toast.makeText(mContext, "恭喜，已完成授权，您可以自由使用其它功能",1).show();
				mAuthorHandler.removeCallbacks(mRunnablAuthor) ;
				mProcessHandler.sendEmptyMessage(0) ;
			}
			
			@Override
			public void onAuthFailed(final String result) {
				
			  	Toast.makeText(mContext, result,1).show();
				
			}
		});
       
       mAuthorHandler.post(mRunnablAuthor) ;
       
		
      /* if(!mAuthEngine.isAuthed()){
 			mAuthEngine.doAuth() ;
 		}
		*/
       
      /* initGrammarEngine();
		// 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
       if (new File(Util.getResourceDir(mContext) + File.separator + AILocalGrammarEngine.OUTPUT_NAME).exists()) {
           initAsrEngine();
           Message msssage = new Message() ;
           msssage.what = 2 ;
           mHandler.sendMessageDelayed(msssage, 500) ;
       }else{
    	   new Thread(new Runnable() {

   			@Override
   			public void run() {
   				//showInfo("开始生成资源...");
   				//Toast.makeText(mContext, "开始生成资源...",1).show();
   				Log.e("liujw","##################开始生成资源...");
   				startResGen();
   				
   			}
   		}).start();
       }*/

       new Thread() {
			public void run() {
				//小娟-雨中的故事.ape 小娟-竹舞.flac
				mSingleMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/小娟-红布绿花朵.mp3")) ;
				mSingleMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/小娟-君不见.mp3")) ;
				mSingleMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/小娟-南海姑娘.flac")) ;
				mSingleMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/小娟-雨中的故事.ape")) ;
				mSingleMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/小娟-竹舞.flac")) ;
				
				mNativeMusicFileList.add(new File(Environment.getExternalStorageDirectory() + File.separator + "/BangStarMusic/One Dance.mp3")) ;
				ArrayList<File> fileList = FileUtils.getInstance().getAllFiles(new File(Environment.getExternalStorageDirectory() + File.separator + "BangStarMusic"));
				mNativeMusicFileList.addAll(fileList) ;
				mRecoginMusicFileList = FileUtils.getInstance().getAllFiles(new File(Environment.getExternalStorageDirectory() + File.separator + "BSDMusic"));
				mHandler.sendEmptyMessage(0);
			};

		}.start();
		
		/*initEngine("zhilingf.v0.4.20s.bin") ;*/
		
	}

	private final static String TAG = "WJMediaPlayerService";

}
