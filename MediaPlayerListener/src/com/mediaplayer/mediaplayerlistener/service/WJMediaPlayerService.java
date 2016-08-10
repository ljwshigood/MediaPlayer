package com.mediaplayer.mediaplayerlistener.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.mediaplayer.mediaplayerlistener.R;
import com.mediaplayer.mediaplayerlistener.ui.LocalGrammarActivity;
import com.mediaplayer.mediaplayerlistener.utils.FileUtils;

public class WJMediaPlayerService extends Service {

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private Context mContext;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private List<File> mMusicFileList = new ArrayList<File>();

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if (mMusicFileList != null && !mMusicFileList.isEmpty()) {
				playMusic(mMusicFileList.get(0).getAbsolutePath());
			}
		};
	};

	private int mCurrentPlayerPositon;

	private void playMusic(String path) {
		
		try {
			
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
			mMediaPlayer.start();

			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {

					if (mCurrentPlayerPositon == mMusicFileList.size() - 1) {
						mCurrentPlayerPositon = 0;
					} else {
						mCurrentPlayerPositon++;
					}
					
					playMusic(mMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath());
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void playShiBieMusicBefore() {
		
		 try {
		      AssetFileDescriptor file = mContext.getResources().openRawResourceFd(R.raw.shibie_before);
		      try {
		    	  mMediaPlayer.reset() ;
		    	  mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
		      } finally {
		        file.close();
		      }
		      mMediaPlayer.prepare();
		  	  mMediaPlayer.start();
		    } catch (IOException ioe) {
		      Log.w(TAG, ioe);
		      mMediaPlayer.release();
		    }
	}
	private void playShiBieMusicAfter() {
		
		 try {
		      AssetFileDescriptor file = mContext.getResources().openRawResourceFd(R.raw.shibie_after);
		      try {
		    	  mMediaPlayer.reset() ;
		    	  mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
		      } finally {
		        file.close();
		      }
		      mMediaPlayer.prepare();
		  	  mMediaPlayer.start();
		    } catch (IOException ioe) {
		      Log.w(TAG, ioe);
		      mMediaPlayer.release();
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
		if (mMusicFileList == null || mMusicFileList.size() == 0) {
			return false;
		}
		return true;
	}
	
	private void previousMusic() {
		if (!checkMusicItem()) {
			return;
		}
		mCurrentPlayerPositon = mCurrentPlayerPositon - 1 < 0 ? mMusicFileList.size() - 1 : mCurrentPlayerPositon - 1 ;
		String filePath = mMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath();
		Log.e("liujw","#################previousMusic #filePath : "+filePath);
		playMusic(filePath);	
	}
	
	private void nextMusic() {
		mCurrentPlayerPositon = (mCurrentPlayerPositon + 1) % mMusicFileList.size();
		String filePath = mMusicFileList.get(mCurrentPlayerPositon).getAbsolutePath();
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
			case 7:
				String fileName = intent.getStringExtra("file_name");
				File file = FileUtils.getInstance().findFileByName(mContext, mMusicFileList, fileName) ;
				Log.e("liujw","######################find music : ");
				if(file != null){
					mCurrentPlayerPositon = FileUtils.getInstance().currentPosition(mContext, mMusicFileList, fileName) ;
					playMusic(file.getAbsolutePath()) ;		
				}
				
				break ;
			case 8:
				Log.e("liujw","######################mMediaPlayer previousMusic: ");
				previousMusic();
				break ;
			case 9:
				pauseMusic() ;
				break ;
			case 10:
				Random rand = new Random();
				int randNum = rand.nextInt(mMusicFileList.size());
				playMusic(mMusicFileList.get(randNum).getAbsolutePath()) ;		
				break ;
			/*case 10:
				playShiBieMusicBefore() ;
				break ;
			case 11:
				playShiBieMusicAfter() ;
				break ;*/
			}
		}
	}
	
	

	public static final String CTL_ACTION = "com.android.iwit.IWITARTIS.CTL_ACTION";

	private mMusicReceiver mMusicServiceReceiver;

	private MediaPlayer mMediaPlayer;

	private AudioManager mAudioManager;

	@Override
	public void onCreate() {
		super.onCreate();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMusicServiceReceiver = new mMusicReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		registerReceiver(mMusicServiceReceiver, filter);
		mContext = WJMediaPlayerService.this;

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		new Thread() {
			public void run() {
				mMusicFileList = FileUtils.getInstance().getAllFiles(
						new File(Environment.getExternalStorageDirectory()
								+ File.separator + "BSDMusic"));
			/*	mMusicFileList = FileUtils.getInstance().getAllFiles(
						new File(Environment.getExternalStorageDirectory()
								+ File.separator + "/KuwoMusic/music"));*/
				mHandler.sendEmptyMessage(0);
			};

		}.start();

	}

	private final static String TAG = "WJMediaPlayerService";

}
