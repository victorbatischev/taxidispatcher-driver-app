package com.psdevelop.tdandrapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

public class ServiceDialog extends Activity {
	
	public static int SERVICE_DLG_TEST=0;
	public static int ORDER_TIME_CONFIRM_DLG=1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent=getIntent();
		String text = "Тестовый диалог";
		int type=-1;
		if(intent.hasExtra("text")) text = intent.getStringExtra("text");
		if(intent.hasExtra("type")) type = intent.getIntExtra("type",-1);
		//Bundle bndl;
		//bndl.
		switch(type)	{
		default:
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Alert");
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setMessage(text);
		alert.setPositiveButton(android.R.string.ok,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ServiceDialog.this.finish();
					}
				});
		alert.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				ServiceDialog.this.finish();
			}
		});
		alert.show();
		}
		
	}
}
