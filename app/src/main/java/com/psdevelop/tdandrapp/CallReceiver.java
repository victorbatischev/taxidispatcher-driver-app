package com.psdevelop.tdandrapp;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;

public class CallReceiver extends BroadcastReceiver {
	
	private ITelephony telephonyService;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Log.v(TAG, "Receving....");
		  if ((ConnectionActivity.timerProcessing()&&ConnectionActivity.TAXOMETR_INCCALL_ABORT)
				  ||(GpsLocationDetector.TAXOMETR_INCCALL_ABORT&&GpsLocationDetector.timerProcessing()))	{
			  if (intent.getAction().equals("android.intent.action.PHONE_STATE"))	{
	          String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
	          if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
	                    //телефон звонит, получаем входящий номер
			  TelephonyManager telephony = (TelephonyManager) 
					  context.getSystemService(Context.TELEPHONY_SERVICE);  
			  try {
				  Class c = Class.forName(telephony.getClass().getName());
				  Method m = c.getDeclaredMethod("getITelephony");
				  m.setAccessible(true);
				  telephonyService = (ITelephony) m.invoke(telephony);
				  //telephonyService.silenceRinger();
				  telephonyService.endCall();
				  Toast.makeText(context, 
			    			"Прерван входящий звонок по причине активности таксометра!", Toast.LENGTH_LONG).show();
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
	          }
			  }
		  }
		
	}

}
