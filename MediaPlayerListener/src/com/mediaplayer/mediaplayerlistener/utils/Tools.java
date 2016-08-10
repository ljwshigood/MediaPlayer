/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.mediaplayer.mediaplayerlistener.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Tools {

	public static String getSystemTime() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(date);
	}
	
	/**
	 * 过滤掉非英文字符
	 * @param refText
	 * @return
	 */
	public static String enFormat(String refText) {
		if(refText != null){
			refText = refText.replaceAll("[^\u0020-\u007e]", "");
		}
		return refText;
	}
	
    /**
     * 返回当前Wifi是否连接上
     * @param context
     * @return true 已连接
     */
    public static boolean isWifiConnected(Context context){
    	ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMan.getActiveNetworkInfo();
		if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI){
			return true;
		}
		return false;
    }


}
