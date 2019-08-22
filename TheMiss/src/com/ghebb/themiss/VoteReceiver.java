package com.ghebb.themiss;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ghebb.themiss.common.Constants;
import com.ghebb.themiss.common.NotificationService;

public class VoteReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationService.getInstance().postNotification(Constants.Notification_NotificationReceived, null);
		
//		Bundle bundle = intent.getExtras();
//		JSONObject json;
//		try {
//			json = new JSONObject(bundle.getString("com.parse.Data"));
//			if(json != null && json.getString("intent").equals("NewUserSignup"))
//				NotificationService.getInstance().postNotification(Constants.Notification_NotificationSignup, null);			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
} 