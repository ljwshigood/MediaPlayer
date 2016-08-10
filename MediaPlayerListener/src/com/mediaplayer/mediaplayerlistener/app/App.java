package com.mediaplayer.mediaplayerlistener.app;

import com.baidu.batsdk.BatSDK;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		BatSDK.init(this, "4c3a600be25fde57");
		BatSDK.setCollectScreenshot(true);
		BatSDK.setSendPrivacyInformation(true) ;
	}
	
}
