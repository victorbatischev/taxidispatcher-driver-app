package com.psdevelop.tdandrapp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GpsLocationDetector extends Service implements LocationListener {
	
	WakeLock wakeLock;
	
	public static final String FROM_SERVICE = "com.psdevelop.FROM_SERVICE";
	public static final String FROM_CACTIVITY = "com.psdevelop.FROM_CACTIVITY";
	public static final String TYPE = "type";
	public static final String MSG_TEXT = "msg_text";
	public static final int FIX_ACTIVITY_PARAMS = 1;
	public static final int GET_FIX_PARAMS = 2;
	public static final int RESTORE_FIX_PARAMS = 3;
	public static final int GET_TAX_SERVICE_PARAMS = 4;
	public static final int SET_TAX_SERVICE_PARAMS = 5;
	public static final int START_TAX_SERVICE = 6;
	public static final int STOP_TAX_SERVICE = 7;
	public static final int GO_LAST_SUMM_TO_SERVICE = 8;
	
	public static final int TAI_SHOW_TIMER_VAL = 201;
	public static final int TAI_MAFL_ENABLE = 202;
	public static final int CHECK_SOCKET_CONNECT = 300;
	public static final int REQUEST_DECLINE = 301;
	public static final int RECEIVE_AUTH = 302;
	public static final int RECEIVE_SECTORS_LIST = 303;
	public static final int RECEIVE_TO_LIST = 304;
	public static final int RECEIVE_FORDERS_BROADCAST = 305;
	public static final int RECEIVE_DR_STATUS = 306;
	public static final int RECEIVE_EARLY = 307;
	
	private LocationManager myManager;
	private boolean imActive=false;
	public SocketClientSrvMode TDSocketClient=null;
	Location lastLocation=null;
	Handler handle;
	GpsScanner gpsScan;
	boolean sound_lock=false;
	String svrInetAddr=""; 
	String accountId="";
	String devId="";
	String server, port, alt_server, alt_port, login_name, psw_str;
	String completingOrderId="";
	SharedPreferences prefs;
	double lastLatitude=0.0;
	double lastLongitude=0.0;
	double lastNewLat = 0.0;
	double lastNewLon = 0.0;
	Vector<WaitInterval> wtIntervals;
	float lastSpeed=0;
	boolean serviceActive = false;
	boolean hasFirstDetect = false;
	boolean hasFirstDetect2 = false;
	boolean hasFirstDetect3 = false;
	boolean hasFirstDetectMissing = false;
	boolean hasFirstDetectSending = false;
	boolean showGPSSysEvents = true;
	boolean requestBalanceStart=false;
	boolean payOrderRefreshStarted=false;
	boolean changeSectRefreshStarted=false;
	boolean sectorDirectionStarted=false;
	boolean confirmPrevWaiting=false;
	boolean confirmSyncWaiting=false;
	boolean disableTarDlg=false;
	int freeOrdTonePref = -1;
    int connectingTonePref = -1;
    int assignOrdTonePref = -1;
	int reportOrdTonePref = -1;
	boolean confirmLineOutOnExit=false;
	boolean ALARM_ORDER_CONFIRM=false;
	boolean MANUAL_SECTOR_REFRESH=false;
	String REGION_PHONE_CODE="86133";
	String STATE_PHONE_CODE="+7";
	int SOCK_CONN_TIMEOUT=0;
	float TRACK_DISTANCE = 0;
	long TRACK_INTERVAL = 0;
	boolean RESTORE_TAXOMETR=false;
	boolean TAXOMETR_AS_SERVICE=false;
	boolean SEND_CURR_COORDS=false;
	TaxometrSrvMode tmeter=null;
	OrderTimerSrvMode sockOrderTimer=null;
	static boolean timerIsActive=false;
	long timerValue=0;
	String timerOrderId="";
	long startMillis=0;
	int prev_summ=0;
	int last_summ=0;
	String orderHistory="";
	boolean USE_GPS_TAXOMETER=false;
	boolean PASSIVE_NET_MODE=false;
	boolean GPSIsActive=false;
	boolean hasGps=false;
	boolean USE_NETWORK_LOCATION=false;
	double timeTariff=0;
	double tmeterTariff=0;
	boolean USE_BOTH_LOCATIONS=false;
	boolean CONFIRM_WIFI_ENABLED=false;
	boolean USE_CALC_SPEED_DIST=true;
	boolean USE_SENS_SPEED_DIST=false;
	int select_tid=-1;
	int TMETER_MIN_DISTANCE=25;
	int TMETER_MAX_DISTANCE=200;
	int TMETER_MIN_SPEED=36;
	int TMETER_MAX_SPEED=140;
	int RECONNECT_NUMBERS=100;
	int START_BACK_TIME=5*60;
	int REGULAR_BACK_TIME=2*60;
	int REGULAR_BACK_DISTANCE=30;
	boolean USE_TIME_DIST_BALANCE=false;
	boolean SHOW_KOPS_IN_SUMM=false;
	boolean SLEEP_TIME_STDIST=false;
	int BACK_TIME=0;
	int lastBackTimeDistance=0;
	int backPortionsCount=REGULAR_BACK_TIME/3;
	double[] backDistPortions=null;
	double prevDistance=0, currDistance=0;
	boolean hasNewDistPortion=false;
	public static boolean TAXOMETR_INCCALL_ABORT=false;
	boolean CALC_SALE_DINAMYC=false;
	int START_BACK_DISTANCE=0;
	int dpart_count=0;
	boolean block_none_gps=false;
	int none_gps_ecount=0;
	String mapsYandexPrefix="Анапа+";
	int MAX_ORDER_PRICE=0;
	int FIXED_OVERST_DSUMM=0;
	boolean WAIT_DLG_AUTO=false;
	boolean WAIT_DLG_WITH_SECT=false;
	boolean EARLY_DLG_WITH_SECT = false;
	boolean HIDE_OTH_SECT_WAIT_ORDS=false;
	boolean LOCK_FREE_ORDERS_INFO = false;
	//@var boolean Флаг Показывать заявки аукциона при ручном вызове списка
	//"SASWM":"no",
	boolean SHOW_ALL_SECT_WAIT_MANUAL=false;
	//@var boolean Флаг Не показывать заявки аукциона при занятости водителя
	//"DWIBS":"no",
	boolean DONT_WAIT_ORDER_IN_BUSY_STATUS=false;
	//@var boolean Флаг Не включать таксометр при отсутствии флага На месте
	//"TBWOP":"no",
	boolean TAXOMETR_BLOCK_WITHOUT_ON_PLACE=false;
	boolean CHECK_TARIF_AREA=false;
	boolean RESET_LOST_BTIME=false;
	boolean SOCKET_IN_SERVICE=true;
	boolean USE_NEW_COORD_LOC_ALGORYTHM = false;
	boolean disableTMReportEdit=false;
    boolean disablePrevSumm = false;
	boolean prevSummOverTaxometr = false;
	boolean dontMinimizeCalcPrice = false;
	boolean taxometerOverSmallPrevSumm = false;
	boolean en_moving=false;
	private boolean userInterrupt = false;
	boolean waitOrdVisible=false;
	String clientVersion="---";
	long lastCCSendTime=0;
	boolean gpsMonitoring=false;
	boolean managerAccessFirstLevel=false;
	boolean dontShutdownService=false;
	boolean unlSndOnStartCAct=false;
	/////===========================================
	/////Переменные дл восстановления старого режима
	/////===========================================
	boolean r_timerIsActive=false;
	boolean r_tmeter_active=false;
	long r_timerValue=0;
	float r_summaryDist=0;
	double r_tmeter_lat=0;
	double r_tmeter_lon=0;
	long r_startmil=0;
	boolean hasRestoreData=false;
	int r_htime=0;
	int r_prev_summ=0;
	String r_orderId="";
	String r_orderHistory="";
	public boolean AUTO_DETECT_SECTOR = false;
	public int checkSectorCounter = 0;
	public boolean START_TIME_CALC_WITH_MENU = false;
	public boolean CALC_TIME_STOPPED = false;
	public String WSS1_PORT = "8088";
	
	public void sendInfoBroadcast(int action_id, String message) {
        Intent intent = new Intent(FROM_SERVICE);
        intent.putExtra(TYPE, action_id);
        intent.putExtra(MSG_TEXT, message);
        sendBroadcast(intent);
    }
	
	public void setMAFLEnable()	{
		this.sendInfoBroadcast(TAI_MAFL_ENABLE, "---");
	}
	
	public void backDistanceCalcIteration(int portionIndex)	{
		try	{
		if((portionIndex>=0)&&(this.backPortionsCount>0)&&(portionIndex<this.backPortionsCount))	{
			if(hasNewDistPortion)	{
				double distPortion=this.currDistance-this.prevDistance;
				this.backDistPortions[portionIndex]=
						(((distPortion>0)&&(distPortion<1000))?distPortion:0);
				hasNewDistPortion=false;
			}
			else	{
				this.backDistPortions[portionIndex]=0;
			}
		}
		lastBackTimeDistance=0;
		for(int i=0;i<this.backPortionsCount;i++)
			lastBackTimeDistance+=this.backDistPortions[i];
		} catch (Exception e)	{
			showMyMsg("Ошибка подсчета расстояния - признака движения! "+e.getMessage());
		}
	}

    public static boolean checkStringDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

	public static boolean checkStringInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static int strToIntDefC(String str_int, int def) {
		int res = def;

		if (checkStringInt(str_int)) {
			res = Integer.parseInt(str_int);
		}

		return res;
	}
	
	public void onCreate() {
		super.onCreate();
		
		lastCCSendTime = System.currentTimeMillis()-70000;
		
		wtIntervals = new Vector<WaitInterval>();
        wtIntervals.add(new WaitInterval(5,"5 минут"));
        wtIntervals.add(new WaitInterval(10,"10 минут"));
        wtIntervals.add(new WaitInterval(15,"15 минут"));
        wtIntervals.add(new WaitInterval(20,"20 минут"));
        
        PowerManager powerManager = (PowerManager) 
				getSystemService(Context.POWER_SERVICE);
		try	{
	    	wakeLock = powerManager.newWakeLock
					(PowerManager.FULL_WAKE_LOCK//PARTIAL_WAKE_LOCK
							, "No sleep");
			wakeLock.acquire();
		} catch(Exception e)	{
			this.showMyMsg("Ошибка wakeLock acquire службы!");
		}
    	PackageManager pm = getPackageManager();
        hasGps = pm.hasSystemFeature(ConnectionActivity.FEATURE_LOCATION_GPS);
		
		this.registerReceiver(new BroadcastReceiver(){
            @Override
                public void onReceive(Context context, Intent intent)
                {
                    int type=intent.getIntExtra(GpsLocationDetector.TYPE, -1);
                    if(!(type==ConnectionActivity.TSI_STOP_NSOCK_SERVICE||type==
                    		GpsLocationDetector.FIX_ACTIVITY_PARAMS)&&dontShutdownService)
                    	sound_lock=false;
                    switch (type)
                    {
                    	case GpsLocationDetector.GO_LAST_SUMM_TO_SERVICE:
                    		last_summ=intent.getIntExtra("last_summ",0);
                    		prev_summ=intent.getIntExtra("prev_summ",0);
                    		r_prev_summ=intent.getIntExtra("prev_summ",0);
                    		orderHistory=intent.getStringExtra("orderHistory");
                    		r_orderHistory=intent.getStringExtra("orderHistory");
                    		break;
                    	case GpsLocationDetector.FIX_ACTIVITY_PARAMS:
                            try {
                                //Message msg = new Message();
                                //msg.arg1 = MainActivity.SHOW_SMS_INFO;
                                //Bundle bnd = new Bundle();
                                //bnd.putString("msg_text", intent.getStringExtra(GpsLocationDetector.MSG_TEXT));
                                //msg.setData(bnd);
                                //handle.sendMessage(msg);
                            	if(((intent.getFloatExtra("summaryDist",0)>0)
                            			&&intent.getBooleanExtra("tmeter_active",false))||
                            			(intent.getBooleanExtra("timerIsActive",false)&&
                            					(intent.getLongExtra("timerValue",0)>0)))	{
                            	r_timerIsActive=intent.getBooleanExtra("timerIsActive",false);
                            	r_tmeter_active=intent.getBooleanExtra("tmeter_active",false);
                            	r_timerValue=intent.getLongExtra("timerValue",0);
                            	r_summaryDist=intent.getFloatExtra("summaryDist",0);
                            	r_tmeter_lat=intent.getDoubleExtra("tmeter_lat",0);
                            	r_tmeter_lon=intent.getDoubleExtra("tmeter_lon",0);
                            	r_startmil=intent.getLongExtra("r_startmil", 0);
                            	r_htime=intent.getIntExtra("r_htime", 0);
                            	r_orderId=intent.getStringExtra("r_orderId");
                            	r_orderHistory=intent.getStringExtra("orderHistory");
                            	r_prev_summ=intent.getIntExtra("prev_summ", 0);
                            	//intent.get
                            	
                            	hasRestoreData=true;
                            	//showMyMsg("СЛУЖБА: Зафиксированы данные для восстановления основного рабочего процесса!");	
                            	}
                            
                            } catch(Exception ex)	{
                                showMyMsg("Ошибка Зафиксированы данные: "+ex);
                            }
                            break;
                        case GpsLocationDetector.GET_FIX_PARAMS:
                            // выполнение полученного намерения
                            //context.startService(new Intent(context, PlayService.class));
                        	if(hasRestoreData)	{
                        	Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
                            rintent.putExtra(GpsLocationDetector.TYPE, GpsLocationDetector.RESTORE_FIX_PARAMS);
                            rintent.putExtra("timerIsActive",r_timerIsActive);
                            rintent.putExtra("tmeter_active",r_tmeter_active);
                            rintent.putExtra("timerValue",r_timerValue);
                            rintent.putExtra("summaryDist",r_summaryDist);
                            rintent.putExtra("tmeter_lat",r_tmeter_lat);
                            rintent.putExtra("tmeter_lon",r_tmeter_lon);
                            rintent.putExtra("r_startmil",r_startmil);
                            rintent.putExtra("r_htime",r_htime);
                            rintent.putExtra("r_orderId",r_orderId);
                            rintent.putExtra("prev_summ",r_prev_summ);//
                            rintent.putExtra("orderHistory",r_orderHistory);
                            rintent.putExtra("last_summ",last_summ);
                            sendBroadcast(rintent);
                            //showMyMsg("СЛУЖБА: Запрошены и пересланы данные для восстановления основного рабочего процесса!");
                        	}
                        	else	{
                        		//showMyMsg("СЛУЖБА: !!!Запрошены но отсутствуют данные для восстановления основного рабочего процесса!");
                        	}
                            break;
                        case ConnectionActivity.TSI_SEND_SECT_DIR:
                        	if(checkActiveNetMode())	{
                        		sendSectDirection();
                        	}
                            break;
                        case ConnectionActivity.TSI_INV_LAUNCH_STAT:
                        	if(en_moving)	{
                    			if(checkActiveNetMode())	{
                    				invertLaunchStatus();
                    			}
                    		}
                    		else	
                    			showMyMsg("Действие не разрешено!");
                        	break;
                        case ConnectionActivity.TSI_CHANGE_SECTOR:
                        	if(checkActiveNetMode())	{
                        		if (en_moving)
        	        				changeSector(false);
        	        			else
        	        				changeSector(true);
                        	}
                        	break;
                        case ConnectionActivity.TSI_SEND_SECTS_QUERY:
                        	if(checkActiveNetMode())	{
                        		changeSectRefreshStarted = true;
                        		TDSocketClient.sendSectorsStatusesQuery();
                        	}
                        	break;
                        case ConnectionActivity.TSI_ACCEPT_FREE_ORD_BUTTON:
							if (DONT_WAIT_ORDER_IN_BUSY_STATUS &&
									TDSocketClient.clientStatus == Driver.IN_WORKING) {
								showMyMsg("Запрещен доступ к аукциону в работе!");
								return;
							}
							waitOrdVisible=false;
        	        		if(checkActiveNetMode())	{
        	        			acceptFormFreeOrders(false);
        	        		}
                        	break;
                        case ConnectionActivity.TSI_SELF_ORD_REQU:
                        	if(checkActiveNetMode())	{
                				TDSocketClient.selfOrderRequest();
                			}
                        	break;
                        case ConnectionActivity.TSI_DECLINE_ORDER:
                        	if(checkActiveNetMode())	{
                				TDSocketClient.declineCancelOrder(false,TDSocketClient.activeOrderID);
                			}
                        	break;
                        case ConnectionActivity.TSI_SND_DBALANCE_REQU:
                        	if(checkActiveNetMode())	{
                				TDSocketClient.sendDrBalanceRequest();
                			}
                        	break;
                        case ConnectionActivity.TSI_SND_ONPL_REQU:
                        	if(checkActiveNetMode())	{
    	            			TDSocketClient.sendDrOnPlaceRequest();
    	            		}
                        	break;
                        case ConnectionActivity.TSI_SND_MAPGPS_REQU:
                        	if(checkActiveNetMode())	{
    	            			TaxometrSrvMode.singleGPSForMapRoute=true;
    	            			startGPSCRequest();
    	            		}
                        	break;
                        case ConnectionActivity.TSI_SND_FSTAT_REQU:
                        	if(checkActiveNetMode())	{
    	            			TDSocketClient.sendFullStatusQuery();
    	            			showMyMsg("Отослан запрос серверу на обновление данных!");
    	            		}
                        	break;
                        case ConnectionActivity.TSI_SHOW_ODETAILS:
                        	showDialogElement( TDDialog.TB_SHOW_MSG, 
                        			"Детали поездки: "+orderHistory);
                        	break;
						case ConnectionActivity.TSI_SHOW_CLIENT_ON_MAP:
							String geoRequest = "";
							try {
								int now_act_ord_index = TDSocketClient.getActiveOrderIndex();
								if (now_act_ord_index >= 0) {
									VectorIstructItem activeOrder = TDSocketClient.activeOrders.
											elementAt(now_act_ord_index);
									intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									geoRequest = "geo:" + activeOrder.clientLat +
											"," + activeOrder.clientLon;

									showMyMsg(geoRequest);

									intent.setData(Uri.parse(geoRequest));
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							} catch(Exception ee) {
								showMyMsg(ee.getMessage() + geoRequest);
							}
							break;
						case ConnectionActivity.TSI_SHOW_CLIENT_ON_MAP_OSM:
							String geoRequestOSM = "";
							try {
								int now_act_ord_index = TDSocketClient.getActiveOrderIndex();
								if (now_act_ord_index >= 0) {
									VectorIstructItem activeOrder = TDSocketClient.activeOrders.
											elementAt(now_act_ord_index);
									intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									geoRequestOSM = "https://www.openstreetmap.org/?mlat=" + activeOrder.clientLat +
											"&mlon=" + activeOrder.clientLon;

									showMyMsg(geoRequestOSM);

									intent.setData(Uri.parse(geoRequestOSM));
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							} catch(Exception ee) {
								showMyMsg(ee.getMessage() + geoRequestOSM);
							}
							break;
						case ConnectionActivity.TSI_SHOW_CLIENT_DESTINATION:
							String geoRequestDest = "";
							try {
								int now_act_ord_index = TDSocketClient.getActiveOrderIndex();
								if (now_act_ord_index >= 0) {
									VectorIstructItem activeOrder = TDSocketClient.activeOrders.
											elementAt(now_act_ord_index);
									intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									geoRequestDest = "geo:" + activeOrder.destLat +
											"," + activeOrder.destLon;

									showMyMsg(geoRequestDest);

									intent.setData(Uri.parse(geoRequestDest));
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							} catch(Exception ee) {
								showMyMsg(ee.getMessage() + geoRequestDest);
							}
							break;
						case ConnectionActivity.TSI_SHOW_CLIENT_DESTINATION_OSM:
							String geoRequestDestOSM = "";
							try {
								int now_act_ord_index = TDSocketClient.getActiveOrderIndex();
								if (now_act_ord_index >= 0) {
									VectorIstructItem activeOrder = TDSocketClient.activeOrders.
											elementAt(now_act_ord_index);
									intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									geoRequestDestOSM = "https://www.openstreetmap.org/?mlat=" + activeOrder.destLat +
											"&mlon=" + activeOrder.destLon;

									showMyMsg(geoRequestDestOSM);

									intent.setData(Uri.parse(geoRequestDestOSM));
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							} catch(Exception ee) {
								showMyMsg(ee.getMessage() + geoRequestDestOSM);
							}
							break;
                        case ConnectionActivity.TSI_SEND_ALARM:
                        	if(checkActiveNetMode())	{
        	        			sendAlarm();
        	        		}
                        	break;
						case ConnectionActivity.SRV_TOGGLE_TIME_TMETER_PROCESS:
							if(checkActiveNetMode() && START_TIME_CALC_WITH_MENU)	{
								CALC_TIME_STOPPED = !CALC_TIME_STOPPED;
								showDialogElement( TDDialog.TB_SHOW_MSG,
										"Подсчет времени таксометром " +
												(CALC_TIME_STOPPED ? "остановлен" : "запущен") + ".");
							} else {
								showDialogElement( TDDialog.TB_SHOW_MSG,
									"Не разрешен режим остановки подсчета" +
											" времени таксометром или неактивно соединение с сервером!");
							}
							break;
                        case ConnectionActivity.TSI_SHOW_TARIFF_DLG:
                        	if(checkActiveNetMode())	{
        	        			showTariffDialog();
        	        		}
                        	break;
                        case ConnectionActivity.TSI_TMETER_START_CONF:
                        	tmeterStartConfirm();
                        	break;
                        case ConnectionActivity.TSI_ORDER_SALE:
                        	saleOrderExt();
                        	break;
                        case ConnectionActivity.TSI_SHOW_NEXT_ORDER:
                        	if(checkActiveNetMode())	{
								showOrdersDlg(TDSocketClient.activeOrders,
										"АКТИВНЫЕ ЗАКАЗЫ", true);
            	        		/*if (!TDSocketClient.occupateNextOrder)
            	        			showMyMsg("Нет других заказов!");
            	        		else
            	        		showDialogElement(TDDialog.TB_SHOW_MSG, 
            		        		"Следующий заказ:\n"+
            						TDSocketClient.nextOrderData);*/
            	        		}
                        	break;
						case ConnectionActivity.TSI_SHOW_EARLY_ORDER:
							if(checkActiveNetMode())	{
								showOrdersDlg(TDSocketClient.earlyOrders,
										"ЗАПЛАНИРОВАННЫЕ ЗАКАЗЫ", true);
							}
							break;
                        case ConnectionActivity.TSI_ACT_BACK_PRESS:
                        	backPressed();
                        	break;
                        case ConnectionActivity.TSI_STOP_NSOCK_SERVICE:
                        	if(!SOCKET_IN_SERVICE)	{
                        		if(!dontShutdownService)
                        			stopSelf();
                        		else
                        			sound_lock=true;
                    		}
                        	break;
                        case ConnectionActivity.TSI_MGR_FUNC_DLG:
                        	if(checkActiveNetMode())	{
                        		showMgrFuncAddToAccountFirstDlg();
                        	}
                        	break;
                        case ConnectionActivity.TSI_SRV_SOUND_UNLOCK:
                        	if(unlSndOnStartCAct&&dontShutdownService)
                        		sound_lock=false;
                        	break;
                        case ConnectionActivity.TSI_SRV_SOUND_MAND_UNLOCK:
                        	if(dontShutdownService)
                        		sound_lock=false;
                        	break;
                        default:
                        	showMyMsg("Неопознанный широковещательный сигнал для службы!");
                    }
                }
            }
			, new IntentFilter(GpsLocationDetector.FROM_CACTIVITY));
		        
		handle = new Handler()	{
		        	@Override
		        	public void handleMessage(Message msg)	{
		        	try	{
		        		if (msg.arg1 == ConnectionActivity.SHOW_MY_MSG) {
		        			showMyMsg(msg.getData().
		        					getString("msg_lbl_text"));
		        		}
						else if (msg.arg1 == ConnectionActivity.EXTRA_EXIT) {
							extraExit();
						}
		        		else if (msg.arg1 == ConnectionActivity.PLAY_MP3) {
		        			playMP3(msg.getData().getInt("res_id"));
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SET_MSG_LABEL) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SET_MSG_LABEL);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SET_TITLE_LABEL) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SET_TITLE_LABEL);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if(msg.arg1 == ConnectionActivity.SHOW_CUSTOM_MSG)	{
		        			showCustomMsg(msg.getData().getString("caption"),
									msg.getData().getString("msg_lbl_text"));
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SET_SECTOR_LABEL) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SET_SECTOR_LABEL);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SET_ORDER_LABEL) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SET_ORDER_LABEL);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.RESET_TM_VARS) {
		        			if(tmeter!=null)	{
		        				tmeter.summaryDist=0;
		        			}
		        			timerValue=0;
		        			sockOrderTimer.hiddenTime=0;
		        		}
		        		else if (msg.arg1 == ConnectionActivity.ADD_LOG_MSG) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ADD_LOG_MSG);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.RESET_TMETER_PROCESS) {
		        			resetTaxometrProcess();
		        		}
		        		
		        		else if (msg.arg1 == ConnectionActivity.SHOW_SERVER_MSG) {
		        	        showServerMsg(msg.getData().getString("msg_lbl_text"));
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOW_STATUS) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_STATUS);
		        	        rintent.putExtra("msg_lbl_text", clientVersion+
		        	        		msg.getData().getString("msg_lbl_text")+", ld="+lastBackTimeDistance);
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.RESET_TMETER)	{
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_RESET_TMETER);
		        	        rintent.putExtra("msg_lbl_text", "---");
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOW_TAXMETER) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_TAXMETER);
		        	        rintent.putExtra("tmeter_text", msg.getData().getString("tmeter_text"));
		        	        rintent.putExtra("ltype", msg.getData().getInt("ltype",0));
		        	        rintent.putExtra("tmeter_val", msg.getData().getDouble("tmeter_val"));
		        	        sendBroadcast(rintent);

		        	        if(msg.getData().
		        					getDouble("tmeter_val")>0)	{
		        			prevDistance=currDistance;
		        			currDistance=msg.getData().
		        					getDouble("tmeter_val");
		        			hasNewDistPortion=true;
		        			}
		        	        
		        	        if(CALC_SALE_DINAMYC)
		        	        	showSaleSumm(false);
		        			else
		        				showSaleSumm(true);
		        		}
		        		
		        		else if (msg.arg1 == ConnectionActivity.SHOW_DLG) {
		        			showDialogElement(msg.getData().getInt("dlg_type",TDDialog.TB_SHOW_MSG),
		        					msg.getData().getString("msg_lbl_text"));
		        		}
		        		else if (msg.arg1 == ConnectionActivity.REBUILD_SOCKET) {
		        			if (!userInterrupt)
		        				rebuildSocketThreadAndService(true, true, false);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SYNC_ORDER) {
		        	        showOrderSyncDialog(msg.getData().
									getString("order_id"), msg.getData().
									getString("msg_lbl_text"), msg.getData().
									getBoolean("confirmWait") && confirmSyncWaiting);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_SIGNAL) {
		        			closeOnlyDialog(false, false);
		        		} 
		        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_CRITICAL_PRM) {
		        			closeOnlyDialog(false, true);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_SVR_SIDE) {
		        			closeOnlyDialog(true, false);
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOTDOWN_SVR_SIDE);
		        	        rintent.putExtra("msg_lbl_text", "---");
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.CHANGE_SECTOR) {
		        	        if (en_moving)
		        				changeSector(false);
		        			else
		        				changeSector(true);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.CHANGE_SECTOR_DIRECTION) {
		        			setSectorDirection();
		        		}
		        		else if (msg.arg1 == ConnectionActivity.PAY_ORDER) {
		        			saleOrder();
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOW_TIMER_VAL) {
		        			showTimerVal();
		        			if(CALC_SALE_DINAMYC)
		        	        	showSaleSumm(false);
		        			else
		        				showSaleSumm(true);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.CHECK_TIMER_CONFLICT)  {
		        			activeTmeterConflict(false);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOTDOWN_LINEOUT) {
		        			closeDialogOutline();
		        		}
		        		else if (msg.arg1 == ConnectionActivity.CHECK_CURR_TARIF) {
		        			checkCurrTarif(false);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SEND_GPS_COORD) {
		        			if(TaxometrSrvMode.singleGPSForMapRoute)	{
		        				TaxometrSrvMode.singleGPSForMapRoute=false;
								String adr="";
		        				try	{
		        					final Vector<String> phones;
		        					phones = extractPhone(TDSocketClient.activeOrderData);
		        					final String ord_str = TDSocketClient.activeOrderData;
		        					if (phones.size()>0)	{
		        						adr=ord_str.replace
		        								(phones.elementAt(0).replace(STATE_PHONE_CODE+REGION_PHONE_CODE, ""),"").replace
		        								(phones.elementAt(0).replace(STATE_PHONE_CODE, ""),"").replace(":","").
		        								replace(",", "+").replace(" ", "");
		        					}
		                        	Intent browseIntent = new Intent(Intent.ACTION_VIEW, 
		                        			Uri.parse("http://psdevelop.ru/ymap2.html?"
		                        					+"lat1="+msg.getData().getString("str_lon")+
		                        					"&lon1="+msg.getData().getString("str_lat")+
		                        					"&adr2="+mapsYandexPrefix+adr));
									browseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		                        	startActivity(browseIntent);
		                        } catch(Exception cex)	{
		                        		showMyMsg(
												"Ошибка открытия страницы маршрута "+
														mapsYandexPrefix+adr+"! "+
														cex.getMessage());
		                        }
		        			}	else	{
		        				TDSocketClient.sendGPSCoords(msg.getData().
		        					getString("str_lat"), msg.getData().
		        					getString("str_lon"));
		        			}
		        		}
		        		else if (msg.arg1 == ConnectionActivity.START_GPSC_REQUEST) {
		        			startGPSCRequest();
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SET_ORD_OPTS) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SET_ORD_OPTS);
		        	        rintent.putExtra("msg_lbl_text", msg.getData().getString("msg_lbl_text"));
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOW_ACTIVITY) {
		        			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_ACTIVITY);
		        	        rintent.putExtra("msg_lbl_text", "---");
		        	        sendBroadcast(rintent);
		        		}
		        		else if (msg.arg1 == ConnectionActivity.SHOW_PAYMENT_DLG) {
		        	        paymentDlg(msg.getData().getString("msg_text"));
		        		}//
		        		else if (msg.arg1 == ConnectionActivity.AUTO_SHOW_WAIT_ORD_DLG) {
		        			if(adlg!=null&&waitOrdVisible)	{
		        				try	{
			        				adlg.dismiss();
			        				waitOrdVisible=false;
		        				} catch (Exception dex)	{
		    	        			showMyMsg("Ошибка adlg.dismiss: "+dex.getMessage());
		    	        		}
		        			}
		        			if(!waitOrdVisible) {
								if (DONT_WAIT_ORDER_IN_BUSY_STATUS &&
										TDSocketClient.clientStatus == Driver.IN_WORKING) {
									return;
								}

		        				if (SHOW_ALL_SECT_WAIT_MANUAL) {
		        					acceptFormFreeOrdersWSectHide(true, true);
								} else {
									acceptFormFreeOrders(true);
								}
							}
		        			//Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        	        //rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_AUTO_SHOW_WAIT_ORD_DLG);
		        	        //rintent.putExtra("msg_lbl_text", "---");
		        	        //sendBroadcast(rintent);
		        		} else if (msg.arg1 == GpsLocationDetector.CHECK_SOCKET_CONNECT) {
							connectCheck();
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_AUTH) {
							int userId = msg.getData().getInt("userId", -1);
							//if (userId > 0) {
								//showMyMsg("Успешная авторизация! userId: " + userId);
							//}
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_SECTORS_LIST) {
							JSONObject sectorsJSON = new JSONObject(
									msg.getData().getString("data", "{}"));
                            if (loadDataFromSocketIO) {
                            	TDSocketClient.parseSectorList(sectorsJSON);
							}
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_TO_LIST) {
							JSONObject toJSON = new JSONObject(
									msg.getData().getString("data", "{}"));
							if (loadDataFromSocketIO) {
								TDSocketClient.parseTarifOptionList(toJSON);
								TDSocketClient.sendStatusQuery();
							}
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_FORDERS_BROADCAST) {
							JSONObject toJSON = new JSONObject(
									msg.getData().getString("data", "{}"));
							if (loadDataFromSocketIO) {
								TDSocketClient.parseFreeOrders(toJSON);
								TDSocketClient.sendStatusQuery();
							}
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_DR_STATUS) {
							JSONObject toJSON = new JSONObject(
									msg.getData().getString("data", "{}"));
							if (loadDataFromSocketIO) {
								TDSocketClient.assignStatusValues(toJSON);
							}
						} else if (msg.arg1 == GpsLocationDetector.RECEIVE_EARLY) {
							JSONObject toJSON = new JSONObject(
									msg.getData().getString("data", "{}"));
							if (loadDataFromSocketIO) {
								TDSocketClient.parseEarlyOrders(toJSON);
							}
						}
		        		else {}
		        	} catch (Exception e)	{
	        			showMyMsg("Ошибка gps_service_msg_handler: "+e.getMessage());
	        		}
		        		
		        	}
		        };

		if (PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("USE_DRIVERS_SOCKET_SERVER", false)) {
			checkTimer = new CheckTimer(this);
		}
	}
	
	public void showSaleSumm(boolean in_last)	{
		final int orderDist = (int)((hasGps&&USE_GPS_TAXOMETER)?tmeter.summaryDist:0);
        final double orderTime = (double)((double)timerValue/60.0);
		double saleSumm = TDSocketClient.calculateTOSumm
				( orderTime, orderDist, this.START_BACK_DISTANCE, prev_summ);
		if(in_last){
			this.last_summ=(int)saleSumm;
		}
		else	{
		if(saleSumm>=0)	{
			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_SALE_SUMM);
	        rintent.putExtra("saleSumm", saleSumm);
	        sendBroadcast(rintent);
		}
		}

		if (loadDataFromSocketIO && mSocket != null && checkTimer != null &&
				checkTimer.sendTaxometerParamsCounter >= 30) {
			checkTimer.sendTaxometerParamsCounter = 0;
			DecimalFormat df = new DecimalFormat("#.##");
			try {
				JSONObject resultJson = new JSONObject();
				resultJson.put("current_sum", df.format(saleSumm));
				resultJson.put("current_dist", orderDist);
				resultJson.put("current_time", (int) orderTime);
				resultJson.put("order_id", TDSocketClient.activeOrderID);

				if (tmeter != null && tmeter.lastGPSLocation != null) {
					resultJson.put("lat", tmeter.lastGPSLocation.getLatitude());
					resultJson.put("lon", tmeter.lastGPSLocation.getLongitude());
				}

				mSocket.emit("taxometr_parameters", resultJson.toString());
			} catch (Exception pex)	{
				this.showMyMsg("Неудачное присваивание параметров taxometr_parameters! "+
						pex.getMessage());
			}
		}
	}
	
	public Vector<String> extractPhone(String data)	{
    	Vector<String> res;
    	res = new Vector<String>();
    	String phone="";
    	//int phonesCount=0;
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
    					//else
    					//	phone=phone;
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
			//else
			//	phone="";
			if (phone.length()>=5)
				res.add(phone);
    		
    	}
    	return res;
    }
	
	//////////////////////////////////////
	//////Диалоговые дупликаты для сервиса вызываемые в том числе через 
	//////Broadcast к сервису, либо сокет классом в сервисе
	//////////////////////////////////////
	public void showMgrFuncAddToAccountFirstDlg()	{
		AlertDialog.Builder mf_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
		final EditText summ_input_text = new EditText(this);
    	summ_input_text.setInputType(InputType.TYPE_CLASS_NUMBER);
    	mf_builder.setView(summ_input_text);
		AlertDialog dlg = mf_builder.setTitle("Положить на счет")
            .setMessage("Введите сумму")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Ввод", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	final int isumm = strToIntDef(summ_input_text.getText()+"",-1);
                    	if(isumm>0)
                    		showMgrFuncAddToAccountSecDlg(isumm);
                    	else
                    		showMyMsg("Недопустимый ввод!");
                        
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
	}
	
	public void showMgrFuncAddToAccountSecDlg(int addsumm)	{
		AlertDialog.Builder mf_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
		final int faddsumm = addsumm;
		final EditText summ_input_text = new EditText(this);
    	summ_input_text.setInputType(InputType.TYPE_CLASS_NUMBER);
    	mf_builder.setView(summ_input_text);
		AlertDialog dlg = mf_builder.setTitle("Положить на счет")
            .setMessage("Введите позывной")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Ввод", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	try	{
                    	final int dr_num = strToIntDef(summ_input_text.getText()+"",-1);
                    	if(dr_num>0)
                    		TDSocketClient.sendMgsAddToAccCommand(dr_num+"",faddsumm+"");
                    	else
                    		showMyMsg("Недопустимый ввод!");
                    	} catch(Exception e)	{
                    		showMyMsg("Ошибка отправки данных!");
                    	}
                        
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
	}
	
	public void tmeterStartConfirm()	{

		if (TAXOMETR_BLOCK_WITHOUT_ON_PLACE) {
			if (!this.TDSocketClient.getActiveOrderDrOnPlace()) {
				showMyMsg("У текущего заказа нет статуса НА ТОЧКЕ!");
				return;
			}
		}

		AlertDialog.Builder time_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
		time_builder.setTitle("Внимание...");
		if (USE_GPS_TAXOMETER&&hasGps)	{
			if(TaxometrSrvMode.serviceActive)
				time_builder.setMessage("Остановить таксометр?");
			else
				time_builder.setMessage("Включить таксометр?");
		}	else
			time_builder.setMessage("Отсутствует GPS-устройство или таксометр не разрешен в настройках, включить/остановить только таймер?");
        // кнопка "Yes", при нажатии на которую приложение закроется
		AlertDialog dlg = time_builder.setPositiveButton("Да", 
        		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                	startOrderTimer();
                }
            }).setNegativeButton("Отмена", 
        		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {

                }
            })
            .create();
        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!sound_lock)
        	dlg.show();
	}
	
	public void sendAlarm()	{
		AlertDialog.Builder dec_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
		AlertDialog dlg = dec_builder.setTitle("Подтверждение действия...")
            .setMessage("Подтверждаете посыл сигнала тревоги?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	String v_obj =
                                "{\"command\":\"alarm\","+
                                "\"msg_end\":\"ok\"}";
                        TDSocketClient.wrapper.
                                    sendToServer(v_obj);
                        
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
    	
    }
	
	public void setSectorDirection()	{
      	
    	Vector<String> sect_items = new Vector<String>();
    	for(int i=0;i<TDSocketClient.workSectors.size();i++)
    		sect_items.add(TDSocketClient.workSectors.
    			elementAt(i).sectorName+" ("+
    			TDSocketClient.workSectors.
    			elementAt(i).drCount+")");
    	
    	CharSequence[] charSequenceItems = sect_items.toArray
    			(new CharSequence[sect_items.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫБЕРИТЕ НАПРАВЛЕНИЕ").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	TDSocketClient.saveCrashData();
            	TDSocketClient.clientStatus = 
            			TDSocketClient.lastConnectClientStatus;
            	if (TDSocketClient.clientStatus==
                        Driver.IN_WORKING)  {
                  			
            		String v_obj = "{\"command\":\"sect_direct\","+
            			"\"sector_id\":\""+
            			TDSocketClient.workSectors.
            			elementAt(which).sectorId+
            			"\",\"order_id\":\""+
            			TDSocketClient.activeOrderID+
                        "\",\"client_id\":\""+
                        TDSocketClient.clientId
                        +"\",\"msg_end\":\"ok\"}";
            		TDSocketClient.wrapper.
                		sendToServer(v_obj);
            	}
          		else
    				showMyMsg("Вы не на исполнении заказа!");
            	
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
    }
	
	public void sendSectDirection()	{
	    	if (MANUAL_SECTOR_REFRESH)	{
	    		sectorDirectionStarted = true;
				TDSocketClient.sendSectorsStatusesQuery();
				showMyMsg("Подождите, выполняется"+
			        	" запрос статусов секторов!");
			}
			else	{
				setSectorDirection();
			}
    }
	
	public void prevConfirmFreeOrder(String order_id)	{
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
    	
    	Vector<String> wait_items = new Vector<String>();
    	for(int i=0;i<this.wtIntervals.size();i++)
    		wait_items.add(this.wtIntervals.
    			elementAt(i).title);
    	final String freeOID = order_id;
    	
    	CharSequence[] charSequenceItems = wait_items.toArray
    			(new CharSequence[wait_items.size()]);
    	
    	AlertDialog dlg = builder.setTitle("Выберите время подачи!").setItems(charSequenceItems, 
	        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	String v_obj = "{\"command\":\"accept_order\","+
                		"\"order_id\":\""+freeOID+"\",\"wait\":\""+ 
            			Integer.toString(wtIntervals.
                    			elementAt(which).timeVal)+
                		"\",\"client_id\":\""+TDSocketClient.clientId
                		+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
        		TDSocketClient.wrapper.
            		sendToServer(v_obj);
        		showMyMsg("Заявка отослана!");
            } } ).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
    }
	
	public void showOrderSyncDialog(String order_id, String order_data, boolean confirmWait)	{
		
    	final String oId = order_id;
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
    	
    	Vector<String> wait_items = new Vector<String>();

    	if (TDSocketClient.waitList.size() > 0) {
			wait_items.add("" + order_data);
			for (int i = 0; i < this.TDSocketClient.waitList.size(); i++) {
				wait_items.add(this.TDSocketClient.waitList.get(i) + " мин.");
			}
		} else {
			wait_items.add("1-2 минуты до '" + order_data + "'");
			for (int i = 0; i < this.wtIntervals.size(); i++) {
				wait_items.add(this.wtIntervals.
						elementAt(i).title);
				//this.showMyMsg(this.wtIntervals.
				//		elementAt(i).title);
			}
		}
    	
    	CharSequence[] charSequenceItems = wait_items.toArray
    			(new CharSequence[wait_items.size()]);
        
    	if (confirmWait)	{
    		builder.setTitle("Подтв. принятия заказа!");
    		
    	}
    	else	{
    		builder.setTitle("Подтв. принятия заказа!").
    		setMessage("Вы отправились на заказ '"+order_data+"'?");
    	}
    	//	
    	if (confirmWait)	{
    		builder.setItems(charSequenceItems, 
    	        	new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	String v_obj="";
					if (TDSocketClient.waitList.size() > 0) {
						if (which == 0) {
							return;
						}
						v_obj = "{\"sync\":\"ok\",\"ot\":\"oac\",\"oid\":\"" + oId +
								"\",\"wait\":\"" + TDSocketClient.waitList.get(which - 1) +
								"\",\"msg_end\":\"ok\"}";
					} else {
						if (which == 0) {
							v_obj = "{\"sync\":\"ok\",\"ot\":\"oac\",\"oid\":\""
									+ oId + "\",\"wait\":\"1" +
									"\",\"msg_end\":\"ok\"}";
						} else
							v_obj = "{\"sync\":\"ok\",\"ot\":\"oac\",\"oid\":\""
									+ oId + "\",\"wait\":\"" + Integer.toString(wtIntervals.
									elementAt(which - 1).timeVal) + "\",\"msg_end\":\"ok\"}";
					}

                	TDSocketClient.wrapper.sendToServer(v_obj);
                	TDSocketClient.stopPeriodicConfirmAlarm();
                } } );
    	}
            // кнопка "Yes", при нажатии на которую приложение закроется
    	else	{
    		builder.setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	String v_obj = "{\"sync\":\"ok\",\"ot\":\"oac\",\"oid\":\""
                                +oId+"\",\"msg_end\":\"ok\"}";
                    	TDSocketClient.wrapper.sendToServer(v_obj);
                    	TDSocketClient.stopPeriodicConfirmAlarm();
                    }
                });
    	}
    	builder.setNegativeButton("Отклонить", 
    			new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
            	TDSocketClient.stopPeriodicConfirmAlarm();
            	TDSocketClient.declineCancelOrder(true,oId);
            }
        });
    	
    	AlertDialog dlg = builder.create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
    }

	public boolean isFailCheckOrderCompany(int orderCompanyId) {
		return !TDSocketClient.checkOrderCompany(orderCompanyId);
	}
	
	//Handler messagFOrdHandler = null;
	AlertDialog adlg = null;
	//Message msgFOrd = new Message();
	
	public void acceptFormFreeOrders(boolean mode_auto)	{
		Vector<String> ord_items = new Vector<String>();
		final boolean ma=mode_auto;
		if (TDSocketClient.freeOrders.size()>0)	{
			
		if (!this.WAIT_DLG_WITH_SECT||(TDSocketClient.workSectors.size()<=0))	{
			
		if (this.WAIT_DLG_WITH_SECT&&(TDSocketClient.workSectors.size()<=0))
			this.showMyMsg("Ожидающие заказы выведены обычным списком так как сектора не определены!");
			
    	for(int i=0;i<TDSocketClient.freeOrders.size();i++) {
    		if (isFailCheckOrderCompany(TDSocketClient.freeOrders.
					elementAt(i).companyId)) {
    			continue;
			}
			ord_items.add(TDSocketClient.freeOrders.
					elementAt(i).orderData);
		}

		if (ord_items.size() <= 0) {
    		return;
		}
    	
    	CharSequence[] charSequenceItems = ord_items.toArray
    			(new CharSequence[ord_items.size()]);
    	
    	final AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
    	
    	adlg = builder.setTitle("=ОЖИДАЮЩИЕ ЗАКАЗЫ=").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	waitOrdVisible=false;
            		if (confirmPrevWaiting)	{
            			prevConfirmFreeOrder(TDSocketClient.freeOrders.
	                			elementAt(which).orderId);
            		}	else	{
	            		String v_obj = "{\"command\":\"accept_order\","+
	                    		"\"order_id\":\""+TDSocketClient.freeOrders.
	                			elementAt(which).orderId+
	                    		"\",\"client_id\":\""+TDSocketClient.clientId
	                    		+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
	            		TDSocketClient.wrapper.
	                		sendToServer(v_obj);
	            		showMyMsg("Заявка отослана!");
            		}
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Не надо", 
            	new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	waitOrdVisible=false;
						TDSocketClient.sendDriverRefuseRequest();
                    }
                }).create();
    	
    		adlg.setCanceledOnTouchOutside(true);
	
    		adlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
	
	            @Override
	            public void onCancel(DialogInterface dialog) {
	            	waitOrdVisible=false;
	            }
	        });
	
    		adlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
	
	            @Override
	            public void onDismiss(DialogInterface dialog) {
	                // TODO Auto-generated method stub
	            	waitOrdVisible=false;
	            }
	        });
    		adlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	
    		if(!ma)	{
    			if (!sound_lock)
    				adlg.show();
    			waitOrdVisible=true;
    		}
    		else
    		{
    			if (!sound_lock)
    				adlg.show();
    			waitOrdVisible=true;
    			
    			/*messagFOrdHandler = new Handler() {
    		        public void handleMessage(android.os.Message msg) {
    		            switch (msg.what) {
    		                case 1:
    		                	adlg.dismiss();
    		                	waitOrdVisible=false;
    		                    break;
    		            }
    		        };
    		    };

    		    msgFOrd.what = 1;*/
    		    //messagFOrdHandler.sendMessageDelayed(msgFOrd, 10000);
    		}
    			
		}
		else {
			final ArrayList<String> litems = new ArrayList<String>();
			Vector<VectorSectorItem> workNotEmptySectors = new Vector<VectorSectorItem>();
			for(int i=0;i<TDSocketClient.workSectors.size();i++)	{
				for(int k=0;k<TDSocketClient.freeOrders.size();k++)
		    		if(TDSocketClient.freeOrders.
		    			elementAt(k).sector_id==this.strToIntDef
		    				(TDSocketClient.workSectors.
				    			elementAt(i).sectorId, -1000) &&
							!isFailCheckOrderCompany(TDSocketClient.freeOrders.
									elementAt(k).companyId))	{
		    			workNotEmptySectors.add(TDSocketClient.workSectors.
				    			elementAt(i));
		    			break;
		    		}
			}
			
			for(int i=0;i<workNotEmptySectors.size();i++)	{
				ord_items.add("["+workNotEmptySectors.
		    			elementAt(i).sectorName+"]");
				litems.add("-1");
				for(int k=0;k<TDSocketClient.freeOrders.size();k++)
		    		if(TDSocketClient.freeOrders.
		    			elementAt(k).sector_id==this.strToIntDef
		    				(workNotEmptySectors.
				    			elementAt(i).sectorId, -1000) &&
							!isFailCheckOrderCompany(TDSocketClient.freeOrders.
									elementAt(k).companyId))	{
		    			ord_items.add(">>>"+TDSocketClient.freeOrders.
		    	    			elementAt(k).orderData);
		    			litems.add(TDSocketClient.freeOrders.
		    	    			elementAt(k).orderId);
		    		}
			}
			
			ord_items.add("[Без сектора]");
			litems.add("-1");
			boolean in_sect;
			for(int k=0;k<TDSocketClient.freeOrders.size();k++)	{
				in_sect=false;
				for(int i=0;i<workNotEmptySectors.size();i++)	{
					if(TDSocketClient.freeOrders.
			    			elementAt(k).sector_id==this.strToIntDef
			    				(workNotEmptySectors.
					    			elementAt(i).sectorId, -1000))
					{
						in_sect=true;
						break;
					}
				}
				if(!in_sect && !isFailCheckOrderCompany(TDSocketClient.freeOrders.
						elementAt(k).companyId))	{
					ord_items.add(">>>"+TDSocketClient.freeOrders.
	    	    			elementAt(k).orderData);
	    			litems.add(TDSocketClient.freeOrders.
	    	    			elementAt(k).orderId);
				}
			}

			if (workNotEmptySectors.size() <= 0 && litems.size() <= 1) {
				return;
			}
			
			CharSequence[] charSequenceItems = ord_items.toArray
	    			(new CharSequence[ord_items.size()]);
	    	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(
	    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
	    	
	    	adlg = builder.setTitle("ОЖИДАЮЩИЕ ЗАКАЗЫ").setItems(charSequenceItems, 
	        	new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	waitOrdVisible=false;
	            	if(!litems.get(which).equals("-1"))	{
	            		if (confirmPrevWaiting)	{
	            			prevConfirmFreeOrder(litems.get(which));
	            		}	else	{
		            		String v_obj = "{\"command\":\"accept_order\","+
		                    		"\"order_id\":\""+litems.get(which)+
		                    		"\",\"client_id\":\""+TDSocketClient.clientId
		                    		+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
		            		TDSocketClient.wrapper.
		                		sendToServer(v_obj);
		            		showMyMsg("Заявка отослана!");
	            		}
	            	}
	            	else
	            		showMyMsg("Необходимо выбирать заявку а не сектор!");
	            } } )
	            // кнопка "Yes", при нажатии на которую приложение закроется
	            .setPositiveButton("Не надо", 
	            	new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	waitOrdVisible=false;
							TDSocketClient.sendDriverRefuseRequest();
	                    }
	                }).create();
	    	
	    	adlg.setCanceledOnTouchOutside(true);
	    	
    		adlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
	
	            @Override
	            public void onCancel(DialogInterface dialog) {
	            	waitOrdVisible=false;
	            }
	        });
	
    		adlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
	
	            @Override
	            public void onDismiss(DialogInterface dialog) {
	                // TODO Auto-generated method stub
	            	waitOrdVisible=false;
	            }
	        });
    		adlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	
    		if(!ma)	{
    			if (!sound_lock)
    				adlg.show();
    			waitOrdVisible=true;
    		}
    		else
    		{
    			if (!sound_lock)
    				adlg.show();
    			waitOrdVisible=true;
    			
    			/*messagFOrdHandler = new Handler() {
    		        public void handleMessage(android.os.Message msg) {
    		            switch (msg.what) {
    		                case 1:
    		                	adlg.dismiss();
    		                	waitOrdVisible=false;
    		                    break;
    		            }
    		        };
    		    };

    		    msgFOrd.what = 1;*/
    		    //messagFOrdHandler.sendMessageDelayed(msgFOrd, 10000);
    		}
			
		}
		}
		else
			this.showMyMsg("Нет заказов в ожидании!");
	}

	public void acceptFormFreeOrdersWSectHide(boolean mode_auto, boolean hideOthersSects)	{
		Vector<String> ord_items = new Vector<String>();
		final boolean ma=mode_auto;
		final Vector<VectorIstructItem> freeOrders =
				new Vector<VectorIstructItem>();

		for (int k = 0; k < TDSocketClient.freeOrders.size(); k++) {
			//showMyMsg("???" + TDSocketClient.freeOrders.elementAt(k).orderData +
			//		( TDSocketClient.freeOrders.elementAt(k).isForAll ? " fa" : " nofa") +
			//		(!isFailCheckOrderCompany(TDSocketClient.freeOrders.elementAt(k).companyId) ? " oc" : " nooc" ));
				if ((TDSocketClient.freeOrders.elementAt(k).sector_id ==
						strToIntDef(TDSocketClient.activeSectorID, -1) ||
						TDSocketClient.freeOrders.elementAt(k).isForAll || !hideOthersSects) &&
						!isFailCheckOrderCompany(TDSocketClient.freeOrders.elementAt(k).companyId)) {
					//showMyMsg(">>>" + TDSocketClient.freeOrders.elementAt(k).orderData);
					freeOrders.addElement(TDSocketClient.freeOrders.elementAt(k));
				}
		}

		if (freeOrders.size()>0)	{

			if (!this.WAIT_DLG_WITH_SECT||(TDSocketClient.workSectors.size()<=0))	{

				if (this.WAIT_DLG_WITH_SECT&&(TDSocketClient.workSectors.size()<=0))
					this.showMyMsg("Ожидающие заказы выведены обычным списком так как сектора не определены!");

				for(int i=0;i<freeOrders.size();i++) {
					ord_items.add(freeOrders.
							elementAt(i).orderData);
				}

				CharSequence[] charSequenceItems = ord_items.toArray
						(new CharSequence[ord_items.size()]);

				final AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));

				adlg = builder.setTitle("=ОЖИДАЮЩИЕ ЗАКАЗЫ=").setItems(charSequenceItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								waitOrdVisible=false;
								if (confirmPrevWaiting)	{
									prevConfirmFreeOrder(freeOrders.
											elementAt(which).orderId);
								}	else	{
									String v_obj = "{\"command\":\"accept_order\","+
											"\"order_id\":\"" + freeOrders.
											elementAt(which).orderId +
											"\",\"client_id\":\"" + TDSocketClient.clientId
											+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
									TDSocketClient.wrapper.
											sendToServer(v_obj);
									showMyMsg("Заявка отослана!");
								}
							} } )
						// кнопка "Yes", при нажатии на которую приложение закроется
						.setPositiveButton("Не надо",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										waitOrdVisible=false;
										TDSocketClient.sendDriverRefuseRequest();
									}
								}).create();

				adlg.setCanceledOnTouchOutside(true);

				adlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						waitOrdVisible=false;
					}
				});

				adlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						waitOrdVisible=false;
					}
				});
				adlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

				if(!ma)	{
					if (!sound_lock)
						adlg.show();
					waitOrdVisible=true;
				}
				else
				{
					if (!sound_lock)
						adlg.show();
					waitOrdVisible=true;

    			/*messagFOrdHandler = new Handler() {
    		        public void handleMessage(android.os.Message msg) {
    		            switch (msg.what) {
    		                case 1:
    		                	adlg.dismiss();
    		                	waitOrdVisible=false;
    		                    break;
    		            }
    		        };
    		    };

    		    msgFOrd.what = 1;*/
					//messagFOrdHandler.sendMessageDelayed(msgFOrd, 10000);
				}

			}
			else
			{

				final ArrayList<String> litems = new ArrayList<String>();
				Vector<VectorSectorItem> workNotEmptySectors = new Vector<VectorSectorItem>();
				for(int i=0;i<TDSocketClient.workSectors.size();i++)	{
					for(int k=0;k<freeOrders.size();k++)
						if(freeOrders.
								elementAt(k).sector_id==this.strToIntDef
								(TDSocketClient.workSectors.
										elementAt(i).sectorId, -1000))	{
							workNotEmptySectors.add(TDSocketClient.workSectors.
									elementAt(i));
							break;
						}
				}

				for(int i=0;i<workNotEmptySectors.size();i++)	{
					ord_items.add("["+workNotEmptySectors.
							elementAt(i).sectorName+"]");
					litems.add("-1");
					for(int k=0;k<freeOrders.size();k++)
						if(freeOrders.
								elementAt(k).sector_id==this.strToIntDef
								(workNotEmptySectors.
										elementAt(i).sectorId, -1000))	{
							ord_items.add(">>>"+freeOrders.
									elementAt(k).orderData
									//(freeOrders.elementAt(k).isForAll ? " fa" : " nofa")
							);
							litems.add(freeOrders.
									elementAt(k).orderId);
						}
				}

				ord_items.add("[Без сектора]");
				litems.add("-1");
				boolean in_sect;
				for(int k=0;k<freeOrders.size();k++)	{
					in_sect=false;
					for(int i=0;i<workNotEmptySectors.size();i++)	{
						if(freeOrders.
								elementAt(k).sector_id==this.strToIntDef
								(workNotEmptySectors.
										elementAt(i).sectorId, -1000))
						{
							in_sect=true;
							break;
						}
					}
					if(!in_sect)	{
						ord_items.add(">>>"+freeOrders.
								elementAt(k).orderData);
						litems.add(freeOrders.
								elementAt(k).orderId);
					}
				}

				CharSequence[] charSequenceItems = ord_items.toArray
						(new CharSequence[ord_items.size()]);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));

				adlg = builder.setTitle("ОЖИДАЮЩИЕ ЗАКАЗЫ").setItems(charSequenceItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								waitOrdVisible=false;
								if(!litems.get(which).equals("-1"))	{
									if (confirmPrevWaiting)	{
										prevConfirmFreeOrder(litems.get(which));
									}	else	{
										String v_obj = "{\"command\":\"accept_order\","+
												"\"order_id\":\""+litems.get(which)+
												"\",\"client_id\":\""+TDSocketClient.clientId
												+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
										TDSocketClient.wrapper.
												sendToServer(v_obj);
										showMyMsg("Заявка отослана!");
									}
								}
								else
									showMyMsg("Необходимо выбирать заявку а не сектор!");
							} } )
						// кнопка "Yes", при нажатии на которую приложение закроется
						.setPositiveButton("Не надо",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										waitOrdVisible=false;
										TDSocketClient.sendDriverRefuseRequest();
									}
								}).create();

				adlg.setCanceledOnTouchOutside(true);

				adlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						waitOrdVisible=false;
					}
				});

				adlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						waitOrdVisible=false;
					}
				});
				adlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

				if(!ma)	{
					if (!sound_lock)
						adlg.show();
					waitOrdVisible=true;
				}
				else
				{
					if (!sound_lock)
						adlg.show();
					waitOrdVisible=true;

    			/*messagFOrdHandler = new Handler() {
    		        public void handleMessage(android.os.Message msg) {
    		            switch (msg.what) {
    		                case 1:
    		                	adlg.dismiss();
    		                	waitOrdVisible=false;
    		                    break;
    		            }
    		        };
    		    };

    		    msgFOrd.what = 1;*/
					//messagFOrdHandler.sendMessageDelayed(msgFOrd, 10000);
				}

			}
		}
		//else
		//	this.showMyMsg("Нет заказов в ожидании!");
	}

	AlertDialog ordersDlg = null;

	public void showOrdersDlg(Vector<VectorIstructItem> dlgOrders,
							  String dlgTitle, boolean showWithSectors)	{
		Vector<String> ord_items = new Vector<String>();
		final Vector<VectorIstructItem> orders = dlgOrders;//TDSocketClient.earlyOrders;
		Vector<VectorSectorItem> sectors = TDSocketClient.workSectors;
		if (orders.size()>0) {
			if (!showWithSectors || sectors.size() <= 0) {
				if (showWithSectors && sectors.size() <= 0)
					this.showMyMsg("Заказы выведены обычным" +
							" списком так как сектора не определены!");

				for(int i = 0; i < orders.size(); i++) {
					ord_items.add(orders.elementAt(i).orderData);
				}

				CharSequence[] charSequenceItems = ord_items.toArray
						(new CharSequence[ord_items.size()]);
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));

				ordersDlg = builder.setTitle(dlgTitle).setItems(charSequenceItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {

							}
						} )
						// кнопка "Yes", при нажатии на которую приложение закроется
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
									}
								}).create();

				ordersDlg.setCanceledOnTouchOutside(true);

				ordersDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
					}
				});

				ordersDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
					}
				});
				ordersDlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				ordersDlg.show();
			} else {
				final ArrayList<String> litems = new ArrayList<String>();
				Vector<VectorSectorItem> workNotEmptySectors = new Vector<VectorSectorItem>();
				for (int i = 0; i < sectors.size(); i++) {
					for (int k = 0; k < orders.size(); k++) {
						if (orders.elementAt(k).sector_id == this.strToIntDef
								(sectors.elementAt(i).sectorId, -1000)) {
							workNotEmptySectors.add(sectors.elementAt(i));
							break;
						}
					}
				}

				for (int i=0;i<workNotEmptySectors.size();i++) {
					ord_items.add("["+workNotEmptySectors.elementAt(i).sectorName+"]");
					litems.add("-1");
					for(int k=0;k<orders.size();k++)
						if (orders.elementAt(k).sector_id==this
								.strToIntDef(workNotEmptySectors.elementAt(i).sectorId, -1000)) {
							ord_items.add(">>>"+orders.elementAt(k).orderData);
							litems.add(orders.elementAt(k).orderId);
						}
				}

				ord_items.add("[Без сектора]");
				litems.add("-1");
				boolean in_sect;
				for (int k = 0; k < orders.size(); k++)	{
					in_sect=false;
					for (int i=0;i<workNotEmptySectors.size();i++)	{
						if (orders.elementAt(k).sector_id==this.strToIntDef
								(workNotEmptySectors.elementAt(i).sectorId, -1000)) {
							in_sect=true;
							break;
						}
					}
					if (!in_sect) {
						ord_items.add(">>>" + orders.elementAt(k).orderData);
						litems.add(orders.elementAt(k).orderId);
					}
				}

				if (workNotEmptySectors.size() <= 0 && litems.size() <= 1) {
					return;
				}

				CharSequence[] charSequenceItems = ord_items.toArray
						(new CharSequence[ord_items.size()]);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));

				ordersDlg = builder.setTitle(dlgTitle).setItems(charSequenceItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (!litems.get(which).equals("-1"))	{
								}
								else {
								}
							} } )
						// кнопка "Yes", при нажатии на которую приложение закроется
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
									}
								}).create();

				ordersDlg.setCanceledOnTouchOutside(true);

				ordersDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
					}
				});

				ordersDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
					}
				});
				ordersDlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				ordersDlg.show();
			}
		} else this.showMyMsg("Нет заказов в списке!");
	}
	
	public void checkGPSActive()	{
		LocationManager lm = 
        		((LocationManager)this.getSystemService(LOCATION_SERVICE));
		
		//boolean gpsMonitoring=prefs.getBoolean("USE_GPS", false);
        
        if ((!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)&&
        		(!USE_NETWORK_LOCATION||USE_BOTH_LOCATIONS))||
        	      (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)&&
        	    		  (USE_NETWORK_LOCATION||USE_BOTH_LOCATIONS)))	{
        	//setGPSEnabled(true);
        	//if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))	{
        	//	turnGPSOn();
        	//	if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))	{
        			String confMessage = "";
        			if(USE_BOTH_LOCATIONS)	{
        				if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))	{
        					confMessage+=" [GPS-ЛОКАЦИЯ]";
        				}
        				if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))	{
        					confMessage+=" [ЛОКАЦИЯ СРЕДСТВАМИ СЕТЕЙ]";
        				}
        			}	else if (USE_NETWORK_LOCATION)	{
        				if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))	{
        					confMessage+=" [ЛОКАЦИЯ СРЕДСТВАМИ СЕТЕЙ]";
        				}
        			}	else	{
        				if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))	{
        					confMessage+=" [GPS-ЛОКАЦИЯ]";
        				}
        			}
        			
        			if (CONFIRM_WIFI_ENABLED&&
        					(USE_BOTH_LOCATIONS||USE_NETWORK_LOCATION))	{
        				confMessage+=" [WIFI-УСТРОЙСТВО]";
        			}
        			AlertDialog.Builder time_builder = new AlertDialog.Builder(
        					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        	        
        			none_gps_ecount++;
        			if(none_gps_ecount>3&&block_none_gps)	{
                		userInterrupt = true;
                		Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
	        	        rintent.putExtra("msg_lbl_text", "---");
                		stopSelf();
                	}
        			else	{
        			AlertDialog dlg=time_builder.setTitle("Внимание...")
                    .setMessage(block_none_gps?"Средства обнаружения неактивны, приложение требуют их включения"+
                    		confMessage+", проверка №"+none_gps_ecount+", всего - 2!":
                    			"Средства обнаружения неактивны, приложение требуют их включения"+
                        		confMessage+"!")
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton("Понятно", 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	if(none_gps_ecount>1&&block_none_gps)	{
                            		userInterrupt = true;
                            		Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
            	        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
            	        	        rintent.putExtra("msg_lbl_text", "---");
                            		stopSelf();
                            	}
                            	else	{
                            		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            		startActivity(intent);
                            	}
                            }
                        }).create();
        			dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        			if (!sound_lock)
        				dlg.show();
        			}
        			
        	//	}
        	//}
        	if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        		GPSIsActive=true;
        	
        }	else	{
        	none_gps_ecount=0;
        	GPSIsActive=true;
        }
        //this.sto
	}
	
	public void startGPSCRequest()	{
		if (hasGps&&USE_GPS_TAXOMETER)	{
			tmeter.requestLUpd(true);
			checkGPSActive();
		}
	}
	
	public void paymentDlg(String dlgText)	{
		AlertDialog.Builder pay_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
  
		AlertDialog dlg = pay_builder.setTitle("Подтверждение выхода на смену...")
            .setMessage("Выйти на смену от "+dlgText+"?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	TDSocketClient.sendPayOkAnwer();
                    	showMyMsg("Сообщено о согласии!");
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	TDSocketClient.sendPayDeclineAnwer();
                    	showMyMsg("Сообщено о несогласии!");
                    }
                }).create();
		dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		if (!sound_lock)
			dlg.show();
	}
	
	public void invertLaunchStatus()	{
		AlertDialog.Builder dec_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
		AlertDialog dlg = dec_builder.setTitle("Подтверждение действия...")
            .setMessage("Подтверждаете смену статуса?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	if ((TDSocketClient.clientStatus==
              					Driver.ON_REST))
                    		TDSocketClient.fromLaunchRequest();
              			else
              				TDSocketClient.onLaunchRequest();
                        
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
	}
	
	public void showOrdOptionsDialog(String settar_oid)	{
		if(TDSocketClient.clientStatus==Driver.IN_WORKING)	{
			if(TDSocketClient.ordersOptions.size()>0)	{
			int act_ord_index = TDSocketClient.getActiveOrderIndex();
			if(act_ord_index>=0)	{
			final String setopt_oid = TDSocketClient.activeOrderID;
			if(settar_oid.equals(setopt_oid))	{
			AlertDialog.Builder option_builder = new AlertDialog.Builder(
					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
			Vector<String> option_items = new Vector<String>();
			Vector<Integer> str_opts = new Vector<Integer>();
			
			//select_options=-1;
			final int act_tplan_id = TDSocketClient.activeOrders.
					elementAt(act_ord_index).tplan_id;
			
			for(int i=0;i<TDSocketClient.ordersOptions.size();i++)	{
				
				if((TDSocketClient.ordersOptions.
						elementAt(i).tplan_id==act_tplan_id)||
						(act_tplan_id<=0))	{
					option_items.add(TDSocketClient.ordersOptions.
							elementAt(i).option_name);
					str_opts.add(TDSocketClient.ordersOptions.
							elementAt(i).id);
				}
				
			}
			
			if (option_items.size()>0)	{
			
			final int options_cnt = option_items.size();
			final boolean[] checkedOptItems = new boolean[options_cnt];
			final int[] optIds = new int[options_cnt];
			
			for(int p=0;p<options_cnt;p++)	{
				optIds[p] = str_opts.elementAt(p).intValue();
				checkedOptItems[p]=false;
			}
			
			String orderOpts = TDSocketClient.activeOrders.
					elementAt(act_ord_index).ordOptComb;
			
			StringTokenizer st = new StringTokenizer(orderOpts, " \t\n\r,."); 

			while(st.hasMoreTokens())	{
				// Получаем слово и что-нибудь делаем с ним, например,
				// просто выводим на экран
				int optsId = strToIntDef(st.nextToken(),0);
				if(optsId>0)
					for(int j=0;j<options_cnt;j++)	{
						for(int l=0;l<TDSocketClient.
								ordersOptions.size();l++)	{
						if((TDSocketClient.ordersOptions.
								elementAt(l).id==optsId)&&
								(optIds[j]==optsId))
							checkedOptItems[j]=true;
						}
					}

				}
			
			/*int j=0;
			while((orderOpts>0)&&(j<options_cnt))	{
				if((orderOpts%2)!=0)
					checkedOptItems[j]=true;
				j++;
				orderOpts = (int)(orderOpts/2);
			}*/
			//checkedOptItems.
			
			CharSequence[] optionsItems = option_items.toArray
	    			(new CharSequence[option_items.size()]);
			AlertDialog dlg = option_builder.setTitle("Выбор опций заказа...").
				setMultiChoiceItems(optionsItems, checkedOptItems,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {

								if (isChecked)
									checkedOptItems[which] = true;
								else
									checkedOptItems[which] = false;
							}
						}).setPositiveButton("OK",
		            		new DialogInterface.OnClickListener()
		            {
		                public void onClick(DialogInterface dialog, int whichButton) 
		                {
		                	String select_options="";
		                	int appendCount=0;
		                	for(int k=0;k<options_cnt;k++)	{
		                		if(checkedOptItems[k]) { 
		                			if (appendCount>0)
		                				select_options+=".";
		                			select_options+=(""+optIds[k]);
		                			appendCount++;
		                		}
		                	}
		                	if(select_options.length()==0)
		                		select_options="-";
		                	TDSocketClient.applySelectOrderOptions(select_options, setopt_oid);
		                }
		            }).setNegativeButton("Отмена", 
		            		new DialogInterface.OnClickListener()
		            {
		                public void onClick(DialogInterface dialog, int whichButton) 
		                {
		                	
		                }
		            })
					.create();
				dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				if (!sound_lock)
					dlg.show();
			}	else	{
				this.showMyMsg("Не найдено опций для тар. плана заявки!");
				
			}
			}	else
				this.showMyMsg("Заявка поменялась, опции предыдущей недоступны!");
			
			}	else
				this.showMyMsg("Ошибка поиска текущей заявки в массиве!");
			}	else
				this.showMyMsg("Не определено ни одной опции!");
		}	else
			this.showMyMsg("Надо иметь статус ЗАНЯТ!");
	}
	
	public void showTariffDialog()	{
		
		if(TDSocketClient.clientStatus==Driver.IN_WORKING)	{
		if(!disableTarDlg)	{
		if(TDSocketClient.ordersTarifs.size()>0)	{
		int act_ord_index = TDSocketClient.getActiveOrderIndex();
		if(act_ord_index>=0)	{
		AlertDialog.Builder tarif_builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
		Vector<String> tarifs_items = new Vector<String>();
		Vector<Integer> tarif_ids = new Vector<Integer>();
		
		final int act_tplan_id = TDSocketClient.activeOrders.
				elementAt(act_ord_index).tplan_id;
		
		final String settar_oid = TDSocketClient.activeOrderID;
		int tarif_id=TDSocketClient.activeOrders.elementAt(act_ord_index).ordTariffId;
		int choice_item_index=-1;
		select_tid=-1;
		
		for(int i=0;i<TDSocketClient.ordersTarifs.size();i++)	{
			if((TDSocketClient.ordersTarifs.
					elementAt(i).tplan_id==act_tplan_id)||
					(act_tplan_id<=0))	{
				tarifs_items.add(TDSocketClient.ordersTarifs.
					elementAt(i).tarif_name);
				tarif_ids.add(TDSocketClient.ordersTarifs.
						elementAt(i).id);
				if((tarif_id>0)&&(tarif_id==TDSocketClient.ordersTarifs.elementAt(i).id))	{
					choice_item_index=tarifs_items.size()-1;
				}
			}
		}
		
		if(tarifs_items.size()>0)	{
		
		final int tar_cnt = tarifs_items.size();
		final int[] tarIds = new int[tar_cnt];
		
		for(int p=0;p<tar_cnt;p++)	{
			tarIds[p] = tarif_ids.elementAt(p).intValue();
		}

		CharSequence[] tarifsItems = tarifs_items.toArray
    			(new CharSequence[tarifs_items.size()]);
		AlertDialog dlg = tarif_builder.setTitle("Выбор тарифа...").
			setSingleChoiceItems(tarifsItems, choice_item_index, new DialogInterface.OnClickListener() {
				 
					@Override
					public void onClick(DialogInterface dialog, int which) {
						select_tid = tarIds[which];
						//dialog.dismiss();
					}
					}
			).setPositiveButton("OK", 
            		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                    TDSocketClient.applySelectOrderTariff(select_tid, settar_oid);
                    showOrdOptionsDialog(settar_oid);
                    
                }
            }).setNegativeButton("Отмена", 
            		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                	showOrdOptionsDialog(settar_oid);
                }
            })
			.create();
		dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		if (!sound_lock)
			dlg.show();
		
		}	else	{
			this.showMyMsg("Не найдено тарифов для тар. плана заявки!");
			showOrdOptionsDialog(settar_oid);
		}
		
		}	else
			this.showMyMsg("Ошибка поиска текущей заявки в массиве!");
		}	else	{
			showOrdOptionsDialog(TDSocketClient.activeOrderID);
			this.showMyMsg("Не определено ни одного тарифа!");
		}
		}	else	{
			showOrdOptionsDialog(TDSocketClient.activeOrderID);
		}
		}	else
			this.showMyMsg("Надо иметь статус ЗАНЯТ!");
	}
	
	public void backPressed()	{
		if (timerIsActive)	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(
    				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
            
    		AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
                .setMessage("Активен таймер или таксометр, он будет обнулен и остановлен при выходе!")
                // кнопка "Yes", при нажатии на которую приложение закроется
                .setPositiveButton(R.string.msg_dialog_close_str, 
                		new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton) 
                        {
                        	closingFunc();
                        }
                    })
                .setNegativeButton("Отмена", 
            		new DialogInterface.OnClickListener()
                    {
                    	public void onClick(DialogInterface dialog, int whichButton) 
                    	{
                    		
                    	}
                    })
                .create();
            dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            if (!sound_lock)
            	dlg.show();
    	}	
    	else if (this.hasGps&&this.USE_GPS_TAXOMETER)	{
    		if (TaxometrSrvMode.serviceActive||TaxometrSrvMode.singleGPSActivating)	{
    			AlertDialog.Builder builder = new AlertDialog.Builder(
    					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
                
    			AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
                    .setMessage("Активен таймер или таксометр, он будет обнулен и остановлен при выходе!")
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	closingFunc();
                            }
                        })
                    .setNegativeButton("Отмена", 
            		new DialogInterface.OnClickListener()
                    {
                    	public void onClick(DialogInterface dialog, int whichButton) 
                    	{
                    		
                    	}
                    })
                    .create();
                dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                if (!sound_lock)
                	dlg.show();
    		}
    		else	{
    			closingFunc();
        	}
    	}
    	else	{
    		closingFunc();
    	}
	}
	
	public void closingFunc()	{
		if(!PASSIVE_NET_MODE)	{
	    	if (this.confirmLineOutOnExit)	{
	    		closeWithOutDialog();
	    	}
	    	else	{
	    		closeDialog();
	    	}
	    	}
	    	else	{
	    		closeDialog();
	    	}
	}
	
	public void closeOnlyDialog(boolean unsuccAutorization, boolean criticalParams)	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
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
                    	userInterrupt = true;
                    	//finish();
                    	Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
	        	        rintent.putExtra("msg_lbl_text", "---");
	        	        sendBroadcast(rintent);
                		stopSelf();
                    }
                }).setCancelable(false)
            .create();
        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!sound_lock)
        	dlg.show();
    }
	
	public void closeWithOutDialog()	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
            .setMessage("Сняться с линии?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	TDSocketClient.sendSignoutRequest();
                    }
                })
            .setNegativeButton("Нет", 
            		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                	closeDialog();
                }
            })
            .create();
        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!sound_lock)
        	dlg.show();
    }

	public void extraExit()	{
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));

		AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
				.setMessage("Ограничение безопасности, обратитесь " +
						"к разработчику!")
						// кнопка "Yes", при нажатии на которую приложение закроется
				.setPositiveButton(R.string.msg_dialog_close_str,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
								rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
								rintent.putExtra("msg_lbl_text", "---");
								sendBroadcast(rintent);
								userInterrupt = true;
								stopSelf();
							}
						})
				.create();
		dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		dlg.setCanceledOnTouchOutside(true);

		dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
				rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
				rintent.putExtra("msg_lbl_text", "---");
				sendBroadcast(rintent);
				userInterrupt = true;
				stopSelf();
			}
		});

		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
				rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
				rintent.putExtra("msg_lbl_text", "---");
				sendBroadcast(rintent);
				userInterrupt = true;
				stopSelf();
			}
		});

		dlg.show();
	}
	
	public void closeDialog()	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
            .setMessage("Закрыть приложение?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
	        	        rintent.putExtra("msg_lbl_text", "---");
	        	        sendBroadcast(rintent);
	        	        if(!dontShutdownService)	{
	        	        	userInterrupt = true;
                			stopSelf();
	        	        }
                		else
                			sound_lock=true;
                    }
                })
            .setNegativeButton("Отмена", 
            		new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                	
                }
            })
            .create();
        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!sound_lock)
        	dlg.show();
    }
	
	public void closeDialogOutline()	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
            .setMessage("Вы сняты с линии, приложение будет закрыто.")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_ACT_FINISH_SIGNAL);
	        	        rintent.putExtra("msg_lbl_text", "---");
	        	        sendBroadcast(rintent);
	        	        if(!dontShutdownService)	{
	        	        	userInterrupt = true;
                			stopSelf();
	        	        }
                		else
                			sound_lock=true;
                    }
                })
            .create();
        dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!sound_lock)
        	dlg.show();
    }
	//////////////////////////////////////
	//////Диалоговые дупликаты для сервиса вызываемые в том числе через 
	//////Broadcast к сервису, либо сокет классом в сервисе
	//////////////////////////////////////
	
	/////////////////////////////////////
	///////Функции таксометра которые надо дуплицировать, обращаются к интерфейсу Activity
	/////////////////////////////////////
	
	public void rebuildSocketThreadAndService(boolean directReStart, boolean onCreateInit, boolean activityStart)	{
		if(!PASSIVE_NET_MODE)	{
		if(this.SOCKET_IN_SERVICE)
			if((TDSocketClient==null?true:
				TDSocketClient.interrupt)||(!onCreateInit)||directReStart)	{
				try	{
					if(TDSocketClient!=null)	{
						try	{
							TDSocketClient.stopSocket();
							TDSocketClient.interrupt=true;
						}	catch (Exception ex)	{
							
						}
					}
				TDSocketClient = new SocketClientSrvMode
					(this, server, port, alt_server, alt_port, 
					login_name, psw_str);
	        
		    	//playMP3(R.raw.guitar);
				playMP3(this.connectingTonePref);
		    	TDSocketClient.CHECK_CONNECTION = true;
		    	TDSocketClient.RECONNECT_NUMBERS = this.RECONNECT_NUMBERS;
		        TDSocketClient.startSocket();
				} catch (Exception e)	{
        			showMyMsg("Ошибка rebuildSocketThreadAndService: "+e.getMessage());
        		}
			}	else	{
				try	{
				if(TDSocketClient.autorized&&TDSocketClient.fastConnection)	{
					TDSocketClient.sendStatusQuery();
					TDSocketClient.sendSectorsStatusesQuery();
					TDSocketClient.sendFullStatusQuery();
				}
				} catch (Exception e)	{
					if(TDSocketClient!=null)	{
						try	{
							TDSocketClient.stopSocket();
							TDSocketClient.interrupt=true;
						}	catch (Exception ex)	{
							
						}
					}
					TDSocketClient = new SocketClientSrvMode
							(this, server, port, alt_server, alt_port, 
							login_name, psw_str);
			        
				    	playMP3(R.raw.guitar);
				    	TDSocketClient.CHECK_CONNECTION = true;
				    	TDSocketClient.RECONNECT_NUMBERS = this.RECONNECT_NUMBERS;
				        TDSocketClient.startSocket();
        			//showMyMsg("Ошибка rebuildSocketThreadAndService: "+e.getMessage());
        		}
			}

		}
		else	{
			this.showDialogElement(TDDialog.TB_SHOW_MSG,
					"Программа работает в техническом режиме без сетевого обмена!");
		}
	}
	
	public void playMP3(int res_id)	{
    	if (!sound_lock)	{
	    	MediaPlayer mediaPlayer;
	    	mediaPlayer = MediaPlayer.create(this, res_id); 
	    	mediaPlayer.setVolume(1, 1);
	    	mediaPlayer.start();
    	}
    }
	
	public boolean checkString(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
	
	public int strToIntDef(String str_int, int def) {
        int res = def;
        
        if (checkString(str_int)) {
            res = Integer.parseInt(str_int);
        }
        
        return res;
    }
	
	public void resetHiddenTime()	{
		this.sockOrderTimer.hiddenTime=0;
	}
	
	public void readSettings()	{
		
	}
	
	public void sendBCast(Intent bintent)	{
		sendBroadcast(bintent);
	}
	
	public void initService(boolean onCreateInit, boolean activityStart)	{
		try	{
			hasFirstDetect = false;
			hasFirstDetect2 = false;
			hasFirstDetect3 = false;
			hasFirstDetectMissing = false;
			hasFirstDetectSending = false;
			prefs = PreferenceManager.
	        		getDefaultSharedPreferences(this);
			if (prefs!=null)	{
				gpsMonitoring = prefs.getBoolean("USE_GPS", false);
				login_name = prefs.getString("USER_LOGIN", "");
		        psw_str = prefs.getString("USER_PSW", "");
		        server = prefs.getString("IP1", "");
		        port = prefs.getString("IP1_PORT", "");
		        WSS1_PORT = prefs.getString("WSS1_PORT", "8088");
		        alt_server = prefs.getString("IP2", "");
		        alt_port = prefs.getString("IP2_PORT", "");
		        RESTORE_TAXOMETR = prefs.getBoolean("RESTORE_TAXOMETR", false);
		        TAXOMETR_AS_SERVICE = prefs.getBoolean("TAXOMETR_AS_SERVICE", false)&&false;
				SEND_CURR_COORDS = prefs.getBoolean("SEND_CURR_COORDS", false);
				ALARM_ORDER_CONFIRM = prefs.getBoolean("ALARM_ORDER_CONFIRM", false);
				MANUAL_SECTOR_REFRESH = prefs.getBoolean("MANUAL_SECTOR_REFRESH", false);
				REGION_PHONE_CODE = prefs.getString("REGION_PHONE_CODE", "86133");
				STATE_PHONE_CODE = prefs.getString("STATE_PHONE_CODE", "+7");
				STATE_PHONE_CODE = STATE_PHONE_CODE.trim();
				USE_GPS_TAXOMETER = prefs.getBoolean("USE_GPS_TAXOMETER", false);
		        USE_NETWORK_LOCATION = prefs.getBoolean("USE_NETWORK_LOCATION", false);
		        USE_BOTH_LOCATIONS = prefs.getBoolean("USE_BOTH_LOCATIONS", false);
		        CONFIRM_WIFI_ENABLED = prefs.getBoolean("CONFIRM_WIFI_ENABLED", false);
		        PASSIVE_NET_MODE = prefs.getBoolean("PASSIVE_NET_MODE", false);
		        requestBalanceStart=prefs.getBoolean("REQ_BALANCE_START", false);
		        confirmLineOutOnExit=prefs.getBoolean("CONF_LINEOUT_EXIT", false);
		        USE_CALC_SPEED_DIST=prefs.getBoolean("USE_CALC_SPEED_DIST", true);
		    	USE_SENS_SPEED_DIST=prefs.getBoolean("USE_SENS_SPEED_DIST", false);
		    	USE_TIME_DIST_BALANCE=prefs.getBoolean("USE_TIME_DIST_BALANCE", false);
				SHOW_KOPS_IN_SUMM=prefs.getBoolean("SHOW_KOPS_IN_SUMM", false);
		    	TAXOMETR_INCCALL_ABORT=prefs.getBoolean("TAXOMETR_INCCALL_ABORT", false);
		    	CALC_SALE_DINAMYC=prefs.getBoolean("CALC_SALE_DINAMYC", false);
		       	SLEEP_TIME_STDIST = prefs.getBoolean("SLEEP_TIME_STDIST", false);
		       	WAIT_DLG_AUTO=prefs.getBoolean("WAIT_DLG_AUTO", false);
		       	WAIT_DLG_WITH_SECT=prefs.getBoolean("WAIT_DLG_WITH_SECT", false);
				HIDE_OTH_SECT_WAIT_ORDS=prefs.getBoolean("HIDE_OTH_SECT_WAIT_ORDS", false);
				LOCK_FREE_ORDERS_INFO=prefs.getBoolean("LOCK_FREE_ORDERS_INFO", false);
		       	CHECK_TARIF_AREA=prefs.getBoolean("CHECK_TARIF_AREA", false);
		       	RESET_LOST_BTIME=prefs.getBoolean("RESET_LOST_BTIME", false);
		       	SOCKET_IN_SERVICE=prefs.getBoolean("SOCKET_IN_SERVICE", true);
				START_TIME_CALC_WITH_MENU = prefs.getBoolean("START_TIME_CALC_WITH_MENU", false);
				USE_NEW_COORD_LOC_ALGORYTHM = prefs.getBoolean(
						"USE_NEW_COORD_LOC_ALGORYTHM", false);
				AUTO_DETECT_SECTOR = prefs.getBoolean(
						"AUTO_DETECT_SECTOR", false);
		       	TAXOMETR_INCCALL_ABORT = TAXOMETR_INCCALL_ABORT&&(SOCKET_IN_SERVICE||
		       			TAXOMETR_AS_SERVICE);
		       	
		       	mapsYandexPrefix=prefs.getString("GEOCODE_PREFIX", "Анапа+");
		       	FIXED_OVERST_DSUMM=this.strToIntDef(
		    			prefs.getString("FIXED_OVERST_DSUMM", "0"),0);
		       	MAX_ORDER_PRICE=this.strToIntDef(
		    			prefs.getString("MAX_ORDER_PRICE", "0"),0);
		       	SOCK_CONN_TIMEOUT=this.strToIntDef(
		    			prefs.getString("SOCK_CONN_TIMEOUT", "0"),0);
		       	TMETER_MIN_DISTANCE=this.strToIntDef(
		    			prefs.getString("TMETER_MIN_DISTANCE", "25"),25);
		    	TMETER_MAX_DISTANCE=this.strToIntDef(
		    			prefs.getString("TMETER_MAX_DISTANCE", "200"),200);
		    	TMETER_MIN_SPEED=this.strToIntDef(
		    			prefs.getString("TMETER_MIN_SPEED", "36"),36);
		    	TMETER_MAX_SPEED=this.strToIntDef(
		    			prefs.getString("TMETER_MAX_SPEED", "140"),140);
		       	RECONNECT_NUMBERS=this.strToIntDef(
		    			prefs.getString("RECONNECT_NUMBERS", "100"),100);
		       	if(RECONNECT_NUMBERS<100)
		    		RECONNECT_NUMBERS=100;
		       	
		       	START_BACK_TIME=this.strToIntDef(
		    			prefs.getString("START_BACK_TIME", "5"),5)*60;
		    	int RBT=this.strToIntDef(
		    			prefs.getString("REGULAR_BACK_TIME", "0"),0);
		    	REGULAR_BACK_TIME=((RBT>0)?RBT*60:15);
		    	REGULAR_BACK_DISTANCE=this.strToIntDef(
		    			prefs.getString("REGULAR_BACK_DISTANCE", "30"),30);
		    	START_BACK_DISTANCE=this.strToIntDef(
		    			prefs.getString("START_BACK_DISTANCE", "0"),0);
				loadDataFromSocketIO = prefs.getBoolean(
						"USE_DRIVERS_SOCKET_SERVER", false);
		    	
		    	backPortionsCount=REGULAR_BACK_TIME/3;
		    	if(backPortionsCount<=0) backPortionsCount=1;
		    	backDistPortions = new double[backPortionsCount];
		       	
				svrInetAddr = prefs.getString("GPS_SRV_ADR", "");
		    	if (svrInetAddr.length()==0)
		    		svrInetAddr = prefs.getString("IP1", "")+":8080";
		    	accountId = prefs.getString("GPS_ACC_ID", "demo");
		    	devId = prefs.getString("GPS_DEV_ID", "demo");
		    	TRACK_INTERVAL = strToIntDef(prefs.getString("TRACK_INTERVAL", "0"),0);
		    	TRACK_DISTANCE = strToIntDef(prefs.getString("TRACK_DISTANCE", "0"),0);
		    	if ((TRACK_DISTANCE<=0)||(TRACK_DISTANCE>499))
		    		TRACK_DISTANCE = 10;
		    	
		    	try	{
		    		freeOrdTonePref = this.getResources().getIdentifier(
		    			prefs.getString("freeOrdTonePref", "bonus"), 
		    			"raw", "com.psdevelop.tdandrapp");
		    		if(freeOrdTonePref<=0)
		    			freeOrdTonePref = R.raw.bonus;
		    	} catch(Exception e)	{
		    		freeOrdTonePref = R.raw.bonus;
		    		this.showMyMsg("Ошибка распознавания freeOrdTonePref!");
		    	}

                try	{
                    connectingTonePref = this.getResources().getIdentifier(
                            prefs.getString("connectingTonePref", "connect"),
                            "raw", "com.psdevelop.tdandrapp");
                    if(connectingTonePref<=0)
                        connectingTonePref = R.raw.connect;
                } catch(Exception e)	{
                    connectingTonePref = R.raw.connect;
                    this.showMyMsg("Ошибка распознавания connectingTonePref!");
                }

                try	{
                    assignOrdTonePref = this.getResources().getIdentifier(
                            prefs.getString("assignOrdTonePref", "order_set"),
                            "raw", "com.psdevelop.tdandrapp");
                    if(assignOrdTonePref<=0)
                        assignOrdTonePref = R.raw.order_set;
                } catch(Exception e)	{
                    assignOrdTonePref = R.raw.order_set;
                    this.showMyMsg("Ошибка распознавания assignOrdTonePref!");
                }

				try	{
					reportOrdTonePref = this.getResources().getIdentifier(
							prefs.getString("reportOrdTonePref", "order_report"),
							"raw", "com.psdevelop.tdandrapp");
					if(reportOrdTonePref<=0)
						reportOrdTonePref = R.raw.order_report;
				} catch(Exception e)	{
					reportOrdTonePref = R.raw.order_report;
					this.showMyMsg("Ошибка распознавания reportOrdTonePref!");
				}
		    	
		    	GPSIsActive=false;
		        
		        if (hasGps&&(USE_GPS_TAXOMETER||prefs.getBoolean("USE_GPS", false))&&
		        		(this.TAXOMETR_AS_SERVICE||this.SOCKET_IN_SERVICE))	{
		        	checkGPSActive();
		        }	else	{
		        	//showMyMsg("Устройство GPS отсутствует!");
		        }
		        
		        if (USE_GPS_TAXOMETER&&(!hasGps||!GPSIsActive)&&
		        		(this.TAXOMETR_AS_SERVICE||this.SOCKET_IN_SERVICE))	{
		        	this.showDialogElement(TDDialog.TB_SHOW_MSG,
						"Отсутствует или не включено устройство GPS, таксометр не будет подсчитывать расстояние!");
		        }
		        
		    	// TODO Auto-generated constructor stub
		        if((prefs.getBoolean("USE_GPS", false)||SEND_CURR_COORDS)&&hasGps)	{
		    	try	{
						myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				        //Назначаем слушателя
						//checkGPSEnabled();
				        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				        		TRACK_INTERVAL, TRACK_DISTANCE, this);
				        
				        GpsStatus.Listener lGPS = new GpsStatus.Listener() {
							public void onGpsStatusChanged(int event) {
								try	{
								if( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
						            //GpsStatus status = lm.getGpsStatus(null); 
						            //Iterable<GpsSatellite> sats = status.getSatellites();
						            //doSomething();
						        	int satellites = 0;
						            int satellitesInFix = 0;
						            //int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
						            //Log.i(TAG, "Time to first fix = " + timetofix);
						            for (GpsSatellite sat : myManager.getGpsStatus(null).getSatellites()) {
						                if(sat.usedInFix()) {
						                    satellitesInFix++;              
						                }
						                satellites++;
						            }
						            if(SOCKET_IN_SERVICE) 
							            if((tmeter!=null?tmeter.serviceActive:false))	{
							            	//tmeter.satellites=satellites;
							            	//tmeter.satellitesInFix=satellitesInFix;
							            }	else	{
							            	Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
						        	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_STATUS);
						        	        rintent.putExtra("msg_lbl_text", clientVersion+
						        	        		(satellites>0?"|C"+satellites:"|")+
						        					(satellitesInFix>=0&&satellites>0?"["+satellitesInFix+"]|":"|"));
						        	        sendBroadcast(rintent);
							            	
							            }
						        }
								} catch(Exception gpse)	{
									showMyMsg("Ошибка определения статуса GPS! "+gpse.getMessage());
								}
						    }
						};
						myManager.addGpsStatusListener(lGPS);
				        
				        if(prefs.getBoolean("USE_GPS", false))	{
				        	gpsScan = new GpsScanner(this);
				        	gpsScan.start();
				        	serviceActive = true;
				        	//this.showMyMsg("Активен сервис B!");
				        }	else	{
				        	//this.showMyMsg("Активен сервис B-!");
				        }
		    	} catch(Exception le)	{
		    		showMyMsg("Ошибка инициализации сервиса В!");
		    	}
		        } else	{
		        	try	{
		        		if (gpsScan!=null)	{
		        			gpsScan.stop();
		        		}
		        		if(myManager!=null)	{
		        			myManager.removeUpdates(this);
		        		}
		        	} catch(Exception le)	{
		    		showMyMsg("Ошибка остановки сервиса В!");
		        	}
		        }
		        
		        
		        if(this.RESTORE_TAXOMETR||this.TAXOMETR_AS_SERVICE||this.SOCKET_IN_SERVICE)	{
		    		
					if (this.TAXOMETR_AS_SERVICE||this.SOCKET_IN_SERVICE)	{
						if(sockOrderTimer==null)
							this.sockOrderTimer = new OrderTimerSrvMode(this);
						if (USE_GPS_TAXOMETER&&hasGps)	{
							if(tmeter==null)	{
								tmeter = new TaxometrSrvMode(this);
								//showMyMsg("Активен таксометр в сервисном режиме!");
							}
				        }
					}
					
					//if(this.RESTORE_TAXOMETR)
					//	showMyMsg("СЛУЖБА: Активен сервис восстановления основного рабочего процесса!");
					
				}
		        
		        if(!onCreateInit)
		        	this.userInterrupt = false;
		        rebuildSocketThreadAndService(false, onCreateInit, activityStart);

		        
			}
			} catch(Exception stse)	{
	    		showMyMsg("Ошибка инициализации фоновой службы!");
	    	}
	}
		  
	public int onStartCommand(Intent intent, int flags, int startId) {
		try	{
			if(this.userInterrupt)	{
				showMyMsg("Outher trying to td service... Stopped by user interrupt");
				stopSelf();
			}
			else if(intent.hasExtra("cactivity_start"))	{
				sound_lock=false;
				initService( imActive, true);
				//showMyMsg("TD service start "+(imActive?"second":"first"));
				//this.showDialogElement(TDDialog.TB_SHOW_MSG, "TD service start "+(imActive?"second":"first"));
			}
			else	{
				sound_lock=true;
				//initService( imActive, sound_lock);
				showMyMsg("Outher trying to td service... Stopped");
				//stopSelf();
			}
			if(this.SOCKET_IN_SERVICE)	{
				Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
		        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_STATUS);
		        rintent.putExtra("msg_lbl_text", clientVersion);
		        sendBroadcast(rintent);
			}
		} catch(Exception ex)	{
			//showMyMsg("Err onStartCommand! "+ex.getMessage());
			sound_lock=true;
		}
		imActive = true;
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		try	{
			wakeLock.release();
		} catch(Exception e)	{
			this.showMyMsg("Ошибка wakeLock release службы!");
		}
		super.onDestroy();
		//showMyMsg("");
	}
	
	public String getCurrTMData()	{
		try	{
			DecimalFormat df = new DecimalFormat("#.###");
			return "("+df.format((double)(this.tmeter!=null?this.tmeter.summaryDist:0)/1000.0)+"км "+
				this.timerValue/60+"мин "+this.timerValue%60+"сек)";
		} catch(Exception e)	{
			return "(==="+e.getMessage()+")";
		}
	}
	
	public void showMyMsg(String message)   {
    	try {
    		if(!this.sound_lock)	{
	        	Toast alertMessage = Toast.makeText(getBaseContext(),//getApplicationContext(), 
	        			"СООБЩЕНИЕ: "
	        			+message, Toast.LENGTH_LONG);
	        	alertMessage.show();
    		}
        } catch(Exception ex)   {
        }
    }
	
	public void showServerMsg(String message)   {
        try {
        	if(!this.sound_lock)	{
	        	Toast alertMessage = Toast.makeText(getBaseContext(), 
	        			"СООБЩЕНИЕ СЕРВЕРА: "
	        			+message, Toast.LENGTH_LONG);
	        	alertMessage.show();
        	}
        } catch(Exception ex)   {
        }
    }
	
	public void showCustomMsg(String caption, String message)   {
        try {
        	if(!this.sound_lock)	{
	        	Toast alertMessage = Toast.makeText(getBaseContext(), 
	        			caption+": "
	        			+message, Toast.LENGTH_LONG);
	        	alertMessage.show();
        	}
        } catch(Exception ex)   {
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		showMyMsg("onBind");
		return null;
	}
	
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		try	{
			if (location!=null)	{
				double newLatitude=location.getLatitude();
				double newLongitude=location.getLongitude();

				if (this.AUTO_DETECT_SECTOR && this.checkSectorCounter >= 5) {
					checkCurrentSector(newLatitude, newLongitude);
				}

				float newSpeed=location.getSpeed();
				if (this.SEND_CURR_COORDS&&this.SOCKET_IN_SERVICE
						&&(TDSocketClient!=null)&&((System.currentTimeMillis()-lastCCSendTime)>60000 ||
                        (TDSocketClient.sendCCoordsByMDelta && ( (Math.abs(lastNewLat - newLatitude) >= 0.001) || (Math.abs(lastNewLon - newLongitude) >= 0.001) )) ))	{
					lastCCSendTime = System.currentTimeMillis();
					lastNewLat = newLatitude;
					lastNewLon = newLongitude;
					TDSocketClient.sendLastCurrCoords(newLatitude, newLongitude);
					if(!hasFirstDetect2) {
						try {
							String devicIMEI = "none";
							/*try {
								TelephonyManager telephonyManager =
									(TelephonyManager) getSystemService(TELEPHONY_SERVICE);
								devicIMEI = telephonyManager.getDeviceId();
							} catch(Exception e)	{
							}*/
						} catch(Exception e)	{

						}
						//showMyMsg("cc:lat="+newLatitude+",lon="+newLongitude);
					}
					this.hasFirstDetect2 = true;
				}
				//if(hasFirstDetect3) showMyMsg("cchng:lat="+newLatitude+
				//		",lon="+newLongitude+",dt="+this.SEND_CURR_COORDS+","+
				//		((TDSocketClient!=null)&&((System.currentTimeMillis()-lastCCSendTime)>60000)));
				//hasFirstDetect3 = true;
				if (gpsMonitoring)	{
					this.gpsScan.hasNewGPSDetection = true;
					this.lastLatitude = newLatitude;
					this.lastLongitude = newLongitude;
					this.lastSpeed = newSpeed;
					if (!hasFirstDetect&&this.showGPSSysEvents)	{
						showMyMsg("WG:"+newLatitude+ 
							"\nLN:"+newLongitude+ 
							"\nSP:"+newSpeed +"\n");
						this.hasFirstDetect = true;
					}
				}
			}
		} catch (Exception le)	{
			showMyMsg("Ошибка считывания сервис В!");
		}
	}
	
	///////////////////////////////////////
	///////отправка сообщений визуальному окну
	///////////////////////////////////////
	public void showTimerVal()	{
		Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_SHOW_TIMER_VAL);
        rintent.putExtra("BACK_TIME", this.BACK_TIME);
        rintent.putExtra("timerValue", timerValue);
        sendBroadcast(rintent);
	}
	///////////////////////////////////////
	///////отправка сообщений визуальному окну
	///////////////////////////////////////
	
	///////////////////////////////////////
	////////////Функции-дупликаты с сервисом(стаб.работа в нем)
	///////////////////////////////////////
	public boolean checkActiveNetMode()	{
		if(!PASSIVE_NET_MODE)	{
			if(this.TDSocketClient!=null)	{
				if(this.TDSocketClient.fastConnection&&!
						this.TDSocketClient.interrupt)	{
					return true;
				}
				else
				{
					playMP3(R.raw.critical);
					
					if(this.TDSocketClient.interrupt)	{
						showMyMsg("Соединение оборвано, пытаюсь соединиться!");
						this.rebuildSocketThreadAndService(true, true, false);
					}
					else	{
						showMyMsg("Соединение оборвано, пытаюсь соединиться!");
					}
					return false;
				}
			}
			else
			{
				playMP3(R.raw.critical);
				showMyMsg("Соединение оборвано, перезагружаю!");
				this.rebuildSocketThreadAndService(true, true, false);
				return false;
			}
		}	else	{
			playMP3(R.raw.critical); 
			showMyMsg("Соединение неактивно, ряд операций недоступен!");
			return false;
		}
	}
	
	public void sendCOSP(String sdata)	{
		if(TDSocketClient!=null)	{
			TDSocketClient.sendCOSP(sdata);
		}
	}
	
	public static boolean timerProcessing()	{
		return timerIsActive||TaxometrSrvMode.serviceActive||TaxometrSrvMode.singleGPSActivating;
	}
	///////////////////////////////////////
	////////////Функции-дупликаты с сервисом(стаб.работа в нем)
	///////////////////////////////////////
	
	/////////////////////////////////////
	///////Функции таксометра которые надо дуплицировать, обращаются к интерфейсу Activity
	/////////////////////////////////////
	public void startOrderTimer()	{
		//this.showMyMsg("on-off timer");
		boolean tmeterActiveOrNone=false;
		this.orderHistory = "";
		//startTDService();
		if(hasGps&&USE_GPS_TAXOMETER)	{
			checkGPSActive();
		if (!TaxometrSrvMode.serviceActive)	{
			
			if(!PASSIVE_NET_MODE)	{
				if (TDSocketClient.clientStatus==
		                Driver.IN_WORKING) {
					tmeter.requestLUpd(false);
					tmeter.tmeterOrderId=TDSocketClient.activeOrderID;
					tmeterActiveOrNone=true;
					//if(orderHistory.indexOf("#"+TDSocketClient.activeOrderID)<0)
					//	orderHistory = orderHistory +"Заказ #"+TDSocketClient.activeOrderID;
					prev_summ=0;
	            	last_summ=0;
	            	orderHistory = "Заказ #"+TDSocketClient.activeOrderID;
				}
		        else
		        {
		            showCustomMsg("Ошибка действия",
		                "Для запуска таксометра надо иметь статус ЗАНЯТ!");
		        }
			}
			else	{
				tmeter.requestLUpd(false);
				tmeter.tmeterOrderId="test_id";
				prev_summ=0;
            	last_summ=0;
				orderHistory = "Заказ-оффлайн";
				tmeterActiveOrNone=true;
			}
			
			}	else	{
				//resetTmeter(false,true,false);
				tmeter.removeLUpd(false);
			}
		}	else
		{
			tmeterActiveOrNone=true;
		}
		
		
		if (!timerIsActive&&tmeterActiveOrNone)	{
			if(!PASSIVE_NET_MODE)	{
				if (TDSocketClient.clientStatus==
                Driver.IN_WORKING) {
					startMillis=System.currentTimeMillis();
					timerIsActive=true;
					timerValue=0;
					timerOrderId=TDSocketClient.activeOrderID;
					prev_summ=0;
	            	last_summ=0;
	            	orderHistory = "Заказ #"+TDSocketClient.activeOrderID;
				}	else
		        {
		            showCustomMsg("Ошибка действия",
		                "Для запуска таймера надо иметь статус ЗАНЯТ!");
		        }
			}	else
			{
				startMillis=System.currentTimeMillis();
				timerIsActive=true;
				timerValue=0;
				timerOrderId="test_id";
				prev_summ=0;
            	last_summ=0;
				orderHistory = "Заказ-оффлайн";
			}
        }
        
		else	{
			if (!timerIsActive&&!tmeterActiveOrNone&&
					hasGps&&USE_GPS_TAXOMETER)	{
				this.showDialogElement(TDDialog.TB_SHOW_MSG,
						"Отключен таксометр GPS (возможно обрыв "+
						"связи и нет достоверной информации о "+
						"заказе, дождитесь соединения), таймер останется неактивным!");
			}
			resetTimer(false,false||tmeterActiveOrNone,true);
		}
		
		if(USE_TIME_DIST_BALANCE&&timerIsActive)	{
			this.BACK_TIME=this.START_BACK_TIME;
		}	else	{
			this.BACK_TIME=0;
		}
		
		showTimerVal();
		
	}
	
	public void resetTaxometrProcess()	{
		boolean bl=timerIsActive;
		if(hasGps&&this.USE_GPS_TAXOMETER)	{
			if (tmeter!=null)	{
				bl=bl||TaxometrSrvMode.serviceActive;
				if(TaxometrSrvMode.serviceActive)	{
					tmeter.removeLUpd(false);
					tmeter.summaryDist=0;
					tmeter.showTMeter(tmeter.summaryDist, TaxometrSrvMode.LOCATION_NONE);
				}
			}
			if(timerIsActive)	{
				resetTimer(false,false,false);
				showTimerVal();
			}
			if(bl)	{
				this.showDialogElement(TDDialog.TB_SHOW_MSG, 
					"Таксометр остановлен так как поменялась заявка!");
			}
		}
	}

	public void checkCurrentSector(double lat, double lon) {
		for (int i = 0; i < TDSocketClient.workSectors.size(); i++) {
			VectorSectorItem sector = TDSocketClient.workSectors.elementAt(i);

			if (!sector.sectorId.equals(TDSocketClient.activeSectorID) &&
					(!TDSocketClient.autoDetectSendedSID.equals(sector.sectorId) ||
							TDSocketClient.autoDetectSendedSID.length() == 0) &&
					sector.isPointInsideSector(lon, lat)) {
				TDSocketClient.autoDetectSendedSID = sector.sectorId;
				String v_obj = "{\"command\":\"change_sector\"," +
						"\"sector_id\":\"" + sector.sectorId + "\",\"client_id\":\"" +
						TDSocketClient.clientId + "\",\"msg_end\":\"ok\"}";
				TDSocketClient.wrapper.
						sendToServer(v_obj);
				//showMyMsg("Автосмена сектора на " + sector.sectorName);
				break;
			}
		}
	}

	public void checkCurrTarif(boolean always)	{
		//////////////////////////////
		if(TDSocketClient.clientStatus==Driver.IN_WORKING&&(CHECK_TARIF_AREA||always)&&(this.lastLocation!=null))	{
			if(TDSocketClient.ordersTarifs.size()>0)	{
			int act_ord_index = TDSocketClient.getActiveOrderIndex();
			if(act_ord_index>=0)	{
				int tarif_id=TDSocketClient.activeOrders.
						elementAt(act_ord_index).ordTariffId;
				int k=-1;
				//String tids="";
				double[] loc = new double[2];
				loc[0] = this.lastLocation.getLatitude();
				loc[1] = this.lastLocation.getLongitude();
				
				for(int i=0;i<TDSocketClient.ordersTarifs.size();i++)	{
					//tids+="["+TDSocketClient.ordersTarifs.elementAt(i).id+"]";
					if(TDSocketClient.ordersTarifs.elementAt(i).id == tarif_id)	{
						k=i;
						break;
					}
				}
				if (k >= 0)	{
					int areaDetectMode = this.USE_NEW_COORD_LOC_ALGORYTHM ? 1 : 0;

					String ctarCoordsTxt="coords:";
					OrderTarif currentTariff = TDSocketClient.ordersTarifs.elementAt(k);

					/*for (int j = 0; j < currentTariff.areaLines.size(); j++) {
						ctarCoordsTxt += (j + ".[lat=" +
								currentTariff.areaLines.elementAt(j).lat +
								",long="+currentTariff.areaLines.elementAt(j).lon+"] ");
					}*/
					/*
					if (always)
						this.showMyMsg("Посл. коорд: lat=" + loc[0] + ",long=" + loc[1] +
							", у тек тарифа " + TDSocketClient.ordersTarifs.elementAt(k).tarif_name +
							" область: " + ctarCoordsTxt);
					*/

					DecimalFormat df = new DecimalFormat("#.###");

					if (!currentTariff.inTarifArea(loc, false, areaDetectMode) &&
							currentTariff.outherAreaTarifId > 0)	{
						this.showMyMsg("Находится вне зоны тарифа, назначаем тариф с ИД="+
								currentTariff.outherAreaTarifId);

						String message = ("[вне зоны т." + currentTariff.id +
								",detectMode=" + areaDetectMode + "," + ctarCoordsTxt + ",> т." +
								currentTariff.outherAreaTarifId + ", (lat:" +
								df.format(loc[0]) + ",lon:" + df.format(loc[1]) + ")]");
						this.sendOrderHistory(message);

						TDSocketClient.applySelectOrderTariff(
								currentTariff.outherAreaTarifId, TDSocketClient.activeOrderID);
						return;
					}

					for (int m = 0; m < TDSocketClient.ordersTarifs.size(); m++)	{
						OrderTarif holdTariff = TDSocketClient.ordersTarifs.elementAt(m);
						if (holdTariff.inTarifArea(loc, true, areaDetectMode) &&
								k != m && holdTariff.tplan_id == currentTariff.tplan_id &&
								holdTariff.id < currentTariff.id)	{
							this.showMyMsg("Находится в зоне тарифа ИД=" + holdTariff.id);

							String message = ("[тек. т." + currentTariff.id + ", в зоне > т." +
									holdTariff.id + ", (lat:" +
									df.format(loc[0]) + ",lon:" + df.format(loc[1]) + ")]");
							this.sendOrderHistory(message);

							TDSocketClient.applySelectOrderTariff(
									holdTariff.id, TDSocketClient.activeOrderID);
						}
					}
					
				}
				else {
					if (always)	{
						this.showMyMsg("Не найден тариф по ид тарифа заявки, ид="+tarif_id);//+". tids="+tids
					}
				}
			}
			else	{
				if(always)	{
					this.showMyMsg("Не определен индекс активной заявки!");
				}
			}
			}
			else	{
				if(always)	{
					this.showMyMsg("Для текущего тарифа не определена область!");
				}
			}
		} else	{
			if(always)	{
				this.showMyMsg("Для проверки надо быть на заявке!");
			}
			if (this.lastLocation==null)	{
				this.showMyMsg("Не определены последние текущие координаты!");
			}
		}
	}

	public void sendOrderHistory(String message) {
		orderHistory += message;
		TDSocketClient.sendOrderHistory(TDSocketClient.activeOrderID, orderHistory,
				last_summ);
	}
	
	public boolean activeTimerConflict(boolean show_dlg)	{
		if (timerIsActive&&
                (!(timerOrderId.
                		equals(TDSocketClient.activeOrderID))||
                		(TDSocketClient.clientStatus==
      					Driver.FREE_DRIVER)) )	{
			
		if(show_dlg)	{
			AlertDialog.Builder time_builder = new AlertDialog.Builder(
					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));//, android.R.style.Theme_Dialog);
			AlertDialog dlg = time_builder.setTitle("Внимание...")
            .setMessage("Таймер будет сброшен так как водитель свободен, либо таймер стартовал на другой заявке!")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Прервать", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	resetTimer(false,false,false);
            			showTimerVal();
                    }
                }).create();
			//dlg.setInverseBackgroundForced(forceInverseBackground);
			dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (!sound_lock)
				dlg.show();
		}
		else	{
			resetTimer(false,false,false);
			showTimerVal();
			this.showMyMsg("Коинфликт состояний или заказов, таймер остановлен!");
		}
				/*.setNegativeButton("Прервать", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	resetTimer(false,false,false);
            			showTimerVal();
                    }
                })*/
                
			
			return true;
		}	else {
			return false;
		}
	}
	
	public boolean activeTmeterConflict(boolean show_dlg)	{
		if(hasGps&&USE_GPS_TAXOMETER)	{
		if (TaxometrSrvMode.serviceActive&&
                (!(tmeter.tmeterOrderId.
                		equals(TDSocketClient.activeOrderID))||
                		(TDSocketClient.clientStatus==
      					Driver.FREE_DRIVER)) )	{
			
			if(show_dlg)	{
			AlertDialog.Builder time_builder = new AlertDialog.Builder(
					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
	        
			AlertDialog dlg = time_builder.setTitle("Внимание...")
            .setMessage("Таксометр будет остановлен так как водитель свободен, либо таксометр стартовал на другой заявке!")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Прервать", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	tmeter.removeLUpd(false);
            			tmeter.summaryDist=0;
            			tmeter.showTMeter(tmeter.summaryDist, TaxometrSrvMode.LOCATION_NONE);
            			resetTimer(false,false,false);
            			showTimerVal();
                    }
                }).create();
			dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (!sound_lock)
				dlg.show();
			}
			else	{
				tmeter.removeLUpd(false);
    			tmeter.summaryDist=0;
    			tmeter.showTMeter(tmeter.summaryDist, TaxometrSrvMode.LOCATION_NONE);
    			resetTimer(false,false,false);
    			showTimerVal();
				this.showDialogElement(TDDialog.TB_SHOW_MSG, "Вы свободны или поменялся заказ, таксометр и таймер остановлены!");
				
			}/*.setNegativeButton("Прервать", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	tmeter.removeLUpd(false);
            			tmeter.summaryDist=0;
            			tmeter.showTMeter(tmeter.summaryDist, TaxometrSrvMode.LOCATION_NONE);
            			resetTimer(false,false,false);
            			showTimerVal();
                    }
                })*/
                
			
			return true;
		}	else {
			return false;
		}
			
		}	else
			return false;
	}
	
	public void resetTimer(boolean showOrderTime, boolean confirmDontReset, boolean keepTimeVal)	{
		
		final boolean keepTime = keepTimeVal;
		
		if (showOrderTime&&timerIsActive)	{
			this.showMyMsg("Время составило "+((int)(timerValue/3600))+
					" ч. "+((int)((timerValue/60)%60))+" мин.");
		}
		
		if (confirmDontReset&&timerIsActive)	{
			AlertDialog.Builder time_builder = new AlertDialog.Builder(
					new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
			AlertDialog dlg = time_builder.setTitle("Подтверждение действия...")
            .setMessage("Остановить таймер?")
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton("Да", 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	timerIsActive=false;
                    	if(!keepTime)
                    		timerValue=0; 
                    	//TDSocketClient.timerOrderId=null;
                    	
                    }
                }).setNegativeButton("Отмена", 
                		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
			dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (!sound_lock)
				dlg.show();
		}
		else	{
			timerIsActive=false;
			if(!keepTimeVal)
				timerValue=0;
			//TDSocketClient.timerOrderId=null;
			
		}
        showTimerVal();
	}
	
	/////////////////////////////////////
	///////Функции таксометра которые надо дуплицировать, обращаются к интерфейсу Activity
	/////////////////////////////////////
	
	//////////////////////////////////////
	//////Диалоговые дупликаты для сервиса вызываемые в том числе через 
    //////Broadcast к сервису, либо сокет классом в сервисе
	//////////////////////////////////////
	public void changeSector(boolean show_only)	{
      	
    	final boolean to_show_only = show_only;
    	Vector<String> sect_items = new Vector<String>();
    	for(int i=0;i<TDSocketClient.workSectors.size();i++)
    		sect_items.add(TDSocketClient.workSectors.
    			elementAt(i).sectorName+" ("+
    			TDSocketClient.workSectors.
    			elementAt(i).drCount+")");
    	
    	CharSequence[] charSequenceItems = sect_items.toArray
    			(new CharSequence[sect_items.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
        
    	AlertDialog dlg = builder.setTitle("ВЫБЕРИТЕ СЕКТОР").setItems(charSequenceItems, 
        	new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (!to_show_only)	{
            		String v_obj = "{\"command\":\"change_sector\","+
            			"\"sector_id\":\""+
            			TDSocketClient.workSectors.
            			elementAt(which).sectorId+
                        "\",\"client_id\":\""+
                        TDSocketClient.clientId
                        +"\",\"msg_end\":\"ok\"}";
            		TDSocketClient.wrapper.
                		sendToServer(v_obj);
            	}
            } } )
            // кнопка "Yes", при нажатии на которую приложение закроется
            .setPositiveButton(R.string.msg_dialog_close_str, 
            		new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	
                    }
                }).create();
    	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	if (!sound_lock)
    		dlg.show();
    }
	//////////////////////////////////////
	//////Диалоговые дупликаты для сервиса вызываемые в том числе через 
    //////Broadcast к сервису, либо сокет классом в сервисе
	//////////////////////////////////////
	
	//////////////////////////////////////////
	///////функции отчета по заявке
	//////////////////////////////////////////
    public void saleOrder()	{
    	TDSocketClient.saveCrashData();
    	TDSocketClient.clientStatus = 
    			TDSocketClient.lastConnectClientStatus;
    	if (TDSocketClient.clientStatus==
                Driver.IN_WORKING)  {
          			TDSocketClient.clientStatus = 
                            Driver.ON_ORDER_COMPLETING;
          			showDialogElement(TDDialog.TB_INP_TEXT,
    					"Введите сумму!");
          		}
          		else
    				showMyMsg("Вы не на исполнении заказа!");
    }
    
    public void saleOrderExt()	{
    	TDSocketClient.saveCrashData();
    	TDSocketClient.clientStatus = 
    			TDSocketClient.lastConnectClientStatus;
    	
    	if (TDSocketClient.clientStatus==
                Driver.IN_WORKING)  {
    			completingOrderId=TDSocketClient.activeOrderID;
		    		if (MANUAL_SECTOR_REFRESH)	{
		    			payOrderRefreshStarted = true;
		    			TDSocketClient.sendSectorsStatusesQuery();
		    			showMyMsg("Подождите, выполняется"+
		    		        	" запрос статусов секторов!");
		    		}
		    		else	{
		          			TDSocketClient.clientStatus = 
		                            Driver.ON_ORDER_COMPLETING;
		          			showDialogElement(TDDialog.TB_INP_TEXT,
		    					"Введите сумму!");
		    		}
          		}
          		else
    				showMyMsg("Вы не на исполнении заказа!");
    }
    
    public void showDialogElement(int dlg_type, String msg) {
    	try	{
    	switch(dlg_type)    {
            case TDDialog.TB_DECISION_WAIT:
            	AlertDialog.Builder dec_builder = new AlertDialog.Builder(
            			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
                
            	AlertDialog dlg = dec_builder.setTitle("ВСТАТЬ В ОЧЕРЕДЬ НА ЗАКАЗ?")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	if (TDSocketClient.clientStatus == 
                                        Driver.IN_DECISION) {
                                    TDSocketClient.clientStatus = 
                                            Driver.IN_ACCEPT_SYNC;
                                    
                                    TDSocketClient.operateFreezeState = true;
                                    String v_obj = "{\"command\":\"order_is_my\","+
                                        "\"order_id\":\""+
                                        TDSocketClient.potentialOrderID+
                                        "\",\"client_id\":\""+TDSocketClient.clientId
                                        +"\",\"msg_end\":\"ok\"}";
                                    TDSocketClient.wrapper.sendToServer(v_obj);

                                    showMyMsg(
                                            "Подождите, выполняется операция "+
                                            "постановки на заказ \""+
                                            TDSocketClient.
                                            potentialOrderData+"\"...");
                                }
                                else if (TDSocketClient.clientStatus == 
                                        Driver.IN_ACCEPT_DECITION)  {
                                    String v_obj = "{\"command\":\"accept_order\","+
                                        "\"order_id\":\""+
                                        TDSocketClient.acceptOrderID+
                                        "\",\"client_id\":\""+
                                        TDSocketClient.clientId
                                        +"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
                                    TDSocketClient.wrapper.
                                            sendToServer(v_obj);
                                    TDSocketClient.setOrderManualAccept(
                                        TDSocketClient.acceptOrderID, true);
                                    TDSocketClient.clientStatus =
                                        Driver.FREE_DRIVER;
                                }
                            }
                        }).setNegativeButton("Отмена", 
                        		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	
                            }
                        }).create();
            	dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            	if (!sound_lock)
            		dlg.show();
                break;
            case TDDialog.TB_NEXT_DECISION:
            	AlertDialog.Builder next_builder = new AlertDialog.Builder(
            			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
                
            	AlertDialog dlg2 = next_builder.setTitle("ВСТАТЬ В ОЧЕРЕДЬ НА СЛЕД. ЗАКАЗ?")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	TDSocketClient.
                                	acceptNextOrder=true;
                            	TDSocketClient.
                                	occupateNextOrder=false;
                            	TDSocketClient.
                                	acceptSyncNextOrder=false;
                            	TDSocketClient.
                                	showedNextOrder=false;
                            	String v_obj = "{\"command\":\"accept_order\","+
                            		"\"order_id\":\""+TDSocketClient.nextOrderID+
                            		"\",\"client_id\":\""+TDSocketClient.clientId
                            		+"\",\"manual\":\"yes\",\"msg_end\":\"ok\"}";
                            	TDSocketClient.wrapper.
                                	sendToServer(v_obj);
                            	TDSocketClient.setOrderManualAccept(
                            			TDSocketClient.nextOrderID, true);
                            }
                        }).setNegativeButton("Отмена", 
                        		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	
                            }
                        }).create();
            	dlg2.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            	if (!sound_lock)
            		dlg2.show();
                break;
            case TDDialog.TB_SHOW_MSG:
                
                AlertDialog.Builder builder = new AlertDialog.Builder(
                		new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));//getBaseContext());
                //new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog)
                AlertDialog dlg3 = builder.setTitle("СООБЩЕНИЕ СЕРВЕРА")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	
                            }
                        }).create();
                dlg3.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                if (!sound_lock)
                	dlg3.show();
                break;
            case TDDialog.TB_OPERATE_SYNC:
            	AlertDialog.Builder sync_builder = new AlertDialog.Builder(
            			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
                
            	AlertDialog dlg5 = sync_builder.setTitle("ВЫПОЛНЯЕТСЯ МНОГОШАГОВАЯ ОПЕРАЦИЯ!")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
                            	
                            }
                        }).create();
            	dlg5.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            	if (!sound_lock)
            		dlg5.show();
                break;
            case TDDialog.TB_INP_TEXT:
            	
            	//int act_ord_index = TDSocketClient.getActiveOrderIndex();
        		//if(act_ord_index>=0)	{
            	AlertDialog.Builder inp_builder = new AlertDialog.Builder(
            			new ContextThemeWrapper(getBaseContext(), android.R.style.Theme_Dialog));
            	
            	//final int orderDistTry = (int)((hasGps&&USE_GPS_TAXOMETER)?tmeter.summaryDist:0);
                //final long orderTimeTry = timerValue;
            	
            	boolean has_timer_conflict=activeTimerConflict(true);
            	boolean tact = false;
            	if (has_timer_conflict)	{
                	//
                }	else {
                if(timerIsActive)	{
                	tact = true;
                	resetTimer(true, false, true);
                }
                else
                	resetTimer(true, false, false);
                }
                
            	has_timer_conflict=has_timer_conflict||activeTmeterConflict(true);
                if(hasGps&&USE_GPS_TAXOMETER)
                if (TaxometrSrvMode.serviceActive)	{
                	tact = true;
                	tmeter.removeLUpd(false);
                }
                
                
                final int orderDist = (int)((hasGps&&USE_GPS_TAXOMETER)?tmeter.summaryDist:0);//(int)((hasGps&&USE_GPS_TAXOMETER)?
                		//(((tmeter.summaryDist-orderDistTry)>30)?orderDistTry:tmeter.summaryDist):0);
                final double orderTime = (double)((double)timerValue/60.0);//(int)(((timerValue-orderTimeTry)>20?orderTimeTry:timerValue)/60);
                //final int tplan_id = //
                //TDSocketClient.activeOrders.
                DecimalFormat df = new DecimalFormat("#.###");
                orderHistory+=(">отч("+df.format((double)((double)orderDist/1000.0))+
                		"км"+orderTime+"мин)");
                TDSocketClient.sendOrderHistory(TDSocketClient.activeOrderID, orderHistory,
    				last_summ);

				NumberFormat formatter = new DecimalFormat("#0.00");
                
            	final EditText input_text = new EditText(this);
            	input_text.setInputType(InputType.TYPE_CLASS_NUMBER);

				double actOrderPrevSumm = 0;
				double bonusSumm = 0;
				try {
					int now_act_ord_index = TDSocketClient.getActiveOrderIndex();
					if (now_act_ord_index >= 0) {
						VectorIstructItem activeOrder = TDSocketClient.activeOrders.
								elementAt(now_act_ord_index);
						actOrderPrevSumm = activeOrder.prevSumm;
						bonusSumm = activeOrder.bonusUse;
					}
				} catch(Exception e) {

				}

				bonusSumm = bonusSumm > 0 && !TDSocketClient.calcTaxometerSummWithBonus
						? bonusSumm
						: 0;

            	if (hasGps && USE_GPS_TAXOMETER && !has_timer_conflict)	{
					if (timerOrderId.equals(TDSocketClient.activeOrderID) ||
						tmeter.tmeterOrderId.equals(TDSocketClient.activeOrderID))	{
						double calcSumm = (tact?TDSocketClient.calculateTOSumm
								(orderTime, orderDist, this.START_BACK_DISTANCE, prev_summ):
								last_summ);

                        if (actOrderPrevSumm > 0 && prevSummOverTaxometr &&
								!(taxometerOverSmallPrevSumm && calcSumm > actOrderPrevSumm)) {
                            calcSumm = actOrderPrevSumm;
                        }

						calcSumm -= bonusSumm;
						calcSumm = calcSumm > 0 ? calcSumm : 0;
						input_text.setText(SHOW_KOPS_IN_SUMM ? formatter.format(calcSumm): (int)calcSumm +"");
						if(disableTMReportEdit || (actOrderPrevSumm > 0 && prevSummOverTaxometr)) {
                            input_text.setEnabled(false);
                        }
					} else {
						double calcSumm = TDSocketClient.calculateTOSumm
								(0, 0, this.START_BACK_DISTANCE, prev_summ);
						if (actOrderPrevSumm > 0 &&
								!(taxometerOverSmallPrevSumm && calcSumm > actOrderPrevSumm)) {
							calcSumm = actOrderPrevSumm;
						}
						if (((TDSocketClient.ordersOptions.size() > 0 ) || (actOrderPrevSumm > 0)) &&
							(calcSumm>0)) {

							calcSumm -= bonusSumm;
							calcSumm = calcSumm > 0 ? calcSumm : 0;
							input_text.setText(SHOW_KOPS_IN_SUMM ? formatter.format(calcSumm): (int)calcSumm +"");
						}

                        if(actOrderPrevSumm > 0 && disablePrevSumm) {
                            input_text.setEnabled(false);
                        }
					}
            	} else {
            		if (timerOrderId.equals(TDSocketClient.activeOrderID) &&
        					(TDSocketClient.calculateTOSumm(orderTime,
									0, this.START_BACK_DISTANCE, prev_summ)>0))	 {
						double calcSumm = (tact?TDSocketClient.calculateTOSumm
								(orderTime, 0, this.START_BACK_DISTANCE, prev_summ):last_summ);

                        if (actOrderPrevSumm > 0 && prevSummOverTaxometr &&
								!(taxometerOverSmallPrevSumm && calcSumm > actOrderPrevSumm)) {
                            calcSumm = actOrderPrevSumm;
                        }

						calcSumm -= bonusSumm;
						calcSumm = calcSumm > 0 ? calcSumm : 0;
						input_text.setText(SHOW_KOPS_IN_SUMM ? formatter.format(calcSumm): (int)calcSumm +"");

            			if(disableTMReportEdit || (actOrderPrevSumm > 0 && prevSummOverTaxometr)) {
							input_text.setEnabled(false);
						}
            		} else {
						double calcSumm = TDSocketClient.calculateTOSumm
								(0, 0, this.START_BACK_DISTANCE, prev_summ);
						if (actOrderPrevSumm > 0 &&
								!(taxometerOverSmallPrevSumm && calcSumm > actOrderPrevSumm)) {
							calcSumm = actOrderPrevSumm;
						}
						if (((TDSocketClient.ordersOptions.size() > 0) || (actOrderPrevSumm > 0)) &&
            					(calcSumm>0)) {

							calcSumm -= bonusSumm;
							calcSumm = calcSumm > 0 ? calcSumm : 0;
            				input_text.setText(SHOW_KOPS_IN_SUMM ? formatter.format(calcSumm): (int)calcSumm +"");
            			}

                        if(actOrderPrevSumm > 0 && disablePrevSumm) {
                            input_text.setEnabled(false);
                        }
            		}
            	}

				final double inputPrice = strToIntDef(input_text.getText()
						.toString().replace(',','.'), 0);
				//showMyMsg("inputPrice: " + inputPrice);
            	inp_builder.setView(input_text);
            	
            	AlertDialog dlg4 = inp_builder.setTitle("ВВОД ДАННЫХ")
                    .setMessage(msg)
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton(R.string.msg_dialog_close_str, 
                    		new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) 
                            {
								double sendPrice = strToIntDef(input_text.getText()
										.toString().replace(',','.'), 0);
								//showMyMsg("sendPrice: " + sendPrice);
                            	if (dontMinimizeCalcPrice && inputPrice > sendPrice) {
                            		showMyMsg("Запрещено менть стоимость в меньшуу сторону!");
                            		return;
								}

                            	try	{
                            	if (((TDSocketClient.clientStatus==
                                        Driver.ON_ORDER_COMPLETING)||
                                        (TDSocketClient.clientStatus==
                                        Driver.IN_WORKING))&&
                                        (completingOrderId.equals(TDSocketClient.
                                        		activeOrderID)))  {
									String inpSale = input_text.getText().toString().replace(',','.');
                            		if ( SHOW_KOPS_IN_SUMM ? GpsLocationDetector.checkStringDouble
											(inpSale) : GpsLocationDetector.checkStringInt
											(inpSale) )   {
                                        String v_obj = "{\"command\":\"order_complete\","+
                                            "\"order_id\":\""+
                                            TDSocketClient.activeOrderID
                                            +"\",\"client_id\":\""+
                                            TDSocketClient.clientId
                                            +"\",\"sale\":\""+
                                            input_text.getText().toString().replace('.',',')+
                                            ((//TDSocketClient.timerIsActive
                                              //		&&
                                            (timerOrderId.
                                            		equals(TDSocketClient.activeOrderID))
                                            		)?
                                            "\",\"time\":\""+orderTime:"")+
                                            ((hasGps&&USE_GPS_TAXOMETER)?
                                            ((//tmeter.serviceActive
                                              //		&&
                                            (tmeter.tmeterOrderId.
                                            		equals(TDSocketClient.activeOrderID))
                                            		)?
                                            "\",\"tdist\":\""+orderDist:"")
                                            :"")
                                            +"\",\"msg_end\":\"ok\"}"; 
                                        
                                        TDSocketClient.wrapper.
                                                sendToServer(v_obj);
                                        
                                        //TDSocketClient.operateFreezeState = true;
                                        showMyMsg(
                                            "Подождите, выполняется операция"+
                                            " отчета по текущей заявке...");
                                    }
                                    else    {
                                    	showMyMsg
                                                ("Ошибка действия: неверно введен отчет!!! "+inpSale+":"+input_text.getText().toString());
                                    }
                                }
                            	else
                    				showMyMsg("Отчет уже выполнил диспетчер!"); 
                            	}	catch(Exception e)	{
                            		showMyMsg("Ошибка операции отчета "+e);
                            	}
                            }
                        }).create();
            	dlg4.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            	if (!sound_lock)
            		dlg4.show();
            	changeSector(false);
        		//}
        		//else
        		//	showMyMsg("Неудачный поиск текущей заявки в массиве!");
                break;
            default:
        }
    	
    }	catch(Exception e)	{
		showMyMsg("Ошибка вывода диалога: "+e);
    }
    
}
	//////////////////////////////////////////
	///////end///функции отчета по заявке
	//////////////////////////////////////////

	public void onProviderDisabled(String provider) {}

	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	/////===========================================
	///// Работа с сокетами
	/////===========================================

	boolean connectAttempt = false;
	private Socket mSocket;
	static boolean inactiveTimeoutBlock = false;
	static int inactiveTimeout = 0;
	CheckTimer checkTimer = null;
	boolean auth = false;
	public String lastStatusData = "";
	public String prevLastStatusData = "";
	public boolean settingsLoaded = false;
	public boolean loadDataFromSocketIO = false;

	////////////////////////////////////
	////////////////////////////////////
	///// Работа с сокетами
	////////////////////////////////////
	////////////////////////////////////

	public void setStatusString(String message) {
		showMyMsg(message);
	}

	long nonStatusIterationCount = 0;
	boolean statusChecked = false;
	long statusRequestCounter = 0;

	public void sendWSStatusRequest(boolean checkStatus) {
		try {
			if (mSocket != null) {
				JSONObject resultJson = new JSONObject();
				resultJson.put("login", login_name);
				resultJson.put("psw", psw_str);
				mSocket.emit("rqst", resultJson.toString());
				if (checkStatus) {
					statusChecked = false;
				}
			}
		} catch (Exception exx) {
			showMyMsg("Error of send status request!");
		}
	}

	public void sendActiveStatusRequest() {
		try {
			if (mSocket != null) {
				JSONObject resultJson = new JSONObject();
				resultJson.put("login", login_name);
				resultJson.put("psw", psw_str);
				mSocket.emit("active_orders", resultJson.toString());
			}
		} catch (Exception exx) {
			showMyMsg("Error of send active_orders status request!");
		}
	}

	public void sendEarlyStatusRequest() {
		try {
			if (mSocket != null) {
				JSONObject resultJson = new JSONObject();
				resultJson.put("login", login_name);
				resultJson.put("psw", psw_str);
				mSocket.emit("early_orders", resultJson.toString());
			}
		} catch (Exception exx) {
			showMyMsg("Error of send active_orders status request!");
		}
	}

	public void connectCheck() {
		if (!settingsLoaded) {
			return;
		}

		if (statusRequestCounter > 2000000) {
			statusRequestCounter = 0;
		}
		statusRequestCounter++;
		if (statusRequestCounter % 40 == 0 && mSocket != null) {
			if (!statusChecked && nonStatusIterationCount > 2) {
				showMyMsg("Reload websocket, no answer!");
				lastStatusData = "";
				try {
					mSocket.disconnect();
					mSocket = null;
					auth = false;
					showMyMsg("TDC dsconn");
				} catch (Exception exx) {
					showMyMsg("Error of status request disconnect!");
				}
				mSocket = null;
				setStatusString("Ожидание соединения...");
			}

            nonStatusIterationCount++;
			if (statusChecked) {
				nonStatusIterationCount = 0;
			}

			sendWSStatusRequest(true);

		}

		if (!connectAttempt && !inactiveTimeoutBlock) {
			try {
				connectAttempt = true;
				try {
					if (mSocket == null) {// ? true : !mSocket.connected()) {
						//setStatusString("Подключение...");
						lastStatusData = "";
						auth = false;
						mSocket = IO.socket("http://" + server + ":" + WSS1_PORT);
						if (mSocket != null) {
							inactiveTimeoutBlock = false;
							mSocket.on("onconnect", new Emitter.Listener() {

										@Override
										public void call(Object... args) {
											lastStatusData = "";
											//setStatusString("Найдено соедениние... идентификация...");
											try {
												JSONObject resultJson = new JSONObject();
												resultJson.put("login", login_name);
												resultJson.put("psw", psw_str);
												mSocket.emit("ident", resultJson.toString());
											} catch (Exception ex) {
												showMyMsg(ex.getMessage());
											}
										}
									}
							).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

								@Override
								public void call(Object... args) {
									//  socket.disconnect();
									auth = false;

								}

							});
							mSocket.on("auth", onAuth);
							//mSocket.on("clstat", onClStat);
							mSocket.on("sectors", onSectorsList);
							mSocket.on("tarifs_and_options", onTOList);
							mSocket.on("req_decline", onReqDecline);
							mSocket.on("forders_wbroadcast", onFordersWbroadcast);
							mSocket.on("rsst", onStatusResponse);
							mSocket.on("early_orders", onEarlyResponse);
							mSocket.connect();
						}
					}
					if (mSocket != null) {// ? mSocket.connected() : false) {
						if (!auth) {
							lastStatusData = "";
							//setStatusString("Найдено соедениние... идентификация...");
							JSONObject resultJson = new JSONObject();
							resultJson.put("login", login_name);
							resultJson.put("psw", psw_str);
							mSocket.emit("ident", resultJson.toString());
						} else {
							if (lastStatusData.length() > 0 &&
									!prevLastStatusData.equals(lastStatusData)) {
								showMyMsg(lastStatusData);
							}
						}
					} else {
						lastStatusData = "";
						auth = false;
						if (mSocket != null) {
							try {
								mSocket.disconnect();
								mSocket = null;
								showMyMsg("TDC dsconn");
							} catch (Exception exx) {

							}
						}
						mSocket = null;
						setStatusString("Ожидание соединения...");
					}
					//mSocket.emit("ident", "me");

				} catch (Exception e) {
					auth = false;
					if (mSocket != null) {
						try {
							mSocket.disconnect();
							mSocket = null;
							//showToast("TDC dsconn");
						} catch (Exception ex) {

						}
					}
					mSocket = null;
					setStatusString("Соединение неудачно! Пробуем снова..." + e.getMessage());
				}
			} finally {
				connectAttempt = false;
			}
		} else {
			if (inactiveTimeoutBlock)
				setStatusString("Отключен от сервера, нажмите на любую кнопку для поключения!" + "...");
		}
	}

	private Emitter.Listener onStatusResponse = new Emitter.Listener() {
		public void handleDrStatus(String data) {
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_DR_STATUS;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			statusChecked = true;
			JSONObject data = (JSONObject) args[0];
			handleDrStatus(data.toString());
		}
	};

	private Emitter.Listener onEarlyResponse = new Emitter.Listener() {
		public void handleEarly(String data) {
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_EARLY;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			JSONObject data = (JSONObject) args[0];
			handleEarly(data.toString());
		}
	};

	private Emitter.Listener onFordersWbroadcast = new Emitter.Listener() {
		public void handleJSONStr(String data) {
			//this.showMyMsg("sock show timer");
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_FORDERS_BROADCAST;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			try {
				JSONObject data = (JSONObject) args[0];
				handleJSONStr(data.toString());
			} catch (Exception e) {
				showMyMsg("Error of RECEIVE_FORDERS_BROADCAST:  " + ":" + e.getMessage());
                //Toast.makeText(getApplicationContext(),
                //        "Error of RECEIVE_FORDERS_BROADCAST: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	};

	private Emitter.Listener onAuth = new Emitter.Listener() {
		public void handleAuth(int userId) {
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_AUTH;
			Bundle bnd = new Bundle();
			bnd.putInt("userId", userId);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			JSONObject data = (JSONObject) args[0];

			int userId = -1;
			try {
				userId = data.getInt("userId");
				auth = true;
			} catch (Exception e) {
				//showToast(e.getMessage());//
			}

			handleAuth(userId);
		}
	};

	private Emitter.Listener onSectorsList = new Emitter.Listener() {
		public void handleJSONStr(String data) {
			//this.showMyMsg("sock show timer");
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_SECTORS_LIST;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			JSONObject data = (JSONObject) args[0];
			handleJSONStr(data.toString());
		}
	};

	private Emitter.Listener onTOList = new Emitter.Listener() {
		public void handleJSONStr(String data) {
			//this.showMyMsg("sock show timer");
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.RECEIVE_TO_LIST;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			JSONObject data = (JSONObject) args[0];
			handleJSONStr(data.toString());
		}
	};

	private Emitter.Listener onReqDecline = new Emitter.Listener() {
		public void handleJSONStr(String data) {
			//this.showMyMsg("sock show timer");
			Message msg = new Message();
			//msg.obj = this.mainActiv;
			msg.arg1 = GpsLocationDetector.REQUEST_DECLINE;
			Bundle bnd = new Bundle();
			bnd.putString("data", data);
			msg.setData(bnd);
			handle.sendMessage(msg);
		}

		@Override
		public void call(Object... args) {
			JSONObject data = (JSONObject) args[0];
			try {
				//if (data.getString("status").equalsIgnoreCase("many_new_order_req"))
				//	handleJSONStr(data.toString());
			} catch (Exception e) {

			}
		}
	};

}
