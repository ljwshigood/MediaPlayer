package com.mediaplayer.mediaplayerlistener.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mediaplayer.mediaplayerlistener.service.WJMediaPlayerService;

public class BootupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "boot complete ", 1).show() ;
		/*Intent intentService = new Intent(context,WJMediaPlayerService.class);
		context.startService(intentService) ;*/
	}

}
