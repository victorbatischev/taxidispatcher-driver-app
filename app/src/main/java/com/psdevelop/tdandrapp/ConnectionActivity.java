package com.psdevelop.tdandrapp;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View; 
import android.view.inputmethod.InputMethodManager;

public class ConnectionActivity extends Activity {
	
	WakeLock wakeLock;
	
	public void sendInfoBroadcast(int action_id, String message) {
        Intent intent = new Intent(GpsLocationDetector.FROM_CACTIVITY);
        intent.putExtra(GpsLocationDetector.TYPE, action_id);
        intent.putExtra(GpsLocationDetector.MSG_TEXT, message);
        sendBroadcast(intent);
    }

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	public final static int SET_MSG_LABEL = 1;
	public final static int SET_TITLE_LABEL = 2;
	public final static int SET_ORDER_LABEL = 3;
	public final static int SET_SECTOR_LABEL = 4;
	public final static int SHOW_SERVER_MSG = 5;
	public final static int SHOW_MY_MSG = 6;
	public final static int SHOW_CUSTOM_MSG = 7;
	public final static int ADD_LOG_MSG = 8;
	public final static int SHOW_DLG = 9;
	public final static int PLAY_MP3 = 10;
	public final static int REBUILD_SOCKET = 11;
	public final static int SYNC_ORDER = 12;
	public final static int SHOTDOWN_SIGNAL = 13;
	public final static int SHOTDOWN_SVR_SIDE = 14;
	public final static int CHANGE_SECTOR = 15;
	public final static int PAY_ORDER = 16;
	public final static int CHANGE_SECTOR_DIRECTION = 17;
	public final static int SHOW_TIMER_VAL = 18;
	public final static int CHECK_TIMER_CONFLICT = 19;
	public final static int PLAY_BONUS = 20;
	public final static int PLAY_CDUB = 21;
	public final static int PLAY_DIN = 22;
	public final static int PLAY_GAI = 23;
	public final static int SHOTDOWN_LINEOUT = 24;
	public final static int SHOW_TAXMETER = 30;
	public final static int TURN_GPS_ON = 25;
	public final static int TURN_GPS_OFF = 26;
	public final static int SEND_GPS_COORD = 27;
	public final static int START_GPSC_REQUEST = 28;
	public final static int SET_ORD_OPTS = 29;
	public final static int SHOW_STATUS = 31;
	public final static int SHOW_ACTIVITY = 32;
	public final static int SHOW_PAYMENT_DLG = 33;
	public final static int RESTORE_TMETER = 34;
	public final static int AUTO_SHOW_WAIT_ORD_DLG = 35;
	public final static int CHECK_CURR_TARIF = 36;
	public final static int FIX_SUMMS_IN_SERVISE = 37;
	public final static int TRY_RESTORE_ACT = 38;
	public final static int RESET_TM_VARS = 39;
	public final static int START_RESTORE_TM = 40;
	public final static int RESET_TMETER = 41;
	public final static int RESET_TMETER_PROCESS = 42;
	public final static int SHOTDOWN_CRITICAL_PRM = 43;
	
	public final static int SRV_SET_MSG_LABEL = 101;
	public final static int SRV_SET_TITLE_LABEL = 102;
	public final static int SRV_SET_ORDER_LABEL = 103;
	public final static int SRV_SET_SECTOR_LABEL = 104;
	public final static int SRV_SHOW_SERVER_MSG = 105;
	public final static int SRV_SHOW_MY_MSG = 106;
	public final static int SRV_SHOW_CUSTOM_MSG = 107;
	public final static int SRV_ADD_LOG_MSG = 108;
	public final static int SRV_SHOW_DLG = 109;
	public final static int SRV_PLAY_MP3 = 110;
	public final static int SRV_REBUILD_SOCKET = 111;
	public final static int SRV_SYNC_ORDER = 112;
	public final static int SRV_SHOTDOWN_SIGNAL = 113;
	public final static int SRV_SHOTDOWN_SVR_SIDE = 114;
	public final static int SRV_CHANGE_SECTOR = 115;
	public final static int SRV_PAY_ORDER = 116;
	public final static int SRV_CHANGE_SECTOR_DIRECTION = 117;
	public final static int SRV_SHOW_TIMER_VAL = 118;
	public final static int SRV_CHECK_TIMER_CONFLICT = 119;
	public final static int SRV_PLAY_BONUS = 120;
	public final static int SRV_PLAY_CDUB = 121;
	public final static int SRV_PLAY_DIN = 122;
	public final static int SRV_PLAY_GAI = 123;
	public final static int SRV_SHOTDOWN_LINEOUT = 124;
	public final static int SRV_SHOW_TAXMETER = 130;
	public final static int SRV_TURN_GPS_ON = 125;
	public final static int SRV_TURN_GPS_OFF = 126;
	public final static int SRV_SEND_GPS_COORD = 127;
	public final static int SRV_START_GPSC_REQUEST = 128;
	public final static int SRV_SET_ORD_OPTS = 129;
	public final static int SRV_SHOW_STATUS = 131;
	public final static int SRV_SHOW_ACTIVITY = 132;
	public final static int SRV_SHOW_PAYMENT_DLG = 133;
	public final static int SRV_RESTORE_TMETER = 134;
	public final static int SRV_AUTO_SHOW_WAIT_ORD_DLG = 135;
	public final static int SRV_CHECK_CURR_TARIF = 136;
	public final static int SRV_FIX_SUMMS_IN_SERVISE = 137;
	public final static int SRV_TRY_RESTORE_ACT = 138;
	public final static int SRV_RESET_TM_VARS = 139;
	public final static int SRV_START_RESTORE_TM = 140;
	public final static int SRV_RESET_TMETER = 141;
	public final static int SRV_TOGGLE_TIME_TMETER_PROCESS = 142;
	public final static int SRV_SHOW_SALE_SUMM = 143;
	public final static int SRV_ACT_FINISH_SIGNAL = 144;
	
	public final static int TSI_SEND_SECT_DIR = 301;
	public final static int TSI_INV_LAUNCH_STAT = 302;
	public final static int TSI_CHANGE_SECTOR = 303;
	public final static int TSI_SEND_SECTS_QUERY = 304;
	public final static int TSI_ACCEPT_FREE_ORD_BUTTON = 305;
	public final static int TSI_SELF_ORD_REQU = 306;
	public final static int TSI_DECLINE_ORDER = 307;
	public final static int TSI_SND_DBALANCE_REQU = 308;
	public final static int TSI_SND_ONPL_REQU = 309;
	public final static int TSI_SND_MAPGPS_REQU = 310;
	public final static int TSI_SND_FSTAT_REQU = 311;
	public final static int TSI_SHOW_ODETAILS = 312;
	public final static int TSI_SEND_ALARM = 313;
	public final static int TSI_SHOW_TARIFF_DLG = 314;
	public final static int TSI_TMETER_START_CONF = 315;
	public final static int TSI_ORDER_SALE = 316;
	public final static int TSI_SHOW_NEXT_ORDER = 317;
	public final static int TSI_ACT_BACK_PRESS = 318;
	public final static int TSI_STOP_NSOCK_SERVICE = 319;
	public final static int TSI_MGR_FUNC_DLG = 320;
	public final static int TSI_SRV_SOUND_UNLOCK = 321;
	public final static int TSI_SRV_SOUND_MAND_UNLOCK = 322;
	public final static int TSI_SHOW_EARLY_ORDER = 323;
	public final static int TSI_SHOW_CLIENT_ON_MAP = 324;
	public final static int TSI_SHOW_CLIENT_DESTINATION = 325;
	public final static int TSI_SHOW_CLIENT_ON_MAP_OSM = 326;
	public final static int TSI_SHOW_CLIENT_DESTINATION_OSM = 327;

	public final static int EXTRA_EXIT = 400;
	
	public static final String FEATURE_LOCATION_GPS = "android.hardware.location.gps";

	boolean RESTORE_TAXOMETR=false;
	boolean TAXOMETR_AS_SERVICE=false;
	boolean SEND_CURR_COORDS=false;
	boolean SHOW_KOPS_IN_SUMM=false;
	static boolean timerIsActive=false;
    float summaryDistSrv=0;
	EditText active_order_tf;
	EditText active_sector_tf;
	TextView statusText;
	TextView timerText;
	TextView tmeterText;
	TextView msgLabel;
	TextView orderOptionsTV;
	TextView tvStatus;
	TextView summCalcTextView;
	String server, port, alt_server, alt_port, login_name, psw_str;
	Handler handle;
	boolean showListMenus, en_moving=false;
	private boolean userInterrupt = false;
	Button nextOrderButton;
	boolean nightMode=false;
	boolean ALARM_ORDER_CONFIRM=false;
	boolean MANUAL_SECTOR_REFRESH=false;
	String REGION_PHONE_CODE="86133";
	String STATE_PHONE_CODE="+7";
	Vector<WaitInterval> wtIntervals;
	SharedPreferences prefs;
	boolean requestBalanceStart=false;
	boolean sendOnPlaceCall=false;
	boolean confirmLineOutOnExit=false;
	String DISP_PHONE="";
	Button dispCallButton, optionsButton, buttonCall;
	Button buttonRep, buttonOth, buttonDirSect, buttonOnLaunch;
	ImageButton tmetrImButton;
	boolean USE_GPS_TAXOMETER=false;
	boolean PASSIVE_NET_MODE=false;
	boolean GPSIsActive=false;
	boolean hasGps=false;
	boolean USE_NETWORK_LOCATION=false;
	boolean USE_BOTH_LOCATIONS=false;
	boolean CONFIRM_WIFI_ENABLED=false;
	boolean USE_CALC_SPEED_DIST=true;
	boolean USE_SENS_SPEED_DIST=false;
	boolean SLEEP_TIME_STDIST=false;
	boolean WAIT_DLG_AUTO=false;
	boolean WAIT_DLG_WITH_SECT=false;
	int TMETER_MIN_DISTANCE=25;
	int TMETER_MAX_DISTANCE=200;
	int TMETER_MIN_SPEED=36;
	int TMETER_MAX_SPEED=140;
	int RECONNECT_NUMBERS=100;
	int freeOrdTonePref=-1;
	int START_BACK_TIME=5*60;
	int REGULAR_BACK_TIME=15;
	int REGULAR_BACK_DISTANCE=30;
	int MAX_ORDER_PRICE=0;
	boolean USE_TIME_DIST_BALANCE=false;
	int lastBackTimeDistance=0;
	int backPortionsCount=REGULAR_BACK_TIME/3;
	double[] backDistPortions;
	static boolean TAXOMETR_INCCALL_ABORT=false;
	boolean CALC_SALE_DINAMYC=false;
	boolean CHECK_TARIF_AREA=false;
	boolean SOCKET_IN_SERVICE=true;
	int START_BACK_DISTANCE=0;
	String mapsYandexPrefix="Анапа+";
	String CURRENCY_SHORT="р.";
	int FIXED_OVERST_DSUMM=0;
	int SOCK_CONN_TIMEOUT=0;
	boolean hasRestoreData = false;
	boolean r_timerIsActive=false;
	boolean r_tmeter_active=false;
	long r_timerValue=0;
	float r_summaryDist=0;
	double r_tmeter_lat=0;
	double r_tmeter_lon=0;
	long r_startmil=0;
	int r_htime=0;
	String r_orderId="";
	int r_prev_summ=0;
	String r_orderHistory="";
	boolean RESET_LOST_BTIME=false;
	String clientVersion="---";
	boolean managerAccessFirstLevel=false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      try	{  
        prefs = PreferenceManager.
        		getDefaultSharedPreferences(this); 
        login_name = prefs.getString("USER_LOGIN", "");
        psw_str = prefs.getString("USER_PSW", "");
        server = prefs.getString("IP1", "");
        port = prefs.getString("IP1_PORT", "");
        alt_server = prefs.getString("IP2", "");
        alt_port = prefs.getString("IP2_PORT", "");
        ////only activity
        showListMenus = prefs.getBoolean("SHOW_LIST_MENUS", true);
        ////only activity
        nightMode = prefs.getBoolean("NIGTH_MODE", false);
        RESTORE_TAXOMETR = prefs.getBoolean("RESTORE_TAXOMETR", false);
        TAXOMETR_AS_SERVICE = prefs.getBoolean("TAXOMETR_AS_SERVICE", false)&&false;
        SEND_CURR_COORDS = prefs.getBoolean("SEND_CURR_COORDS", false);
		  SHOW_KOPS_IN_SUMM = prefs.getBoolean("SHOW_KOPS_IN_SUMM", false);
        ALARM_ORDER_CONFIRM = prefs.getBoolean("ALARM_ORDER_CONFIRM", false);
        REGION_PHONE_CODE = prefs.getString("REGION_PHONE_CODE", "86133");
		  STATE_PHONE_CODE = prefs.getString("STATE_PHONE_CODE", "+7");
		  STATE_PHONE_CODE = STATE_PHONE_CODE.trim();
		CURRENCY_SHORT = prefs.getString("CURRENCY_SHORT", "р.");
		  MANUAL_SECTOR_REFRESH = prefs.getBoolean("MANUAL_SECTOR_REFRESH", false);
		  USE_GPS_TAXOMETER = prefs.getBoolean("USE_GPS_TAXOMETER", false);
        USE_NETWORK_LOCATION = prefs.getBoolean("USE_NETWORK_LOCATION", false);
        USE_BOTH_LOCATIONS = prefs.getBoolean("USE_BOTH_LOCATIONS", false);
        CONFIRM_WIFI_ENABLED = prefs.getBoolean("CONFIRM_WIFI_ENABLED", false);
        PASSIVE_NET_MODE = prefs.getBoolean("PASSIVE_NET_MODE", false);
        requestBalanceStart=prefs.getBoolean("REQ_BALANCE_START", false);
        ////only activity
    	sendOnPlaceCall=prefs.getBoolean("SEND_ONPLACE_CALL", false);
    	confirmLineOutOnExit=prefs.getBoolean("CONF_LINEOUT_EXIT", false);
    	USE_CALC_SPEED_DIST=prefs.getBoolean("USE_CALC_SPEED_DIST", true);
    	USE_SENS_SPEED_DIST=prefs.getBoolean("USE_SENS_SPEED_DIST", false);
    	USE_TIME_DIST_BALANCE=prefs.getBoolean("USE_TIME_DIST_BALANCE", false);
    	TAXOMETR_INCCALL_ABORT=prefs.getBoolean("TAXOMETR_INCCALL_ABORT", false);
    	CALC_SALE_DINAMYC=prefs.getBoolean("CALC_SALE_DINAMYC", false);
    	SLEEP_TIME_STDIST=prefs.getBoolean("SLEEP_TIME_STDIST", false);
    	WAIT_DLG_AUTO=prefs.getBoolean("WAIT_DLG_AUTO", false);
    	WAIT_DLG_WITH_SECT=prefs.getBoolean("WAIT_DLG_WITH_SECT", false);
    	CHECK_TARIF_AREA=prefs.getBoolean("CHECK_TARIF_AREA", false);
    	RESET_LOST_BTIME=prefs.getBoolean("RESET_LOST_BTIME", false);
    	SOCKET_IN_SERVICE=true;
    	DISP_PHONE=prefs.getString("DISP_PHONE", "");
    	mapsYandexPrefix=prefs.getString("GEOCODE_PREFIX", "Анапа+");
    	FIXED_OVERST_DSUMM=GpsLocationDetector.strToIntDefC(
    			prefs.getString("FIXED_OVERST_DSUMM", "0"),0);
    	MAX_ORDER_PRICE=GpsLocationDetector.strToIntDefC(
    			prefs.getString("MAX_ORDER_PRICE", "0"),0);
    	SOCK_CONN_TIMEOUT=GpsLocationDetector.strToIntDefC(
    			prefs.getString("SOCK_CONN_TIMEOUT", "0"),0);
    	TMETER_MIN_DISTANCE=GpsLocationDetector.strToIntDefC(
    			prefs.getString("TMETER_MIN_DISTANCE", "25"),25);
    	TMETER_MAX_DISTANCE=GpsLocationDetector.strToIntDefC(
    			prefs.getString("TMETER_MAX_DISTANCE", "200"),200);
    	TMETER_MIN_SPEED=GpsLocationDetector.strToIntDefC(
    			prefs.getString("TMETER_MIN_SPEED", "36"),36);
    	TMETER_MAX_SPEED=GpsLocationDetector.strToIntDefC(
    			prefs.getString("TMETER_MAX_SPEED", "140"),140);
    	RECONNECT_NUMBERS=GpsLocationDetector.strToIntDefC(
    			prefs.getString("RECONNECT_NUMBERS", "100"),100);
    	
    	START_BACK_TIME=GpsLocationDetector.strToIntDefC(
    			prefs.getString("START_BACK_TIME", "5"),5)*60;
    	int RBT=GpsLocationDetector.strToIntDefC(
    			prefs.getString("REGULAR_BACK_TIME", "0"),0);
    	REGULAR_BACK_TIME=((RBT>0)?RBT*60:15);
    	REGULAR_BACK_DISTANCE=GpsLocationDetector.strToIntDefC(
    			prefs.getString("REGULAR_BACK_DISTANCE", "30"),30);
    	START_BACK_DISTANCE=GpsLocationDetector.strToIntDefC(
    			prefs.getString("START_BACK_DISTANCE", "0"),0);
    	
    	backPortionsCount=REGULAR_BACK_TIME/3;
    	if(backPortionsCount<=0) backPortionsCount=1;
    	backDistPortions = new double[backPortionsCount];
    	
    	if (this.RESTORE_TAXOMETR||this.TAXOMETR_AS_SERVICE||this.SOCKET_IN_SERVICE)	{
            this.registerReceiver(new BroadcastReceiver(){
                @Override
                    public void onReceive(Context context, Intent intent)
                    {
                        int type=intent.getIntExtra(GpsLocationDetector.TYPE, -1);
                        switch (type)
                        {
                            case GpsLocationDetector.RESTORE_FIX_PARAMS:
                                try {
                                	
                                	if (intent.getBooleanExtra("timerIsActive",false)
                                	||intent.getBooleanExtra("tmeter_active",false))	{
                                		
                                		r_timerIsActive=intent.getBooleanExtra("timerIsActive",false);
                                		r_tmeter_active=intent.getBooleanExtra("tmeter_active",false);
                                		r_timerValue=intent.getLongExtra("timerValue",0);
                                    	r_summaryDist=intent.getFloatExtra("summaryDist",0);
                                    	r_tmeter_lat=intent.getDoubleExtra("tmeter_lat",0);
                                    	r_tmeter_lon=intent.getDoubleExtra("tmeter_lon",0);
                                    	r_startmil=intent.getLongExtra("r_startmil",0);
                                    	r_htime=intent.getIntExtra("r_htime",0);
                                    	r_orderId=intent.getStringExtra("r_orderId");
                                    	if(intent.getStringExtra("orderHistory").length()>0)
                                    		r_orderHistory=intent.getStringExtra("orderHistory");
                                    	r_prev_summ=intent.getIntExtra("prev_summ",0);
                                    	hasRestoreData=true;
                                    	
                                    	showMyMsg("Приняты данные для восстановления таксометра!");
                                    	
                                	}
                                	
                                } catch(Exception ex)	{
                                    showMyMsg("Ошибка RESTORE_FIX_PARAMS: "+ex);
                                }
                                break;
                            case SRV_SET_MSG_LABEL:
                            	msgLabel.setText(intent.getStringExtra
                    					("msg_lbl_text"));
                            	break;
                            case SRV_SET_TITLE_LABEL:
                            	statusText.setText(intent.getStringExtra
                    					("msg_lbl_text"));
                            	break;
                            case SRV_SET_ORDER_LABEL:
                            	active_order_tf.setText(intent.getStringExtra
                    					("msg_lbl_text"));
                            	break;
                            case SRV_SET_SECTOR_LABEL:
                            	active_sector_tf.setText(intent.getStringExtra
                    					("msg_lbl_text"));
                            	break;
                        	case SRV_ADD_LOG_MSG:
                        		String str_data = intent.getStringExtra
                    					("msg_lbl_text");
	            				try	{
	            			                msgLabel.setText(
	            			                		str_data);
	            			    } catch(Exception ex)   {

	            			    }
                        		break;
                        	case SRV_SHOW_DLG:
                        		showDialogElement(intent.getIntExtra("dlg_type", 
                        				TDDialog.TB_SHOW_MSG),intent.getStringExtra
                    					("msg_lbl_text"));
                        		break;
                        	case SRV_SHOTDOWN_SIGNAL:
                        		break;
                        	case SRV_SHOTDOWN_SVR_SIDE:
                        		break;
                        	case SRV_SHOW_TIMER_VAL:
                        		timerText.setText((intent.getIntExtra("BACK_TIME",0)>0?"(-"+
                        				intent.getIntExtra("BACK_TIME",0)+")":"")+
                        				((int)(intent.getLongExtra("timerValue",0)/3600))+":"+
                        				((int)((intent.getLongExtra("timerValue",0)/60)%60))+
                        				":"+(intent.getLongExtra("timerValue",0)%60));
                        		break;
                        	case SRV_ACT_FINISH_SIGNAL:
                        		userInterrupt = true;
                        		finish();
                        		break;
                        	case SRV_SHOW_SALE_SUMM:
								NumberFormat formatter = new DecimalFormat("#0.00");
								summCalcTextView.setText(" " + (SHOW_KOPS_IN_SUMM ?
										formatter.format(intent.getDoubleExtra("saleSumm",0)).replace('.',',') : (int)intent.getDoubleExtra("saleSumm",0))+" "+CURRENCY_SHORT);
                        		break;
                        	case SRV_SHOW_TAXMETER:
                        		switch(intent.getIntExtra("ltype",0))	{
	                				case Taxometr.LOCATION_NONE:
	                					tmeterText.setText(intent.getStringExtra
	                							("tmeter_text")+" [-X-]");
	                					break;
	                				case Taxometr.LOCATION_SATELLITE:
	                					tmeterText.setText(intent.getStringExtra
	                							("tmeter_text")+" [(--]");
	                					break;
	                				case Taxometr.LOCATION_NETWORK:
	                					tmeterText.setText(intent.getStringExtra
	                							("tmeter_text")+" [~|~]");
	                					break;
	                				default:
	                					tmeterText.setText(intent.getStringExtra
	                							("tmeter_text")+" [-?-]");
	                			}
                        		summaryDistSrv=intent.getFloatExtra("tmeter_val", 0);
	                        		break;
                        	case SRV_SET_ORD_OPTS:
                        		orderOptionsTV.setText(intent.getStringExtra("msg_lbl_text"));
                        		break;
                        	case SRV_SHOW_STATUS:
                        		tvStatus.setText(intent.getStringExtra("msg_lbl_text"));
                        		break;
                        	case SRV_SHOW_ACTIVITY:
                        		setVisible(true);
                        		break;
                        	case SRV_RESET_TMETER:
                        		tmeterText.setText("0км [-X-]");
                    			summCalcTextView.setText(" 0 "+CURRENCY_SHORT);
                    			timerText.setText("0:0:0");
                        		break;
                        	case GpsLocationDetector.TAI_MAFL_ENABLE:
                        		managerAccessFirstLevel=true;
                        	default:
                        		showMyMsg("Неопознанная инструкция от службы!");
                        }
                    }
                }
            , new IntentFilter(GpsLocationDetector.FROM_SERVICE));
            }
    	
    	try	{
    		freeOrdTonePref = this.getResources().getIdentifier(
    			prefs.getString("freeOrdTonePref", "bonus"), 
    			"raw", "com.psdevelop.tdandrapp");
    		if(freeOrdTonePref<=0)
    			freeOrdTonePref = R.raw.bonus;
    	} catch(Exception e)	{
    		freeOrdTonePref = R.raw.bonus;
    	}
    	if(RECONNECT_NUMBERS<100)
    		RECONNECT_NUMBERS=100;
    	
        wtIntervals = new Vector<WaitInterval>();
        wtIntervals.add(new WaitInterval(5,"5 минут"));
        wtIntervals.add(new WaitInterval(10,"10 минут"));
        wtIntervals.add(new WaitInterval(15,"15 минут"));
        wtIntervals.add(new WaitInterval(20,"20 минут"));
        
        setContentView(R.layout.online_layout);
        
        tmeterText = (TextView)findViewById(R.id.tmeterTextView);
        
        handle = new Handler()	{
        	@Override
        	public void handleMessage(Message msg)	{
        		try	{
        		if (msg.arg1 == ConnectionActivity.SET_MSG_LABEL) {
        			msgLabel.setText(msg.getData().
        					getString("msg_lbl_text"));
        		}//
        		else if (msg.arg1 == ConnectionActivity.SET_TITLE_LABEL) {
        			statusText.setText(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SET_SECTOR_LABEL) {
        			active_sector_tf.setText(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SET_ORDER_LABEL) {
        			active_order_tf.setText(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOW_SERVER_MSG) {
        			showServerMsg(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOW_MY_MSG) {
        			showMyMsg(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOW_STATUS) {
        			tvStatus.setText(clientVersion+msg.getData().
        					getString("msg_lbl_text")+", ld="+lastBackTimeDistance);
        		}
        		else if (msg.arg1 == ConnectionActivity.RESET_TMETER)	{
        			tmeterText.setText("0км [-X-]");
        			summCalcTextView.setText(" 0 р.");
        			timerText.setText("0:0:0");
        		}
        		else if (msg.arg1 == ConnectionActivity.ADD_LOG_MSG) {
        				String str_data = msg.getData().
        					getString("msg_lbl_text");
        				try	{
        			                msgLabel.setText(
        			                		str_data);
        			    } catch(Exception ex)   {
        			    	//Toast alertMessage = Toast.makeText(this, 
        			    	//		"СООБЩЕНИЕ: "
        			    	//		+ex.getMessage(), Toast.LENGTH_LONG);
        			    	//alertMessage.show();
        			    }
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOW_DLG) {
        			showDialogElement(msg.getData().
        					getInt("dlg_type"),msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.PLAY_MP3) {
        			playMP3(msg.getData().
        					getInt("res_id"));
        		}
        		else if (msg.arg1 == ConnectionActivity.REBUILD_SOCKET) {
        			if (!userInterrupt)
        				rebuildSocketThreadAndService();
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_SIGNAL) {
        			closeOnlyDialog(false, false);
        		} 
        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_CRITICAL_PRM) {
        			closeOnlyDialog(false, true);
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_SVR_SIDE) {
        			closeOnlyDialog(true, false);
        		}
        		else if (msg.arg1 == ConnectionActivity.CHANGE_SECTOR) {
        			if (en_moving)
        				changeSector(false);
        			else
        				changeSector(true);
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_LINEOUT) {
        			closeDialogOutline();
        		}
        		else if (msg.arg1 == ConnectionActivity.SET_ORD_OPTS) {
        			orderOptionsTV.setText(msg.getData().
        					getString("msg_lbl_text"));
        		}
        		else if (msg.arg1 == ConnectionActivity.SHOW_ACTIVITY) {
        			setVisible(true);
        		}
        		} catch (Exception e)	{
        			showMyMsg("Ошибка: "+e.getMessage());
        		}
        	}
        };
        
        ////////////////////////////////////////////
        ////only avtivity operations/////////
        ////////////////////////////////////////////
        active_order_tf = (EditText)findViewById(R.id.CurrOrderEdit);
        active_sector_tf = (EditText)findViewById(R.id.CurrSectorEdit);
        active_order_tf.setFocusable(false);
        active_sector_tf.setFocusable(false);
        
        active_sector_tf.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (MANUAL_SECTOR_REFRESH)	{
        			if(SOCKET_IN_SERVICE)	{
        				sendInfoBroadcast(TSI_SEND_SECTS_QUERY, "---");
        			}
        			showMyMsg("Подождите, выполняется"+
        		        	" запрос статусов секторов!");
        		}
        		else	{
        			changeSector(false);
        		}
        	}
    	});
        
        active_order_tf.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		sendInfoBroadcast(TSI_ACCEPT_FREE_ORD_BUTTON, "---");
        	}
    	});
        
        InputMethodManager inputMethodManager = 
        		(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
        		active_order_tf.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(
        		active_sector_tf.getWindowToken(), 0);
        statusText = (TextView)findViewById(R.id.StatusText);
        timerText = (TextView)findViewById(R.id.timerText);
        msgLabel = (TextView)findViewById(R.id.msgLabel);
        orderOptionsTV = (TextView)findViewById(R.id.ordOptsTextView);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        tvStatus.setText(clientVersion);
        summCalcTextView = (TextView)findViewById(R.id.summCalcTView);
		  summCalcTextView.setText("0000.00 "+CURRENCY_SHORT);
        if(!CALC_SALE_DINAMYC)
        	summCalcTextView.setText("--------");

        if (this.nightMode)	{
        	RelativeLayout rl = (RelativeLayout)findViewById(R.id.lineLayot);
        	rl.setBackgroundColor(Color.BLACK);
        	statusText.setTextColor(Color.WHITE);
        	TextView SectorTitle = (TextView)findViewById(R.id.SectorTitle);
        	SectorTitle.setTextColor(Color.WHITE);
        	TextView CurrOrderTitle = (TextView)findViewById(R.id.CurrOrderTitle);
        	CurrOrderTitle.setTextColor(Color.WHITE);
        	active_order_tf.setBackgroundColor(Color.BLACK);
        	active_order_tf.setTextColor(Color.WHITE);
        	active_sector_tf.setBackgroundColor(Color.BLACK);
        	active_sector_tf.setTextColor(Color.WHITE);
        	timerText.setTextColor(Color.WHITE);
        	tmeterText.setTextColor(Color.WHITE);
        	orderOptionsTV.setTextColor(Color.WHITE);
        }
        
        nextOrderButton = (Button)findViewById(R.id.nextOrdButton);
        dispCallButton = (Button)findViewById(R.id.dispCallButton);
        optionsButton = (Button)findViewById(R.id.optionsButton);
        tmetrImButton = (ImageButton)findViewById(R.id.tmeterImButton);
        buttonCall = (Button)findViewById(R.id.buttonCall); 
        buttonRep = (Button)findViewById(R.id.buttonReport); 
        buttonOth = (Button)findViewById(R.id.buttonOther);
        buttonDirSect = (Button)findViewById(R.id.buttonDirSect);
        buttonOnLaunch = (Button)findViewById(R.id.buttonOnLaunch);
        
        buttonDirSect.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        	    sendSectDirection();
        	}
    	});
        
        buttonOnLaunch.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		invertLaunchStatus();
        	}
    	});
        
        buttonCall.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				sendInfoBroadcast(TSI_SND_ONPL_REQU, "---");
        	}
    	});
        
        buttonRep.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		sendInfoBroadcast(TSI_ORDER_SALE, "---");
        	}
    	});
        
        buttonOth.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		othersActionDialog();
        	}
    	});
        
        nextOrderButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				showNextDialog();
        	}
    	});
        
        dispCallButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
				callToClient();
        	}
    	});
        
        optionsButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		sendInfoBroadcast(TSI_SHOW_TARIFF_DLG, "---");
        	}
    	});
        
        tmetrImButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		sendInfoBroadcast(TSI_TMETER_START_CONF, "---");
        	}
    	});
        ////////////////////////////////////////////
        ////end of only avtivity operations/////////
        ////////////////////////////////////////////
        
        PowerManager powerManager = (PowerManager) 
				getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock
				(PowerManager.FULL_WAKE_LOCK//PARTIAL_WAKE_LOCK
						, "No sleep");
		wakeLock.acquire();
        
        GPSIsActive=false;
        PackageManager pm = getPackageManager();
        hasGps = pm.hasSystemFeature(FEATURE_LOCATION_GPS);
        
        this.userInterrupt = false;
        rebuildSocketThreadAndService();
        tvStatus.setText(clientVersion);
	} catch (Exception e)	{
		showMyMsg("Ошибка создания рабочего процесса: "+e.getMessage());
	}
    }

    public void showNextDialog() {
		Vector<String> oth_act_items = new Vector<String>();
		oth_act_items.add("Текущие заказы");
		oth_act_items.add("Запланированные");

		CharSequence[] charSequenceItems = oth_act_items.toArray
				(new CharSequence[oth_act_items.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("ВЫБЕРИТЕ ДЕЙСТВИЕ").setItems(charSequenceItems,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch(which)	{
							case 0:
								sendInfoBroadcast(TSI_SHOW_NEXT_ORDER, "---");
								break;
							case 1:
								sendInfoBroadcast(TSI_SHOW_EARLY_ORDER, "---");
								break;
							default:
								showMyMsg("Пункт меню неактивен!");
								break;
						}
					} } )
				// кнопка "Yes", при нажатии на которую приложение закроется
				.setPositiveButton("Не надо",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{

							}
						}).show();
	}
	
	//////////////////////////////////
	/////Функции выгружаемого Activity
	//////////////////////////////////
	
	public void callDisp()	{
		if (timerIsActive)	{
    		showMyMsg("Активен таймер или таксометр, сверните программу кнопкой ^ и наберите вручную!");
    		this.playMP3(R.raw.critical);
    	}	
    	else if (this.hasGps&&this.USE_GPS_TAXOMETER)	{
    		if (Taxometr.serviceActive||Taxometr.singleGPSActivating)	{
    			showMyMsg("Активен таймер или таксометр, сверните программу кнопкой ^ и наберите вручную!");
        		this.playMP3(R.raw.critical);
    		}
    		else	{
    			callDispAllow();
        	}
    	}
    	else	{
    		callDispAllow();
    	}
	}
	
	public void callDispAllow()	{
		if (DISP_PHONE.length() >= 5)	{
		AlertDialog.Builder dec_builder = new AlertDialog.Builder(this);
        
    	dec_builder.setTitle("Подтверждение действия...")
            .setMessage("Набрать номер диспетчера?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	try	{
							Intent dialIntent = new Intent(Intent.ACTION_DIAL,
									Uri.fromParts("tel", STATE_PHONE_CODE + DISP_PHONE, null));
							startActivity(dialIntent);
                        	} catch(Exception cex)	{
                        		showMyMsg(
                                        "Ошибка набора номера!");
                        	}
                        
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).show();
		}	else {
			showMyMsg(
                    "Неверно указан номер диспетчера в настройках!");
		}
    	
    }
	
	public void othersActionDialog()	{
		Vector<String> oth_act_items = new Vector<String>();
		oth_act_items.add("Занят/свободен");
		oth_act_items.add("С руки");
		oth_act_items.add("Освобождение");
		oth_act_items.add("Отказ");
		oth_act_items.add("Данные за смену");
		oth_act_items.add("На точке");
		oth_act_items.add("Обновить статус");
		oth_act_items.add("Детали поездки");
		oth_act_items.add("Позвонить клиенту");
		oth_act_items.add("Тревога (SOS)");
		oth_act_items.add("Позвонить диспетчеру");
		oth_act_items.add("Ожидание");
		oth_act_items.add("Где клиент? В Гугл-картах");
		oth_act_items.add("Куда едем? В Гугл-картах");
		oth_act_items.add("Где клиент? В Интернет");
		oth_act_items.add("Куда едем? В Интернет");
		if(managerAccessFirstLevel)
			oth_act_items.add("Менеджер");
		
		CharSequence[] charSequenceItems = oth_act_items.toArray
    			(new CharSequence[oth_act_items.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setTitle("ВЫБЕРИТЕ ДЕЙСТВИЕ").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	switch(which)	{
            	case 0:
            	    invertLaunchStatus();
            		break;
            	case 1:
            		sendInfoBroadcast(TSI_SELF_ORD_REQU, "---");
            		break;
            	case 2:
                    sendSectDirection();
            		break;
            	case 3:
            		sendInfoBroadcast(TSI_DECLINE_ORDER, "---");
            		break;
            	case 4:
            		sendInfoBroadcast(TSI_SND_DBALANCE_REQU, "---");
            		break;
            	case 5:
            		sendInfoBroadcast(TSI_SND_ONPL_REQU, "---");
            		break;
            	case 6:
            		sendInfoBroadcast(TSI_SND_FSTAT_REQU, "---");
            		break;
            	case 7:
            		sendInfoBroadcast(TSI_SHOW_ODETAILS, "---");
            		break;
				case 8:
					callToClient();
					break;
				case 9:
					sendInfoBroadcast(TSI_SEND_ALARM, "---");
					break;
				case 10:
					callDisp();
					break;
				case 11:
					sendInfoBroadcast(SRV_TOGGLE_TIME_TMETER_PROCESS, "---");
					break;
				case 12:
					sendInfoBroadcast(TSI_SHOW_CLIENT_ON_MAP, "---");
					break;
				case 13:
					sendInfoBroadcast(TSI_SHOW_CLIENT_DESTINATION, "---");
					break;
				case 14:
					sendInfoBroadcast(TSI_SHOW_CLIENT_ON_MAP_OSM, "---");
					break;
				case 15:
					sendInfoBroadcast(TSI_SHOW_CLIENT_DESTINATION_OSM, "---");
					break;
            	case 16:
            		if(managerAccessFirstLevel) {
						sendInfoBroadcast(TSI_MGR_FUNC_DLG, "---");
					}
                	break;
            	default:
            		showMyMsg("Пункт меню неактивен!");
            		break;
            	}
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Не надо", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).show();
    	
	}
	
    @Override
    public void onBackPressed()	{
		sendInfoBroadcast(TSI_ACT_BACK_PRESS, "---");
    }
    
    public void showServerMsg(String message)   {
        try {
        	Toast alertMessage = Toast.makeText(getApplicationContext(), 
        			"СООБЩЕНИЕ СЕРВЕРА: "
        			+message, Toast.LENGTH_LONG);
        	alertMessage.show();
        } catch(Exception ex)   {
        }
    }
    
    public void showMyMsg(String message)   {
    	try {
        	Toast alertMessage = Toast.makeText(getApplicationContext(), 
        			"СООБЩЕНИЕ: "
        			+message, Toast.LENGTH_LONG);
        	alertMessage.show();
        } catch(Exception ex)   {
        }
    }
    
    @Override
    public void onDestroy()	{
    	userInterrupt = true;
    	wakeLock.release();
    	
    	System.runFinalizersOnExit(true);
    	System.exit(0);
    	super.onDestroy();
    }
    
    public void playMP3(int res_id)	{
    	MediaPlayer mediaPlayer;
    	mediaPlayer = MediaPlayer.create(this, res_id); 
    	mediaPlayer.setVolume(1, 1);
    	mediaPlayer.start();
    }
    
    public void callToClientAllow()	{
    	final Vector<String> phones;
    	phones = extractPhone(active_order_tf.
    			getText().toString());
    	
    	CharSequence[] charSequenceItems = phones.toArray
    			(new CharSequence[phones.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	builder.setTitle("ВЫБЕРИТЕ НОМЕР").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	try	{
            	
            	Intent dialIntent = new Intent(Intent.ACTION_DIAL,
            			Uri.fromParts("tel", phones.elementAt(which), null));
            	startActivity(dialIntent);
            	} catch(Exception cex)	{
            		showMyMsg(
                            "Ошибка набора номера!");
            	}
                        
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Отмена", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).show();
    }
    
    public void callToClient()	{
    	if (timerIsActive)	{
    		showMyMsg("Активен таймер или таксометр, сверните программу кнопкой ^ и наберите вручную!");
    		this.playMP3(R.raw.critical);
    	}	
    	else if (this.hasGps&&this.USE_GPS_TAXOMETER)	{
    		if (Taxometr.serviceActive||Taxometr.singleGPSActivating)	{
    			showMyMsg("Активен таймер или таксометр, сверните программу кнопкой ^ и наберите вручную!");
        		this.playMP3(R.raw.critical);
    		}
    		else	{
        		callToClientAllow();
        	}
    	}
    	else	{
    		callToClientAllow();
    	}
    	
    }
    
    public Vector<String> extractPhone(String data)	{
    	Vector<String> res;
    	res = new Vector<String>();
    	String phone="";
    	if (data.length()>4)	{
    		boolean prevNotNum=false;
    		for(int i=0;i<data.length()-1;i++)	{
    			
    			if((data.charAt(i)>='0')&&
    					(data.charAt(i)<='9'))	{
    				if(prevNotNum)	{
    					if(phone.length()<=7 && phone.length()>=5)
    						phone = this.STATE_PHONE_CODE+this.REGION_PHONE_CODE+phone;
    					else if (phone.length()<=10 && phone.length()>7)
    						phone = this.STATE_PHONE_CODE+phone;

    					if (phone.length()>=5)
    						res.add(phone);
    					phone="";
    				}
    				phone+=data.charAt(i);
    				prevNotNum=false;
    			}
    			else
    				prevNotNum=true;
    			
    		}
    		
    		if(phone.length()<=7 && phone.length()>=5)
				phone = this.STATE_PHONE_CODE+this.REGION_PHONE_CODE+phone;
			else if (phone.length()<=10 && phone.length()>7)
				phone = this.STATE_PHONE_CODE+phone;

			if (phone.length()>=5)
				res.add(phone);
    		
    	}
    	return res;
    }
    
	public void startTDService()	{
		Intent gpsScan = new Intent(getBaseContext(),
				GpsLocationDetector.class);
		gpsScan.putExtra("cactivity_start", "yes");
		startService(gpsScan);
	}
	//////////////////////////////////
	/////Функции выгружаемого Activity
	//////////////////////////////////
	
	public static boolean timerProcessing()	{
		return timerIsActive||Taxometr.serviceActive||Taxometr.singleGPSActivating;
	}
	
	public void invertLaunchStatus()	{
		sendInfoBroadcast(TSI_INV_LAUNCH_STAT,"---");
	}
	
    public void changeSector(boolean show_only)	{
		if(show_only)
			this.sendInfoBroadcast(TSI_CHANGE_SECTOR, "yes");
		else
			this.sendInfoBroadcast(TSI_CHANGE_SECTOR, "no");
    }
    
    public void sendSectDirection()	{
	    sendInfoBroadcast(TSI_SEND_SECT_DIR,"---");
    }
    
    public void showDialogElement(int dlg_type, String msg) {
    	try	{
    	switch(dlg_type)    {
            case TDDialog.TB_SHOW_MSG:
                
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                
                builder.setTitle("СООБЩЕНИЕ СЕРВЕРА")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	
                            }
                        }).show();
                break;
            default:
        }
    	
    }	catch(Exception e)	{
		showMyMsg("Ошибка вывода диалога: "+e);
    }
    
}

	/////////////////////////////////////
	///////Функции таксометра которые надо дуплицировать, обращаются к интерфейсу Activity
	/////////////////////////////////////
	
	public void rebuildSocketThreadAndService()	{
		if(!PASSIVE_NET_MODE)	{
			startTDService();
		}
		else	{
			this.showDialogElement(TDDialog.TB_SHOW_MSG,
					"Программа работает в техническом режиме без сетевого обмена!");
		}
	}
    
    public void closeOnlyDialog(boolean unsuccAutorization, boolean criticalParams)	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
            .setMessage(criticalParams?"Изменились критически важные настройки, приложение перезагрузится!":
            	(unsuccAutorization?
            		"Приложение будет закрыто, СЕРВЕР ОТКАЗАЛ В СОЕДИНЕНИИ, свяжитесь с диспетчером!":
            			"Приложение будет закрыто, обратитесь в поддержку или обновите программу!"))
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	if(!SOCKET_IN_SERVICE)	{
                			sendInfoBroadcast(TSI_STOP_NSOCK_SERVICE, "---");
                		}
                    	userInterrupt = true;
                    	finish();
                    }
                }).setCancelable(false)
            .show();
    }
    
    public void closeDialogOutline()	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
            .setMessage("Вы сняты с линии, приложение будет закрыто.")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	if(!SOCKET_IN_SERVICE)	{
                			sendInfoBroadcast(TSI_STOP_NSOCK_SERVICE, "---");
                		}
                    	userInterrupt = true;
                    	finish();
                    }
                })
            .show();
    }    

}
