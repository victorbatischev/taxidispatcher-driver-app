package com.psdevelop.tdandrapp;

import java.util.Vector;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {
	
	EditText loginEdit;
	EditText pswEdit;
	TextView settInfo;
	boolean showListMenus;
	Button buttonSettings, buttonEnter;
	private LocationManager myManager;
	Vector<NearConnection> NCons;
	SharedPreferences prefs;

	@Override
	public void onLocationChanged(Location location) {
		try {
			double lldif = 2;
			if (NCons != null && prefs!=null) {
				String ip1 = prefs.getString("IP1", "");
				if(ip1==null || ip1.length()==0 || ( ip1.length()>0 && ip1.equals("0.0.0.0"))) {
					for (int i = 0; i < NCons.size(); i++) {
						if (Math.abs(location.getLatitude() - NCons.get(i).lat) <= lldif &&
								Math.abs(location.getLongitude() - NCons.get(i).lon) <= lldif) {

							myManager.removeUpdates(this);

							AlertDialog.Builder dec_builder = new AlertDialog.Builder(this);
							final int nci = i;

							dec_builder.setTitle("Найден ближайший населенный пункт " + NCons.get(i).name)
									.setMessage("У вас пустое значение адреса сервера, установить?")
											// кнопка "Yes", при нажатии на которую приложение закроется
									.setPositiveButton("Да",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int whichButton) {
													try {
														SharedPreferences.Editor edt = prefs.edit();
														edt.putString("IP1", NCons.get(nci).ip);
														edt.commit();
													} catch (Exception cex) {
														showMsg("Ошибка!");
													}

												}
											}).setNegativeButton("Отмена",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {

										}
									}).show();

							return;
						}
					}
				}
				showMsg("Не найден ближайший адрес подключения! "+
						location.getLatitude()+":"+location.getLongitude());
			}
			myManager.removeUpdates(this);
		}catch (Exception e){
			showMsg("GPS STOP: "+e.getMessage());
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		// Если пользователь разрешил локацию или с его согласия система включила
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	protected void onStart() {
		super.onStart();
		prefs = PreferenceManager.
				getDefaultSharedPreferences(this);

		loginEdit.setText(prefs.getString("USER_LOGIN", ""));
		pswEdit.setText(prefs.getString("USER_PSW", ""));
		settInfo = (TextView)findViewById(R.id.settInfo);
		loginEdit.setText(prefs.getString("USER_LOGIN", ""));
		pswEdit.setText(prefs.getString("USER_PSW", ""));
		if(prefs.getString("USER_LOGIN", "").length()<=0||
				prefs.getString("USER_PSW", "").length()<=0)	{
			settInfo.setText("Пустые логин или пароль, установите их в параметрах: " +
					"кнопка 'Настройки'!");
		}
		else settInfo.setText("");
	}

	public void showMsg(String msg){
		Toast toastMSG = Toast.
				makeText(getApplicationContext(),
						"ConnActivity Текст сообщения: "
								+msg+".", Toast.LENGTH_LONG);
		toastMSG.show();
	}

	public void startGPS(){
		if(myManager!=null){
			myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					5000, 0, this);
			myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					5000, 0, this);
		}
	}

	public String getIpPref(){
		if(prefs!=null){
			return prefs.getString("IP1", "");
		}else{
			return null;
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		NCons = new Vector<NearConnection>();
		NCons.add(new NearConnection("Такси Осакаровка",50.564808, 72.568424, "2.74.198.185"));
		NCons.add(new NearConnection("Такси Лыткарино Онлайн",55.577856, 37.903470, "online.taxilyt.ru"));
        
        final SharedPreferences prefs = PreferenceManager.
        		getDefaultSharedPreferences(this); 
        //boolean val = prefs.getBoolean("", false);
        //http://85.175.227.245:8080/gprmc/Data?acct=demo
        //&dev=test01&gprmc=$GPRMC,082320,A,3128.7540,N,14257.6714,W,000.0,000.0,210911,,*1
        try	{
        
        setContentView(R.layout.activity_main);
        loginEdit = (EditText)findViewById(R.id.Login);
        pswEdit = (EditText)findViewById(R.id.PasswordEdit);
		settInfo = (TextView)findViewById(R.id.settInfo);
        
        loginEdit.setText(prefs.getString("USER_LOGIN", ""));
        pswEdit.setText(prefs.getString("USER_PSW", ""));
        showListMenus = prefs.getBoolean("SHOW_LIST_MENUS", true);
		if(prefs.getString("USER_LOGIN", "").length()<=0||
				prefs.getString("USER_PSW", "").length()<=0)	{
			settInfo.setText("Пустые логин или пароль, установите их в параметрах: " +
					"кнопка 'Настройки'!");
		}
		else settInfo.setText("");

		try{
			myManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

		}catch(Exception e){
			showMsg("GPS START: "+e.getMessage());
		}
        
        //enterListView = (ListView)findViewById(R.id.listViewOnLineMenu);
        
        buttonEnter = (Button)findViewById(R.id.buttonEnter);
        
        buttonEnter.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Intent connActivity = new Intent(getBaseContext(),
                        ConnectionActivity.class);

            	try	{
					String ip1 = getIpPref();
					if(ip1==null || ip1.length()==0 || ( ip1.length()>0 && ip1.equals("0.0.0.0"))) {
						startGPS();
						showMsg("Поиск ближайшего адреса подключения!");
					}
					else {
						if(loginEdit.getText().toString().length()==0||pswEdit.getText().toString().length()==0){
							showMsg("Не установлен логин или пароль, зайдите в настройки!");
						}else
						startActivity(connActivity);
					}
            	} catch (Exception e) {
                    //e.printStackTrace();
                	Toast toastErrorStartActivitySMS = Toast.
                			makeText(getApplicationContext(), 
                			"Ошибка старта connActivity! Текст сообщения: "
                			+e.getMessage()+".", Toast.LENGTH_LONG);
                	toastErrorStartActivitySMS.show();
                }
        	}
    	});
        
        buttonSettings = (Button)findViewById(R.id.buttonSettings);
        
        buttonSettings.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Intent settingsActivity = new Intent(getBaseContext(),
                        Prefs.class);
            	try	{
            		startActivity(settingsActivity);
            	} catch (Exception e) {
                    //e.printStackTrace();
                	Toast toastErrorStartActivitySMS = Toast.
                			makeText(getApplicationContext(), 
                			"Ошибка вывода настроек! Текст сообщения: "
                			+e.getMessage()+".", Toast.LENGTH_LONG);
                	toastErrorStartActivitySMS.show();
                }
        	}
    	});
        
        /*enterListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                  // When clicked, show a toast with the TextView text
                  //Toast.makeText(getApplicationContext(), 
                	//	  Integer.toString(position)+". "+
                	//	  ((TextView) view).getText() ,
                	//	  Toast.LENGTH_SHORT).show();
                  switch(position)	{
                  	case 0:
                  		Intent connActivity = new Intent(getBaseContext(),
                                ConnectionActivity.class);
                    	try	{
                    		startActivity(connActivity);	
                    	} catch (Exception e) {
                            //e.printStackTrace();
                        	Toast toastErrorStartActivitySMS = Toast.
                        			makeText(getApplicationContext(), 
                        			"Ошибка старта connActivity! Текст сообщения: "
                        			+e.getMessage()+".", Toast.LENGTH_LONG);
                        	toastErrorStartActivitySMS.show();
                        }
                  		break;
                  	case 1:
                  		Intent settingsActivity = new Intent(getBaseContext(),
                                Prefs.class);
                    	try	{
                    		startActivity(settingsActivity);
                    	} catch (Exception e) {
                            //e.printStackTrace();
                        	Toast toastErrorStartActivitySMS = Toast.
                        			makeText(getApplicationContext(), 
                        			"Ошибка вывода настроек! Текст сообщения: "
                        			+e.getMessage()+".", Toast.LENGTH_LONG);
                        	toastErrorStartActivitySMS.show();
                        }
                  		break;
                  	case 2:
                  		finish();
                  		break;	
                  	case 3:
                  		selectUpgradeSource();
                  		break;
                  	default:	
                  }
                }
              });
        
        if (!showListMenus)
        	enterListView.setVisibility(0); */
        
        } catch (Exception e) {
        	errorCloseDialog(e.getMessage());
        }
        
    }
    
    public void errorCloseDialog(String msg)	{
		AlertDialog.Builder dec_builder = new AlertDialog.Builder(this);
        
    	dec_builder.setTitle("Ошибка запуска приложения!").setMessage(msg)
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Ок", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	System.runFinalizersOnExit(true);
                    	System.exit(0);
                    }
                }).show();
	}


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/
    
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
        	case R.id.exit_on_enter: 
        		finish();
        		break;
        	case R.id.work_start:
        		Intent connActivity = new Intent(getBaseContext(),
                        ConnectionActivity.class);
            	try	{
            		startActivity(connActivity);	
            	} catch (Exception e) {
                    //e.printStackTrace();
                	Toast toastErrorStartActivitySMS = Toast.makeText(this, 
                			"Ошибка старта connActivity! Текст сообщения: "
                			+e.getMessage()+".", Toast.LENGTH_LONG);
                	toastErrorStartActivitySMS.show();
                }
        		break;
        	case R.id.action_settings:
        		Intent settingsActivity = new Intent(getBaseContext(),
                        Prefs.class);
            	try	{
            		startActivity(settingsActivity);
            	} catch (Exception e) {
                    //e.printStackTrace();
                	Toast toastErrorStartActivitySMS = Toast.makeText(this, 
                			"Ошибка вывода настроек! Текст сообщения: "
                			+e.getMessage()+".", Toast.LENGTH_LONG);
                	toastErrorStartActivitySMS.show();
                }
        		break;
        	default:
    	}
    	
    	return(super.onOptionsItemSelected(item));
    }*/
    
    @Override
    public void onDestroy()	{
    	super.onDestroy();
    	System.runFinalizersOnExit(true);
    	System.exit(0);
    }
    
    public void selectUpgradeSource()	{
    	Vector<String> sect_items = new Vector<String>();

    	sect_items.add("Яндекс-Диск");
    	sect_items.add("Google-Диск");
    	sect_items.add("DropBOX");
    	//sect_items.add("MSSQL");
    	
    	CharSequence[] charSequenceItems = sect_items.toArray
    			(new CharSequence[sect_items.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
    	builder.setTitle("ВЫБЕРИТЕ ИСТОЧНИК").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	switch(which)	{
            		case 0:
            			Intent browser = new Intent(Intent.ACTION_VIEW, 
                  				Uri.parse("http://yadi.sk/d/jx1C54cPBsdQ2"));
                  		startActivity(browser);
            			break;
            		case 1:
            			Intent gbrowser = new Intent(Intent.ACTION_VIEW, 
                  				Uri.parse("https://docs.google.com/file/d/"+
                  				"0B-wUU0BCQHxzRFpwZmxzdlVreVk/edit?usp=sharing"));
                  		startActivity(gbrowser);
            			break;
            		case 2:
            			Intent db_browser = new Intent(Intent.ACTION_VIEW, 
                  				Uri.parse("https://www.dropbox.com/s/"+
                  						"863aq1v46nwecm4/TDAndrApp.apk"));
                  		startActivity(db_browser);
            			break;
            		default:
            	}
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).show();
    }
    
}
