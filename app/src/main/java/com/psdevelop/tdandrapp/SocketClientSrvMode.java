package com.psdevelop.tdandrapp;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Poltarokov SP
 */
public class SocketClientSrvMode implements Runnable	{ //extends Thread  {
        GpsLocationDetector mainActiv;
        private String server;
        private String port;
        private String alternativeServer="";
        private String alternativePort="";
        //private String thirdReserveServer="";
        //private String thirdRSrvPort="";
        String db;
        String login;
        String psw;
        String clientId = "";
        boolean operateFreezeState = false;
        public int badAttempts=0;
        public boolean fastConnection = false;
        int counter = 1;
        Thread t;
        boolean SocketHasError = false;
        Vector<VectorIstructItem> inputOrders;
        Vector<VectorIstructItem> freeOrders;
        Vector<VectorSectorItem> workSectors;
        Vector<VectorIstructItem> activeOrders;
        Vector<VectorIstructItem> earlyOrders;
        Vector<OrderTarif> ordersTarifs;
        Vector<OrderOption> ordersOptions;
        Vector<TarifPlan> tarifPlans;
        ArrayList<Integer> waitList;
        boolean autorized = false;
        public int clientStatus = -1;
        boolean showedNextOrder = false;
        boolean acceptNextOrder = false;
        boolean acceptSyncNextOrder = false;
        boolean occupateNextOrder = false;
        InputStream is = null;
        OutputStream os = null;
        BufferedInputStream bis;
        //DataInputStream dis = null;
        //DataOutputStream dos = null;
        Socket socket = null;
        String activeOrderID = "";
        String activeOrderData = "";
        double activeOrderPrevSumm = 0;
        String nextOrderID = "";
        String nextOrderData = "";
        String potentialOrderID = "";
        String potentialOrderData = "";
        String acceptOrderID = "";
        String acceptOrderData = "";
        int lastConnectClientStatus = -1;
        String lastConnectOrderID = "";
        String lastConnectOrderData = "";
        String activeSectorID = "";
        String activeSectorName = "";
        String autoDetectSendedSID = "";
        String lastConnectSectorID = "";
        String lastConnectSectorName = "";
        boolean lastAcceptNextOrder = false;
        boolean lastOccupateNextOrder = false;
        String lastConnectNextOrderID = "";
        String lastConnectNextOrderData = "";
        boolean playTones = true;
        public SocketWrapperSrvMode wrapper;
        public SocketDataProcessorSrvMode dataProcessor;
        boolean first_sect_request = true;
        boolean send_ask = false;
        boolean has_IO_activity = true;
        public boolean CHECK_CONNECTION = false;
        public int CHECK_CONN_TIME = 600;
        public int RECONNECT_NUMBERS = 10;
        boolean interrupt = false;
        int testAttemptsCount = 0;
        String position="?";
        int clientVersion=3036;
        int srvClientVersion=-1;
        int minSrvClientVersion=-1;
        boolean mandatorySrvUpdate=false;
        boolean useAlternativeServer=false;
        boolean useThirdServer=false;
        int tarifPlanId=-1;
        int groupTPlanId=-1;
        boolean sockActReload=false;
        boolean enableOStat=false;
        int companyId = -1;
        boolean dontShowWaitFromOtherCompanies = false;
        boolean dontShowWaitWithEmptyCompanies = false;
        boolean calcTaxometerSummWithBonus = false;
        boolean sendCCoordsByMDelta = false;
        //boolean restTMNextOrd=false;

        public SocketClientSrvMode(GpsLocationDetector mainAct, String server, 
                String port, String alt_server, String alt_port, 
                String login_name, String psw_str) {
        	//super.
        	
            this.mainActiv = mainAct;
            this.server = server;
            this.port = port;
            this.alternativeServer = alt_server;
            this.alternativePort = alt_port;
            this.testAttemptsCount = 0;
            this.badAttempts = 0;
            this.login = login_name;
            this.psw = psw_str;
            this.fastConnection = false;
            this.inputOrders = new Vector<VectorIstructItem>();
            this.freeOrders = new Vector<VectorIstructItem>();
            this.workSectors = new Vector<VectorSectorItem>();
            this.activeOrders = new Vector<VectorIstructItem>();
			this.earlyOrders = new Vector<VectorIstructItem>();
            this.ordersTarifs = new Vector<OrderTarif>();
            this.ordersOptions = new Vector<OrderOption>();
            this.tarifPlans = new Vector<TarifPlan>();
            this.waitList = new ArrayList<Integer>();
            this.playTones = true;
            this.clientStatus = Driver.NOT_CONNECTED;
            this.lastConnectOrderID="";
            this.lastConnectOrderData="";
            this.lastConnectClientStatus=Driver.FREE_DRIVER;
            this.operateFreezeState = false;
            this.first_sect_request = true;
            this.send_ask = false;
            this.dataProcessor = new SocketDataProcessorSrvMode(this);
            this.wrapper = new SocketWrapperSrvMode(this);
            this.has_IO_activity = true;
            this.mainActiv.clientVersion = "v"+this.clientVersion;
        }
        
        public void startSocket() {
        	//this.showMyMsg("Try to BEFORE stArt service socket!");
            t = new Thread(this);
            t.start();
        	//setMainFormTitle("aaa");
        	//this.start();
        }
        
        public boolean checkString(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
        }

        public boolean checkStringLong(String str) {
            try {
                Long.parseLong(str);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        
        public boolean checkStringDouble(String str) {
            try {
                Double.parseDouble(str);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public void playMTones(int counter)  {
            this.playMP3(R.raw.guitar);
            //
        }
        
        public void alarmUncheckConfirm()	{
        	if (this.mainActiv.ALARM_ORDER_CONFIRM)	{
        		this.playMTones(4);
        		showMyMsg("Есть неподтвержденные заявки!");
        	}
        }
        
        public void startPeriodicConfirmAlarm(int signal_count)	{
        	if (this.mainActiv.ALARM_ORDER_CONFIRM)	{
        		this.wrapper.order_confirm_alarm_counter=
        				50*signal_count - 1;
        	}
        }
        
        public void stopPeriodicConfirmAlarm()	{
        	this.wrapper.order_confirm_alarm_counter=0;
        }
        
        public void setOrderManualAccept(String orderId, boolean accept)    {
            for (int o_counter=0;o_counter<
                    this.inputOrders.size();o_counter++)    {
                VectorIstructItem o_checkItem =
                    (VectorIstructItem)this.inputOrders.
                        elementAt(o_counter);
                if(o_checkItem.orderId.equals(
                        orderId))    {
                     o_checkItem.manual_accepted=true;
                     break;
                }
            }
        }
        
        public boolean getOrderManualAccept(String orderId)    {
            boolean accept = false;
                        for (int o_counter=0;o_counter<
                                this.inputOrders.size();o_counter++)    {
                            VectorIstructItem o_checkItem =
                                (VectorIstructItem)this.inputOrders.
                                    elementAt(o_counter);
                            if(o_checkItem.orderId.equals(
                                    orderId))    {
                                 accept = o_checkItem.manual_accepted;
                                 break;
                            }
                        }
            return accept;
        }
        
        public boolean getOrderAutoAccept(String orderId)    {
            boolean accept = false;
                        for (int o_counter=0;o_counter<
                                this.inputOrders.size();o_counter++)    {
                            VectorIstructItem o_checkItem =
                                (VectorIstructItem)this.inputOrders.
                                    elementAt(o_counter);
                            if(o_checkItem.orderId.equals(
                                    orderId))    {
                                 accept = o_checkItem.auto_accepted;
                                 break;
                            }
                        }
            return accept;
        }
        
        public void parseSectorList(JSONObject input_json)   {
            try {
                String scount_str = input_json.getString
                   ("s_cnt");
                if (checkString(scount_str))  {
                    int sector_count = 
                            Integer.parseInt(scount_str);
                    
                    this.workSectors.removeAllElements();
                    for(int i=0;i<sector_count;i++)    {
                        VectorSectorItem v_sect = 
                            new VectorSectorItem(
                                input_json.getString
                                ("id"+i),
                                input_json.getString
                                ("nm"+i));

                        if (input_json.has("cm"+i)) {
                            v_sect.companyId = this.strToIntDef(
                                    input_json.getString
                                    ("cm" + i), -1);
                        }

                        if (input_json.has("fal"+i)) {
                            v_sect.forAll = this.strToIntDef(
                                    input_json.getString
                                            ("fal" + i), -1) == 1;
                        }

                        int arcnt=0;
                        if (input_json.has("ccn"+i)) {
                            arcnt = this.strToIntDef(input_json.getString
                                    ("ccn" + i), -1);
                        }
                        for(int f=0;f<arcnt;f++)	{
                            v_sect.areaLines.add(new TarifAreaLine(this.strToDoubleDef(input_json.getString
                                    ("la"+i+"_"+f),0),this.strToDoubleDef(input_json.getString
                                    ("lo"+i+"_"+f),0)));
                        }
                        this.workSectors.addElement(v_sect);
                    }
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга списка секторов! "
                      +ex.getMessage());
                }
        }
        
        public void parseTarifOptionList(JSONObject input_json)   {
            try {
                String scount_str = input_json.getString
                   ("t_cnt");
                if (checkString(scount_str))  {
                    int sector_count = 
                            Integer.parseInt(scount_str);
                    
                    this.ordersTarifs.removeAllElements();
                    for(int i=0;i<sector_count;i++)    {
                        OrderTarif tarif = 
                            new OrderTarif(
                                this.strToIntDef(input_json.getString
                                ("tid"+i),-1),
                                input_json.getString
                                ("tn"+i),
                                this.strToDoubleDef(input_json.getString
                                ("txt"+i), 0),
                                this.strToDoubleDef(input_json.getString
                                ("tmt"+i), 0));
                        if(input_json.has("ttpi"+i))
                        	tarif.tplan_id=this.strToIntDef(input_json.getString
                                    ("ttpi"+i),-1);
                        if(input_json.has("tshn"+i))
                        	tarif.short_name=input_json.getString
                                    ("tshn"+i);
                        if(input_json.has("tdip"+i))
                        	tarif.part_dist=this.strToIntDef(input_json.getString
                                    ("tdip"+i),0);
                        if(input_json.has("tstds"+i))
                        	tarif.start_dist=this.strToIntDef(input_json.getString
                                    ("tstds"+i),0);
                        if(input_json.has("tdpt"+i))
                        	tarif.part_dist_tariff=this.strToDoubleDef(input_json.getString
                                    ("tdpt"+i),0);
                        if(input_json.has("tspt"+i))
                        	tarif.stop_tariff=this.strToDoubleDef(input_json.getString
                                    ("tspt"+i),0);
                        if(input_json.has("otarid"+i))
                        	tarif.outherAreaTarifId=this.strToIntDef(input_json.getString
                                    ("otarid"+i),-1);
                        if(input_json.has("otplid"+i))
                        	tarif.outherAreaTPlanId=this.strToIntDef(input_json.getString
                                    ("otplid"+i),-1);
                        int trarcnt=0;
                        if(input_json.has("trarcnt"+i))
                        	trarcnt=this.strToIntDef(input_json.getString
                                    ("trarcnt"+i),-1);
                        for(int f=0;f<trarcnt;f++)	{
                        	tarif.areaLines.add(new TarifAreaLine(this.strToDoubleDef(input_json.getString
                                    ("tralat"+i+"_"+f),0),this.strToDoubleDef(input_json.getString
                                    ("tralon"+i+"_"+f),0)));
                        }

                        if(input_json.has("mek"+i)) {
                            tarif.missEveryKmValue = this.strToIntDef(input_json.getString
                                    ("mek" + i), 0);
                        }

                        //this.showMyMsg("Tar: "+tarif.time_tariff+","+tarif.tmeter_tariff);
                        this.ordersTarifs.addElement(tarif);
                    }
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга списка тарифов! "
                      +ex.getMessage());
            }
            
            try {
                String scount_str = input_json.getString
                   ("op_cnt");
                if (checkString(scount_str))  {
                    int sector_count = 
                            Integer.parseInt(scount_str);
                    
                    this.ordersOptions.removeAllElements();
                    for(int i=0;i<sector_count;i++)    {
                        OrderOption option = 
                            new OrderOption(
                                this.strToIntDef(input_json.getString
                                ("oid"+i),0),
                                input_json.getString
                                ("on"+i),
                                this.strToDoubleDef(input_json.getString
                                ("oscf"+i), 0),
                                this.strToDoubleDef(input_json.getString
                                ("oscm"+i), 0));
                        if(input_json.has("otpi"+i))
                        	option.tplan_id=this.strToIntDef(input_json.getString
                                    ("otpi"+i),-1);
                        if(input_json.has("oshn"+i))
                        	option.short_name=input_json.getString
                                    ("oshn"+i);
                        //this.showMyMsg("Opt: "+option.opt_coeff+","+option.opt_composed);
                        this.ordersOptions.addElement(option);
                    }
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга списка опций! "
                      +ex.getMessage());
            }
            
            try {
                String scount_str = input_json.getString
                   ("tpl_cnt");
                if (checkString(scount_str))  {
                    int sector_count = 
                            Integer.parseInt(scount_str);
                    
                    this.tarifPlans.removeAllElements();
                    for(int i=0;i<sector_count;i++)    {
                        TarifPlan tplan = 
                            new TarifPlan(
                                this.strToIntDef(input_json.getString
                                ("tpid"+i),0),
                                input_json.getString
                                ("tpn"+i));
                        //this.showMyMsg("Opt: "+option.opt_coeff+","+option.opt_composed);
                        if(input_json.has("tpshn"+i))
                        	tplan.short_name=input_json.getString
                                    ("tpshn"+i);
                        this.tarifPlans.addElement(tplan);
                    }
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга списка тарифных планов! "
                      +ex.getMessage());
            }
            
        }
        
        public void parseFreeOrders(JSONObject input_json)   {
            try {
                String ocount_str = input_json.getString
                   ("ocnt");
                if (checkString(ocount_str))  {
                    int order_count = 
                            Integer.parseInt(ocount_str);
                    
                    this.freeOrders.removeAllElements();
                    for(int i=0;i<order_count;i++)    {
                    	VectorIstructItem v_ord = 
                            new VectorIstructItem(
                                input_json.getString
                                ("id"+i),
                                input_json.getString
                                ("oad"+i));
                    	if (input_json.has("oes"+i))
                    		v_ord.sector_id = this.strToIntDef(input_json.getString
                                ("oes"+i), -1);

                        if (input_json.has("fas"+i)) {
                            v_ord.isForAll = this.strToIntDef(input_json.getString
                                    ("fas"+i), -1) == 1;
                        }

                        if (input_json.has("cmp"+i)) {
                            v_ord.companyId = this.strToIntDef(input_json.getString
                                    ("cmp"+i), -1);
                        }

                        if (input_json.has("orb"+i))	{
                            v_ord.ratingBonus = this.strToDoubleDef(
                                    input_json.getString("orb"+i), -1);
                            if (v_ord.ratingBonus > 0) { // && !this.mainActiv.LOCK_FREE_ORDERS_INFO
                                v_ord.orderData += "(+" + v_ord.ratingBonus + ")";
                            }
                        }

                        if (input_json.has("oppr"+i))	{
                            v_ord.prevSumm = this.strToDoubleDef(
                                    input_json.getString("oppr"+i), -1);
                            if (v_ord.prevSumm > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
                                v_ord.orderData += "(" + v_ord.prevSumm + "р.)";
                            }
                        }

                        if (input_json.has("opdn"+i))	{
                            v_ord.prevDistance = this.strToDoubleDef(
                                    input_json.getString("opdn"+i), -1);
                            if (v_ord.prevDistance > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
                                v_ord.orderData += "(" + v_ord.prevDistance + "км.)";
                            }
                        }

                        if (input_json.has("ocrd"+i))	{
                            v_ord.cargoDesc=input_json.getString("ocrd"+i);
                            if (v_ord.cargoDesc.length() > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
                                v_ord.orderData += "(" + v_ord.cargoDesc + ")";
                            }
                        }

                        if (input_json.has("oena" + i))	{
                            v_ord.endAdres=input_json.getString("oena" + i);
                            if (v_ord.endAdres.length() > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
                                v_ord.orderData += "(->" + v_ord.endAdres + ")";
                            }
                        }

                        if (input_json.has("oprd" + i) && v_ord.cargoDesc.length() > 0)	{
                            long prevDT = this.strToLongDef(
                                    input_json.getString("oprd"+i), -1);
                            if (prevDT > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
                                Date date = new Date((prevDT - 10800)*1000L); // *1000 is to convert seconds to milliseconds
                                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); // the format of your date
                                //sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                //give a timezone reference for formating (see comment at the bottom
                                v_ord.orderData += "(" + sdf.format(date) + ")";
                            }
                        }

                        if (input_json.has("ocln"+i))	{
                            v_ord.clientName=input_json.getString("ocln"+i);
                        }

                        if(!this.mainActiv.HIDE_OTH_SECT_WAIT_ORDS || (input_json.has("oes"+i) &&
                                input_json.getString("oes"+i).equals(this.activeSectorID)) ||
                                v_ord.isForAll)
                            this.freeOrders.addElement(v_ord);
                    }
                    
                    if (order_count>0 && this.freeOrders.size()>0)	{
                    	//this.showServerMsg("ОБНОВЛЕН СПИСОК ОЖИДАЮЩИХ ЗАКАЗОВ!!!");
                    	this.playMP3(this.mainActiv.freeOrdTonePref);//R.raw.bonus);
                    	if (this.mainActiv.WAIT_DLG_AUTO)
                    		showWaitOrderDlg();
                    }
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга свободных заказов! "
                      +ex.getMessage());
                }
        }

		public void parseEarlyOrders(JSONObject input_json) {
			try {
				String ocount_str = input_json.getString
						("cn");
				//this.showMyMsg("Парсинг запланированных заказов! " + ocount_str);
				if (checkString(ocount_str))  {
					int order_count =
							Integer.parseInt(ocount_str);

					int prevEarlyCount = this.earlyOrders.size();
					this.earlyOrders.removeAllElements();
					for(int i = 0; i < order_count; i++)    {
						VectorIstructItem v_ord =
								new VectorIstructItem(
										input_json.getString
												("oid"+i),
										input_json.getString
												("odt"+i));
						if (input_json.has("oes"+i))
							v_ord.sector_id = this.strToIntDef(input_json.getString
									("oes"+i), -1);

						if (input_json.has("fas"+i)) {
							v_ord.isForAll = this.strToIntDef(input_json.getString
									("fas"+i), -1) == 1;
						}

						if (input_json.has("cmp"+i)) {
							v_ord.companyId = this.strToIntDef(input_json.getString
									("cmp"+i), -1);
						}

						if (input_json.has("orb"+i))	{
							v_ord.ratingBonus = this.strToDoubleDef(
									input_json.getString("orb"+i), -1);
							if (v_ord.ratingBonus > 0) { // && !this.mainActiv.LOCK_FREE_ORDERS_INFO
								v_ord.orderData += "(+" + v_ord.ratingBonus + ")";
							}
						}

						if (input_json.has("oppr"+i))	{
							v_ord.prevSumm = this.strToDoubleDef(
									input_json.getString("oppr"+i), -1);
							if (v_ord.prevSumm > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
								v_ord.orderData += "(" + v_ord.prevSumm + "р.)";
							}
						}

						if (input_json.has("opdn"+i))	{
							v_ord.prevDistance = this.strToDoubleDef(
									input_json.getString("opdn"+i), -1);
							if (v_ord.prevDistance > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
								v_ord.orderData += "(" + v_ord.prevDistance + "км.)";
							}
						}

						if (input_json.has("ocrd"+i))	{
							v_ord.cargoDesc=input_json.getString("ocrd"+i);
							if (v_ord.cargoDesc.length() > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
								v_ord.orderData += "(" + v_ord.cargoDesc + ")";
							}
						}

						if (input_json.has("oena" + i))	{
							v_ord.endAdres=input_json.getString("oena" + i);
							if (v_ord.endAdres.length() > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
								v_ord.orderData += "(->" + v_ord.endAdres + ")";
							}
						}

						if (input_json.has("oprd" + i) && v_ord.cargoDesc.length() > 0)	{
							long prevDT = this.strToLongDef(
									input_json.getString("oprd"+i), -1);
							if (prevDT > 0 && !this.mainActiv.LOCK_FREE_ORDERS_INFO) {
								Date date = new Date((prevDT - 10800)*1000L); // *1000 is to convert seconds to milliseconds
								SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); // the format of your date
								//sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
								//give a timezone reference for formating (see comment at the bottom
								v_ord.orderData += "(" + sdf.format(date) + ")";
							}
						}

						if (input_json.has("ocln"+i))	{
							v_ord.clientName=input_json.getString("ocln"+i);
						}
						this.earlyOrders.addElement(v_ord);
					}

					if (order_count > 0 && order_count != prevEarlyCount &&
                            this.earlyOrders.size() > 0) {
						//this.showServerMsg("ОБНОВЛЕН СПИСОК ОЖИДАЮЩИХ ЗАКАЗОВ!!!");
						this.playMP3(this.mainActiv.freeOrdTonePref);//R.raw.bonus);
						if (this.mainActiv.WAIT_DLG_AUTO)
							showWaitOrderDlg();
					}

				}
			} catch (Exception ex) {
				this.showMyMsg("Ошибка парсинга запланированных заказов! " +
						ex.getMessage());
			}
		}

        
        public void parseOperationAnswer(JSONObject input_json)   {
        	try {
        	if (input_json.getString("opnm").equalsIgnoreCase("dr_bal"))	{
        		String balanceMessage = "Ваш баланс равен: "+
                        input_json.getString("dr_bal") + ". Заработал за посл. 12 часов: "+
                        input_json.getString("lst12hs") + "." +
                        (input_json.has("dr_rating")
                            ? " Рейтинг: " + input_json.getString("dr_rating") + "."
                            : "");

        	    this.showDialogElement(TDDialog.TB_SHOW_MSG, balanceMessage);
        	}
        	else if (input_json.getString("opnm").equalsIgnoreCase("drinc"))	{
        		
        		if(input_json.getString("drinc").equalsIgnoreCase("ok"))
        		this.showDialogElement(TDDialog.TB_SHOW_MSG,
        				"Сумма зачислена на счет! "+(input_json.has("omsg")?
        						input_json.getString("omsg"):""));
        		else
        			this.showDialogElement(TDDialog.TB_SHOW_MSG,
            				"Произошли неполадки при зачислении на счет, "
            				+ "проверьте кассу в программе диспетчера!");
        	}
            else if (input_json.getString("opnm").equalsIgnoreCase("wtl"))	{
                if(!input_json.getString("wtl").equalsIgnoreCase("ok")) {
                    return;
                }

                this.waitList = new ArrayList<Integer>();
                try {
                    String wcountStr = input_json.getString("wc");
                    if (checkString(wcountStr)) {
                        int wcount = Integer.parseInt(wcountStr);
                        for (int i = 0; i < wcount; i++) {
                            this.waitList.add(input_json.getInt("tv" + i));
                        }
                    }
                }   catch (Exception ex)    {
                    this.showMyMsg("Ошибка парсинга списка времени ожидания! "
                            + ex.getMessage());
                }
            }
        	else
        	{
        		
        	}
        	}   catch (Exception ex)    {
                this.addToLogMessage(
                  "\nОшибка парсинга результата операции! "
                  +ex.getMessage());
            }
        }
        
        public void parseSectorsStatuses(JSONObject input_json)   {
            try {
                String scount_str = input_json.getString
                   ("s_cnt");
                if (checkString(scount_str))  {
                    int sector_count = 
                            Integer.parseInt(scount_str);
                    
                    //this.workSectors.removeAllElements();
                    for(int i=0;i<sector_count;i++)  
                    for(int j=0;j<this.workSectors.size();j++)	{
                    	if(this.workSectors.elementAt(j).
                    			sectorId.equals(
                                input_json.getString
                                ("id"+i)))	{
                    		this.workSectors.elementAt(j).drCount =     
                    		this.strToIntDef(input_json.getString
                                        ("dc"+i), 0);
                    		break;
                    	}
                        //this.workSectors.addElement(v_sect);
                    }
                    
                    if (this.mainActiv.payOrderRefreshStarted)	{
                    	this.mainActiv.payOrderRefreshStarted = false;
                    	this.saleOrder();
                    }
                    else if (this.mainActiv.changeSectRefreshStarted)	{
                    	this.mainActiv.changeSectRefreshStarted = false;
                    	this.changeSector();
                    }
                    else if (this.mainActiv.sectorDirectionStarted)	{
                    	this.mainActiv.sectorDirectionStarted = false;
                    	this.setSectorDirection();
                    }
                    else	{
                    	
                    }
                    this.mainActiv.payOrderRefreshStarted = false;
                    this.mainActiv.changeSectRefreshStarted = false;
                    this.mainActiv.sectorDirectionStarted = false;
                    
                }
            }   catch (Exception ex)    {
                    this.addToLogMessage(
                      "\nОшибка парсинга статусов секторов! "
                      +ex.getMessage());
                }
        }
        
        public void viewDriverStatus()  {
            String statusName="НЕ ОПРЕДЕЛЕН";
            
            switch (this.clientStatus)  {
                case -2:
                    statusName="ОШИБКА СОЕДИНЕНИЯ";
                    break;
                case -1:
                    statusName="НЕ ПОДКЛЮЧЕН";
                    break;
                case 0:
                    statusName="НЕ АВТОРИЗОВАН";
                    break;
                case 1:
                    statusName="СВОБОДЕН";
                    break;
                case 2:
                    statusName="РЕШЕНИЕ О ПРИНЯТИИ";
                    break;
                case 3:
                    statusName="НА ЗАКАЗЕ";
                    break; 
                case 4:
                    statusName="НА ЗАКАЗЕ С РУКИ";
                    break;
                case 5:
                    statusName="ОТМЕНЯЕТСЯ ДИСПЕТЧЕРОМ";
                    break;
                case 6:
                    statusName="ОТМЕНЯЕТ САМ";
                    break;   
                case 7:
                    statusName="НА ПЕРЕРЫВЕ";
                    break;
                case 8:
                    statusName="СНИМАЕТСЯ С ЛИНИИ";
                    break;
                case 9:
                    statusName="ОТЧИТЫВАЕТСЯ";
                    break;
                case 10:
                    statusName="ОЖИДАНИЕ ОПЕРАЦИИ";
                    break;
                case 11:
                    statusName="ПОПЫТКА НА ПЕРЕРЫВ";
                    break;
                case 12:
                    statusName="ПОПЫТКА С ПЕРЕРЫВА";
                    break;
                case 13:
                    statusName="РЕШЕНИЕ ВСТАТЬ В ОЧЕРЕДЬ";
                    break;
                default: 
                    statusName="НЕ ОПРЕДЕЛЕН";
            }
            setMainFormTitle(statusName);
            
            setSectorLabel
                    (this.activeSectorName+", "+this.position+"-й");
            if ((this.clientStatus==Driver.IN_CANCELING)||
                    (this.clientStatus==Driver.IN_SELF_CANCELING)||
                    (this.clientStatus==Driver.ON_ORDER_COMPLETING)||
                    (this.clientStatus==Driver.IN_WORKING)) {
                
                setActiveOrderLabel
                    (this.activeOrderData);
                
            }   else
            {
                setActiveOrderLabel
                    ("......");
            }
            
        }
        
        public boolean directSend(String send_data, String dataDesc) {
        boolean result=false;
            
            try {    
                os.write(send_data.getBytes());
                os.flush();
                result=true;
            } catch(Exception ex)   {
                this.addToLogMessage(
                    " \nНеудачная передача \""+dataDesc+
                    "\"! "+ex.getMessage());
                this.fastConnection = false;
            }
            return result;
        }
        
        public void applySelectOrderTariff(int tarif_id, String settar_oid)	{
        	if(clientStatus==Driver.IN_WORKING)	{
        		if(settar_oid.equals(activeOrderID))	{
        		int aindx=getActiveOrderIndex();
        		if(aindx>=0)	{
        			
        		if(this.activeOrders.elementAt(aindx).
        				ordTariffId!=tarif_id)	{
        		String v_obj = "{\"cmd\":\"settar\",\"tarid\":\""
            		+tarif_id +"\",\"oid\":\""+settar_oid+
            		"\",\"msg_end\":\"ok\"}";
        		wrapper.sendToServer(v_obj);
        		
        		} else
        			this.showMyMsg("Тариф не изменен!");
        		} else
        			this.showMyMsg("Неудачное определение активного индекса!");
        		} else
        			this.showMyMsg("Ошибка заказ поменялся!");
        	} else
    			this.showMyMsg("Ошибка, надо иметь статус ЗАНЯТ!");
        }
        
        public void applySelectOrderOptions(String order_options, String setopt_oid)	{
        	if(clientStatus==Driver.IN_WORKING)	{
        		if(setopt_oid.equals(activeOrderID))	{
        		int aindx=getActiveOrderIndex();
            	if(aindx>=0)	{
        		if(!this.activeOrders.elementAt(aindx).
        				ordOptComb.equals(order_options))	{
        		String v_obj = "{\"cmd\":\"setopts\",\"oopts\":\""
            		+order_options +"\",\"oid\":\""+setopt_oid+
            		"\",\"msg_end\":\"ok\"}";
        		wrapper.sendToServer(v_obj);
        		} else
        			this.showMyMsg("В опциях не внесено изменений!");
        		} else
        			this.showMyMsg("Неудачное определение активного индекса!");
        		} else
        			this.showMyMsg("Ошибка заказ поменялся!");
        	} else
    			this.showMyMsg("Ошибка, надо иметь статус ЗАНЯТ!");
        }
        
        public void sendGPSCoords(String latitude, String longitude)   {
            String v_obj = "{\"cmd\":\"gpsc\",\"lt\":\""
            		+latitude +"\",\"ln\":\""+longitude
                +"\",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendStatusQuery()   {
            mainActiv.sendWSStatusRequest(false);
            String v_obj = "{\"cmd\":\"r\""
                    + ",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendFullStatusQuery()   {
            mainActiv.sendWSStatusRequest(false);
            String v_obj = "{\"cmd\":\"r\""
                    + ",\"full\":\"ok\",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendPayOkAnwer()   {
            String v_obj = "{\"cmd\":\"dpay_aok\""
                +",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendPayDeclineAnwer()   {
            String v_obj = "{\"cmd\":\"dpay_ano\""
                +",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendOPRequest()   {
            String v_obj = "{\"cmd\":\"op_requ\""
                +",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendSectorsStatusesQuery()   {
            String v_obj = "{\"cmd\":\"ss\""
                +",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendLineStatusQuery()   {
            String v_obj = "{\"cmd\":\"dls\""
                +",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendReportRequest(String rname)   {
            String v_obj = "{\"cmd\":\"rpr\",\"rnm\":\""
                +rname+"\",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendOrderHistory(String orderId, String orderHistory, double last_summ)	{
        	this.sendClientRequest("ohist", "-1", "-1", orderId, orderHistory, ""+last_summ);
        }
        
        public void sendCOSP(String sdata)	{
        	if(enableOStat)
        		if(this.activeOrderID!=null&&this.activeOrderID!="")
        			this.sendAccStatusPart(this.activeOrderID, sdata);
        }
        
        public void sendAccStatusPart(String orderId, String statusData)	{
        	this.sendClientRequest("acst", "-1", "-1", orderId, statusData, "---");
        }
        
        public void sendMgsAddToAccCommand(String dr_num, String addsumm)	{
        	this.sendClientRequest("drinc", "-1", "-1", dr_num, addsumm, "---");
        	this.showMyMsg("Данные о зачислении переданы на отправку, ждите оповещения о результате!");
        }
        
        public void sendLastCurrCoords(double lat, double lon)	{
        	DecimalFormat df = new DecimalFormat("#.######");
        	this.sendClientRequest("lcc", "-1", "-1", "-1", df.format(lat).replace(",", "."), df.format(lon).replace(",", "."));
        }
        
        public void sendClientRequest(String op_name, String param1, String param2, 
        		String param3, String param4, String param5)   {
            String v_obj = "{\"cmd\":\"opr\",\"opnm\":\""
                +op_name+"\",\"prm1\":\""+param1
                +"\",\"prm2\":\""+param2
                +"\",\"prm3\":\""+param3
                +"\",\"prm4\":\""+param4
                +"\",\"prm5\":\""+param5
                +"\",\"msg_end\":\"ok\"}";
            wrapper.sendToServer(v_obj);
        }
        
        public void sendDrBalanceRequestStart()	{
        	if(this.mainActiv.requestBalanceStart)
        		this.sendClientRequest("dr_bal", "-1", 
        			"-1", "-1", "---", "---");
        }
        
        public void sendDrBalanceRequest()	{
        	this.sendClientRequest("dr_bal", "-1", 
        			"-1", "-1", "---", "---");
        }

        public void sendWaitListRequest()	{
            this.sendClientRequest("wtl", "-1",
                    "-1", "-1", "---", "---");
        }

        public void sendDriverRefuseRequest()	{
            this.sendClientRequest("dr_refuse", "-1",
                    "-1", "-1", "---", "---");
        }
        
        public void sendOrderAcknow(String orderId, String orderData, boolean confirmWait)	{
        	//Предлагается заказ
            boolean replyTake = false;
            boolean has_acknow = false;
            boolean show_acknow = true;
            for (int o_counter=0;o_counter<
                    inputOrders.size();o_counter++)    {
                VectorIstructItem o_checkItem =
                    (VectorIstructItem)inputOrders.
                        elementAt(o_counter);
                if(o_checkItem.orderId.equals(
                		orderId))    {
                     replyTake = true;
                     if (o_checkItem.has_acknowlegment)
                    	 has_acknow = true;
                     o_checkItem.has_acknowlegment = true;
                     break;
                }
            }
            
            if (!replyTake) {
                VectorIstructItem ord_item =
                    new VectorIstructItem(1);
                ord_item.orderId = orderId;
                ord_item.orderData = orderData;
                ord_item.auto_accepted = true;
                ord_item.has_acknowlegment = true;
                
                inputOrders.addElement(ord_item);
            }
            
            if (!has_acknow)	{
            	if (show_acknow)	{
            		Message msg = new Message();
                	msg.obj = this.mainActiv;
                	msg.arg1 = ConnectionActivity.SYNC_ORDER;
                	Bundle bnd = new Bundle();
                	bnd.putString("order_id", orderId);
                	bnd.putString("msg_lbl_text", orderData);
                	bnd.putBoolean("confirmWait", confirmWait);
                	msg.setData(bnd);
                	this.mainActiv.handle.sendMessage(msg);
                	this.startPeriodicConfirmAlarm(5);
            	}
            	else	{
            		String v_obj = "{\"sync\":\"ok\",\"ot\":\"oac\",\"oid\":\""
            				+orderId+"\",\"msg_end\":\"ok\"}";
            		wrapper.sendToServer(v_obj);
            	}
            }
        }
        
        public void sendAcceptStatus()   {
            String v_obj = "{\"sync\":\"ok\""+
                "\",\"oper_type\":\"accept_status\""
                +",\"client_id\":\""+clientId
                +"\",\"msg_end\":\"ok\"}";
            directSend(v_obj, "Подтверждение синхр. статуса!");
        }
        
        public void onLaunchRequest()	{
        	if ((clientStatus==
                    Driver.FREE_DRIVER)||
                (clientStatus==
                    Driver.IN_DECISION)||
                (clientStatus==
                    Driver.IN_ACCEPT_DECITION)) {
                clientStatus =
                    Driver.ON_REST_ATTEMPT;    
                String v_obj =
                    "{\"command\":\"on_rest\","+
                    "\"msg_end\":\"ok\"}";
                wrapper.
                    sendToServer(v_obj);
            }   else
            {
                showMyMsg(
                        "На перерыв можно уйти только"+
                        " в свободном состоянии!");
            }
        }
        
        public void fromLaunchRequest()	{
        	if ((clientStatus==
                    Driver.ON_REST)) {
                clientStatus =
                    Driver.FROM_REST_ATTEMPT;
                String v_obj =
                    "{\"command\":\"from_rest\","+
                    "\"msg_end\":\"ok\"}";
                wrapper.
                        sendToServer(v_obj);
            }   else
            {
                showMyMsg(
                        "С перерыва можно сняться"+
                        " будучи на перерыве!");
            }
        }
        
        public void sendDrOnPlaceRequest()	{
        	if (clientStatus==
                    Driver.IN_WORKING) {
                String v_obj =
                    "{\"command\":\"iam_here\""+
                    ",\"order_id\":\""+
                	activeOrderID+
                    "\",\"msg_end\":\"ok\"}";
                wrapper.
                        sendToServer(v_obj);
            }
            else
            {
                showCustomMsg("Ошибка действия",
                    "Для сообщения НА ТОЧКЕ надо иметь статус ЗАНЯТ!");
            }
        }
        
        public void selfOrderRequest()	{
        	if (clientStatus==
                    Driver.FREE_DRIVER) {
                String v_obj =
                    "{\"command\":\"onhand_order\","+
                    "\"msg_end\":\"ok\"}";
                wrapper.
                        sendToServer(v_obj);
            }
            else
            {
                showCustomMsg("Ошибка действия",
                    "Для запроса С РУКИ надо иметь статус СВОБОДЕН!");
            }
        }
        
        public void sendSignoutRequest()	{
        	if (clientStatus==
                    Driver.FREE_DRIVER) {
                String v_obj =
                    "{\"command\":\"sign_out\","+
                    "\"msg_end\":\"ok\"}";
                wrapper.
                        sendToServer(v_obj);
                showMyMsg("Послан запрос на снятие с линии!");
            }
            else
            {
                showCustomMsg("Ошибка действия",
                    "Для запроса снятия с линии надо иметь статус СВОБОДЕН!");
            }
        }
        
        public void queueQuery()	{
        	String v_obj =
                    "{\"command\":\"queue_query\","+
                    "\"msg_end\":\"ok\"}";
        			wrapper.
                        sendToServer(v_obj);
        }
        
        public void declineCancelOrder(boolean is_decline, String decline_id)	{
        	if (clientStatus==
                    Driver.IN_WORKING)  {
        				String v_obj =
        						"{\"command\":\"decline_order\","+
        						"\"order_id\":\""+
        						(is_decline?decline_id:
        							activeOrderID)+"\","+
        						"\"msg_end\":\"ok\"}";
            			wrapper.
                            sendToServer(v_obj);
              		}
              		else
        				showMyMsg("Вы не на исполнении заказа!");
        }
        
        public void checkServerActiveOrders(JSONObject input_json)   {
            
        }

        public long strToLongDef(String str_int, long def) {
            long res = def;

            if (checkStringLong(str_int)) {
                res = Long.parseLong(str_int);
            }

            return res;
        }
        
        public int strToIntDef(String str_int, int def) {
            int res = def;
            
            if (checkString(str_int)) {
                res = Integer.parseInt(str_int);
            }
            
            return res;
        }
        
        public double strToDoubleDef(String str_double, double def) {
            double res = def;
            
            if (checkStringDouble(str_double)) {
                res = Double.parseDouble(str_double);
            }
            
            return res;
        }

    public boolean checkOrderCompany(int orderCompanyId) {
        if (orderCompanyId > 0 && this.dontShowWaitFromOtherCompanies &&
                this.companyId > 0 && this.companyId != orderCompanyId) {
            return false;
        }

        if (this.dontShowWaitWithEmptyCompanies &&
                this.companyId > 0 && orderCompanyId <= 0) {
            return false;
        }

        return true;
    }
        
        public void assignSettings(JSONObject input_json)	{
        	try	{

        		if(input_json.has("fttar"))
        			mainActiv.timeTariff=
        			strToDoubleDef(input_json.getString("fttar"),0);
        		
        		//if(input_json.has("rstnok"))
	        	//	if(input_json.getString("rstnok").equals("1"))
	        	//		this.restTMNextOrd=true;
        		
        		if(input_json.has("txtar"))
        			mainActiv.tmeterTariff=
        			strToDoubleDef(input_json.getString("txtar"),0);
        		
        		if(input_json.has("dstd"))
	        		if(input_json.getString("dstd").equals("1"))
	        			this.mainActiv.disableTarDlg=true;
        		
        		if(input_json.has("mafl"))
	        		if(input_json.getString("mafl").equals("1"))	{
	        			this.mainActiv.managerAccessFirstLevel=true;
	        			this.mainActiv.setMAFLEnable();
	        		}
        		
        		if(input_json.has("eost"))
	        		if(input_json.getString("eost").equals("1"))
	        			this.enableOStat=true;
        		
        		if(input_json.has("skar"))
	        		if(input_json.getString("skar").equals("1"))
	        			sockActReload=true;
        		
        		if(input_json.has("dtre"))
	        		if(input_json.getString("dtre").equals("1"))
	        			this.mainActiv.disableTMReportEdit=true;

                if (input_json.has("dpsm") &&
                        input_json.getString("dpsm").equals("yes")) {
                        this.mainActiv.disablePrevSumm = true;
                }

                if (input_json.has("psot") &&
                        input_json.getString("psot").equals("yes")) {
                    this.mainActiv.prevSummOverTaxometr = true;
                }

                if (input_json.has("dmcp") &&
                        input_json.getString("dmcp").equals("yes")) {
                    this.mainActiv.dontMinimizeCalcPrice = true;
                }

                if (input_json.has("tosp") &&
                        input_json.getString("tosp").equals("yes")) {
                    this.mainActiv.taxometerOverSmallPrevSumm = true;
                }
        		
        		if(input_json.has("nss"))
	        		if(input_json.getString("nss").equals("1"))
	        			this.mainActiv.dontShutdownService=true;

	        	if(input_json.has("en_moving"))
	        		if(input_json.getString("en_moving").equals("1"))
	        			this.mainActiv.en_moving=true;
	        			else
	        				this.mainActiv.en_moving=false;
	        	if(input_json.has("cpw"))
	        		if(input_json.getString("cpw").equals("1"))
	        			this.mainActiv.confirmPrevWaiting=true;
	        			else
	        				this.mainActiv.confirmPrevWaiting=false;
	        	if(input_json.has("csw"))
	        		if(input_json.getString("csw").equals("1"))
	        			this.mainActiv.confirmSyncWaiting=true;
	        			else
	        				this.mainActiv.confirmSyncWaiting=false;
	        	if(input_json.has("mand_upd"))
	        		if(input_json.getString("mand_upd").equals("1"))
	        			this.mandatorySrvUpdate=true;
	        			else
	        				this.mandatorySrvUpdate=false;
	        	if(input_json.has("curr_mver"))
	        		this.srvClientVersion = this.strToIntDef(
	        				input_json.getString("curr_mver"),-1);
	        	if(input_json.has("min_mver"))
	        		this.minSrvClientVersion = this.strToIntDef(
	        				input_json.getString("min_mver"),-1);
	        	
	        	if(input_json.has("tplid"))
	        		this.tarifPlanId = this.strToIntDef(
	        				input_json.getString("tplid"),-1);
	        	if(input_json.has("grtpi"))
	        		this.groupTPlanId = this.strToIntDef(
	        				input_json.getString("grtpi"),-1);

                if(input_json.has("cmpi")) {
                    this.companyId = this.strToIntDef(
                            input_json.getString("cmpi"), -1);
                }

                if(input_json.has("DWOC")) {
                    this.dontShowWaitFromOtherCompanies =
                            input_json.getString("DWOC").equals("yes");
                }

                if(input_json.has("DWWC")) {
                    this.dontShowWaitWithEmptyCompanies =
                            input_json.getString("DWWC").equals("yes");
                }

                if (input_json.has("CSWB")) {
                    this.calcTaxometerSummWithBonus =
                            input_json.getString("CSWB").equals("yes");
                }

                if (input_json.has("SCWMD")) {
                    this.sendCCoordsByMDelta =
                            input_json.getString("SCWMD").equals("yes");
                }
	        	
	        	if(input_json.has("RCNNS"))	{
	        		if(this.strToIntDef(
	        				input_json.getString("RCNNS"),100)>=0)
	        		this.RECONNECT_NUMBERS = this.strToIntDef(
	        				input_json.getString("RCNNS"),100);

	        		if(this.RECONNECT_NUMBERS!=strToIntDef(
	        				this.mainActiv.prefs.getString
	        				("RECONNECT_NUMBERS", "100"),100))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("RECONNECT_NUMBERS");
		        			edt.putString("RECONNECT_NUMBERS", 
		        					""+this.RECONNECT_NUMBERS);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек RECONNECT_NUMBERS с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
	        	}
	        	
	        	if(input_json.has("ip2"))	{
	        	if(!input_json.getString("ip2").equals(this.alternativeServer))	{
	        		try	{
	        			Editor edt = this.mainActiv.prefs.edit();
	        			edt.remove("IP2");
	        			edt.putString("IP2", input_json.getString("ip2"));
	        			edt.commit();
	        		} catch (Exception pex)	{
	        			this.showMyMsg("Неудачное присваивание настроек с сервера! "+
	        					pex.getMessage());
	        		}
	        	}
	        	}
	        	
	        	if(input_json.has("rsbt"))	{
		        	if(input_json.getString("rsbt").equals("1")&&
		        			!this.mainActiv.prefs.getBoolean("RESET_LOST_BTIME", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("RESET_LOST_BTIME");
		        			edt.putBoolean("RESET_LOST_BTIME", true);
		        			edt.commit();
		        			this.mainActiv.RESET_LOST_BTIME = true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек RESET_LOST_BTIME true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("rsbt").equals("0")&&
		        			this.mainActiv.prefs.getBoolean("RESET_LOST_BTIME", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("RESET_LOST_BTIME");
		        			edt.putBoolean("RESET_LOST_BTIME", false);
		        			edt.commit();
		        			this.mainActiv.RESET_LOST_BTIME = false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек RESET_LOST_BTIME false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

                if(input_json.has("stcwm"))	{
                    if(input_json.getString("stcwm").equals("yes")&&
                            !this.mainActiv.prefs.getBoolean("START_TIME_CALC_WITH_MENU", false))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            edt.remove("START_TIME_CALC_WITH_MENU");
                            edt.putBoolean("START_TIME_CALC_WITH_MENU", true);
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек START_TIME_CALC_WITH_MENU true с сервера! "+
                                    pex.getMessage());
                        }
                    }

                    if(input_json.getString("stcwm").equals("no")&&
                            this.mainActiv.prefs.getBoolean("START_TIME_CALC_WITH_MENU", true))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            edt.remove("START_TIME_CALC_WITH_MENU");
                            edt.putBoolean("START_TIME_CALC_WITH_MENU", false);
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек START_TIME_CALC_WITH_MENU false с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }
	        	
	        	if(input_json.has("use_gps"))	{
		        	if(input_json.getString("use_gps").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_GPS", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("USE_GPS");
		        			edt.putBoolean("USE_GPS", true);
		        			edt.commit();
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек UG true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("use_gps").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_GPS", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("USE_GPS");
		        			edt.putBoolean("USE_GPS", false);
		        			edt.commit();
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек UG false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

                if(input_json.has("ldfs"))	{
                    if(input_json.getString("ldfs").equals("yes")&&
                            !this.mainActiv.prefs.getBoolean("USE_DRIVERS_SOCKET_SERVER", false))	{
                        this.mainActiv.loadDataFromSocketIO = true;
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            edt.remove("USE_DRIVERS_SOCKET_SERVER");
                            edt.putBoolean("USE_DRIVERS_SOCKET_SERVER", true);
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек USE_DRIVERS_SOCKET_SERVER true с сервера! "+
                                    pex.getMessage());
                        }
                    }

                    if(input_json.getString("ldfs").equals("no")&&
                            this.mainActiv.prefs.getBoolean("USE_DRIVERS_SOCKET_SERVER", true))	{
                        this.mainActiv.loadDataFromSocketIO = false;
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            edt.remove("USE_DRIVERS_SOCKET_SERVER");
                            edt.putBoolean("USE_DRIVERS_SOCKET_SERVER", false);
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек USE_DRIVERS_SOCKET_SERVER false с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }
	        	
	        	if(input_json.has("rqbs"))	{
		        	if(input_json.getString("rqbs").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("REQ_BALANCE_START", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("REQ_BALANCE_START");
		        			edt.putBoolean("REQ_BALANCE_START", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек REQ_BALANCE_START true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("rqbs").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("REQ_BALANCE_START", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("REQ_BALANCE_START");
		        			edt.putBoolean("REQ_BALANCE_START", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек REQ_BALANCE_START false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("sopc"))	{
		        	if(input_json.getString("sopc").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("SEND_ONPLACE_CALL", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("SEND_ONPLACE_CALL");
		        			edt.putBoolean("SEND_ONPLACE_CALL", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SEND_ONPLACE_CALL true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("sopc").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("SEND_ONPLACE_CALL", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("SEND_ONPLACE_CALL");
		        			edt.putBoolean("SEND_ONPLACE_CALL", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SEND_ONPLACE_CALL false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("cloe"))	{
		        	if(input_json.getString("cloe").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("CONF_LINEOUT_EXIT", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("CONF_LINEOUT_EXIT");
		        			edt.putBoolean("CONF_LINEOUT_EXIT", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CONF_LINEOUT_EXIT true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("cloe").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("CONF_LINEOUT_EXIT", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("CONF_LINEOUT_EXIT");
		        			edt.putBoolean("CONF_LINEOUT_EXIT", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CONF_LINEOUT_EXIT false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("gps_srv_adr"))	{
		        	if(!input_json.getString("gps_srv_adr").equals(
		        			this.mainActiv.prefs.getString("GPS_SRV_ADR", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("GPS_SRV_ADR");
		        			edt.putString("GPS_SRV_ADR", input_json.getString("gps_srv_adr"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек GPS_SRV_ADR с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("gps_acc_id"))	{
		        	if(!input_json.getString("gps_acc_id").equals(
		        			this.mainActiv.prefs.getString("GPS_ACC_ID", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("GPS_ACC_ID");
		        			edt.putString("GPS_ACC_ID", input_json.getString("gps_acc_id"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек GPS_ACC_ID с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("gps_dev_id"))	{
		        	if(!input_json.getString("gps_dev_id").equals(
		        			this.mainActiv.prefs.getString("GPS_DEV_ID", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("GPS_DEV_ID");
		        			edt.putString("GPS_DEV_ID", input_json.getString("gps_dev_id"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек GPS_DEV_ID с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("track_interval"))	{
		        	if(!input_json.getString("track_interval").equals(
		        			this.mainActiv.prefs.getString("TRACK_INTERVAL", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TRACK_INTERVAL");
		        			edt.putString("TRACK_INTERVAL", input_json.getString("track_interval"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TRACK_INTERVAL с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("track_distance"))	{
		        	if(!input_json.getString("track_distance").equals(
		        			this.mainActiv.prefs.getString("TRACK_DISTANCE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TRACK_DISTANCE");
		        			edt.putString("TRACK_DISTANCE", input_json.getString("track_distance"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TRACK_DISTANCE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("tmminds"))	{
		        	if(!input_json.getString("tmminds").equals(
		        			this.mainActiv.prefs.getString("TMETER_MIN_DISTANCE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TMETER_MIN_DISTANCE");
		        			edt.putString("TMETER_MIN_DISTANCE", input_json.getString("tmminds"));
		        			edt.commit();
		        			this.mainActiv.TMETER_MIN_DISTANCE = this.strToIntDef(
		        					this.mainActiv.prefs.getString("TMETER_MIN_DISTANCE", "7"), 7);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TMETER_MIN_DISTANCE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("tmmaxds"))	{
		        	if(!input_json.getString("tmmaxds").equals(
		        			this.mainActiv.prefs.getString("TMETER_MAX_DISTANCE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TMETER_MAX_DISTANCE");
		        			edt.putString("TMETER_MAX_DISTANCE", input_json.getString("tmmaxds"));
		        			edt.commit();
		        			this.mainActiv.TMETER_MAX_DISTANCE = this.strToIntDef(
		        					this.mainActiv.prefs.getString("TMETER_MAX_DISTANCE", "800"), 800);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TMETER_MAX_DISTANCE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("tmminsp"))	{
		        	if(!input_json.getString("tmminsp").equals(
		        			this.mainActiv.prefs.getString("TMETER_MIN_SPEED", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TMETER_MIN_SPEED");
		        			edt.putString("TMETER_MIN_SPEED", input_json.getString("tmminsp"));
		        			edt.commit();
		        			this.mainActiv.TMETER_MIN_SPEED = this.strToIntDef(
		        					this.mainActiv.prefs.getString("TMETER_MIN_SPEED", "12"), 12);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TMETER_MIN_SPEED с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("tmmaxsp"))	{
		        	if(!input_json.getString("tmmaxsp").equals(
		        			this.mainActiv.prefs.getString("TMETER_MAX_SPEED", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("TMETER_MAX_SPEED");
		        			edt.putString("TMETER_MAX_SPEED", input_json.getString("tmmaxsp"));
		        			edt.commit();
		        			this.mainActiv.TMETER_MAX_SPEED = this.strToIntDef(
		        					this.mainActiv.prefs.getString("TMETER_MAX_SPEED", "200"), 200);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TMETER_MAX_SPEED с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("stbdist"))	{
		        	if(!input_json.getString("stbdist").equals(
		        			this.mainActiv.prefs.getString("START_BACK_DISTANCE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("START_BACK_DISTANCE");
		        			edt.putString("START_BACK_DISTANCE", input_json.getString("stbdist"));
		        			edt.commit();
		        			this.mainActiv.START_BACK_DISTANCE = this.strToIntDef(
		        					this.mainActiv.prefs.getString("START_BACK_DISTANCE", "0"), 0);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек START_BACK_DISTANCE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("stbctm"))	{
		        	if(!input_json.getString("stbctm").equals(
		        			this.mainActiv.prefs.getString("START_BACK_TIME", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("START_BACK_TIME");
		        			edt.putString("START_BACK_TIME", input_json.getString("stbctm"));
		        			edt.commit();
		        			this.mainActiv.START_BACK_TIME = this.strToIntDef(
		        					this.mainActiv.prefs.getString("START_BACK_TIME", "0"), 0);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек START_BACK_TIME с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("regbctm"))	{
		        	if(!input_json.getString("regbctm").equals(
		        			this.mainActiv.prefs.getString("REGULAR_BACK_TIME", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("REGULAR_BACK_TIME");
		        			edt.putString("REGULAR_BACK_TIME", input_json.getString("regbctm"));
		        			edt.commit();
		        			//int RBT = this.strToIntDef(
		        			//		this.mainActiv.prefs.getString("REGULAR_BACK_TIME", "1"), 1);
		        			//this.mainActiv.REGULAR_BACK_TIME = ((RBT>0)?RBT*60:15);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек REGULAR_BACK_TIME с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("regbdst"))	{
		        	if(!input_json.getString("regbdst").equals(
		        			this.mainActiv.prefs.getString("REGULAR_BACK_DISTANCE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("REGULAR_BACK_DISTANCE");
		        			edt.putString("REGULAR_BACK_DISTANCE", input_json.getString("regbdst"));
		        			edt.commit();
		        			this.mainActiv.REGULAR_BACK_DISTANCE = this.strToIntDef(
		        					this.mainActiv.prefs.getString("REGULAR_BACK_DISTANCE", "30"), 30);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек REGULAR_BACK_DISTANCE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("MOPR"))	{
		        	if(!input_json.getString("MOPR").equals(
		        			this.mainActiv.prefs.getString("MAX_ORDER_PRICE", "0")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("MAX_ORDER_PRICE");
		        			edt.putString("MAX_ORDER_PRICE", input_json.getString("MOPR"));
		        			edt.commit();
		        			this.mainActiv.MAX_ORDER_PRICE = this.strToIntDef(
		        					this.mainActiv.prefs.getString("MAX_ORDER_PRICE", "0"), 0);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек MAX_ORDER_PRICE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("ovsdssum"))	{
		        	if(!input_json.getString("ovsdssum").equals(
		        			this.mainActiv.prefs.getString("FIXED_OVERST_DSUMM", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("FIXED_OVERST_DSUMM");
		        			edt.putString("FIXED_OVERST_DSUMM", input_json.getString("ovsdssum"));
		        			edt.commit();
		        			this.mainActiv.FIXED_OVERST_DSUMM = this.strToIntDef(
		        					this.mainActiv.prefs.getString("FIXED_OVERST_DSUMM", "0"), 0);
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек FIXED_OVERST_DSUMM с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("geocpref"))	{
		        	if(!input_json.getString("geocpref").equals(
		        			this.mainActiv.prefs.getString("GEOCODE_PREFIX", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			edt.remove("GEOCODE_PREFIX");
		        			edt.putString("GEOCODE_PREFIX", input_json.getString("geocpref"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек GEOCODE_PREFIX с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("ALOC"))	{
		        	if(input_json.getString("ALOC").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("ALARM_ORDER_CONFIRM", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("ALARM_ORDER_CONFIRM");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("ALARM_ORDER_CONFIRM", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек ALARM_ORDER_CONFIRM true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("ALOC").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("ALARM_ORDER_CONFIRM", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("ALARM_ORDER_CONFIRM");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("ALARM_ORDER_CONFIRM", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек ALARM_ORDER_CONFIRM false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("MSREF"))	{
		        	if(input_json.getString("MSREF").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("MANUAL_SECTOR_REFRESH", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("MANUAL_SECTOR_REFRESH");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("MANUAL_SECTOR_REFRESH", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек MANUAL_SECTOR_REFRESH true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("MSREF").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("MANUAL_SECTOR_REFRESH", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("MANUAL_SECTOR_REFRESH");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("MANUAL_SECTOR_REFRESH", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек MANUAL_SECTOR_REFRESH false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("UGPT"))	{
		        	if(input_json.getString("UGPT").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_GPS_TAXOMETER", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_GPS_TAXOMETER");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_GPS_TAXOMETER", true);
		        			edt.commit();
		        			this.mainActiv.USE_GPS_TAXOMETER=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_GPS_TAXOMETER true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("UGPT").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_GPS_TAXOMETER", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_GPS_TAXOMETER");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_GPS_TAXOMETER", false);
		        			edt.commit();
		        			this.mainActiv.USE_GPS_TAXOMETER=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_GPS_TAXOMETER false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("NETL"))	{
		        	if(input_json.getString("NETL").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_NETWORK_LOCATION", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_NETWORK_LOCATION");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_NETWORK_LOCATION", true);
		        			edt.commit();
		        			this.mainActiv.USE_NETWORK_LOCATION=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_NETWORK_LOCATION true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("NETL").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_NETWORK_LOCATION", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_NETWORK_LOCATION");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_NETWORK_LOCATION", false);
		        			edt.commit();
		        			this.mainActiv.USE_NETWORK_LOCATION=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_NETWORK_LOCATION false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("UBLK"))	{
		        	if(input_json.getString("UBLK").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_BOTH_LOCATIONS", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_BOTH_LOCATIONS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_BOTH_LOCATIONS", true);
		        			edt.commit();
		        			this.mainActiv.USE_BOTH_LOCATIONS=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_BOTH_LOCATIONS true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("UBLK").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_BOTH_LOCATIONS", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_BOTH_LOCATIONS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_BOTH_LOCATIONS", false);
		        			edt.commit();
		        			this.mainActiv.USE_BOTH_LOCATIONS=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_BOTH_LOCATIONS false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("iuse_bl"))	{
		        	if(input_json.getString("iuse_bl").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_BOTH_LOCATIONS", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_BOTH_LOCATIONS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_BOTH_LOCATIONS", true);
		        			edt.commit();
		        			this.mainActiv.USE_BOTH_LOCATIONS=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_BOTH_LOCATIONS true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("iuse_bl").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_BOTH_LOCATIONS", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_BOTH_LOCATIONS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_BOTH_LOCATIONS", false);
		        			edt.commit();
		        			this.mainActiv.USE_BOTH_LOCATIONS=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_BOTH_LOCATIONS false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("iuse_tm"))	{
		        	if(input_json.getString("iuse_tm").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_GPS_TAXOMETER", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_GPS_TAXOMETER");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_GPS_TAXOMETER", true);
		        			edt.commit();
		        			this.mainActiv.USE_GPS_TAXOMETER=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_GPS_TAXOMETER true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("iuse_tm").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_GPS_TAXOMETER", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_GPS_TAXOMETER");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_GPS_TAXOMETER", false);
		        			edt.commit();
		        			this.mainActiv.USE_GPS_TAXOMETER=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_GPS_TAXOMETER false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("iuse_nls"))	{
		        	if(input_json.getString("iuse_nls").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_NETWORK_LOCATION", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_NETWORK_LOCATION");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_NETWORK_LOCATION", true);
		        			edt.commit();
		        			this.mainActiv.USE_NETWORK_LOCATION=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_NETWORK_LOCATION true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("iuse_nls").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_NETWORK_LOCATION", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_NETWORK_LOCATION");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_NETWORK_LOCATION", false);
		        			edt.commit();
		        			this.mainActiv.USE_NETWORK_LOCATION=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_NETWORK_LOCATION false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("CNWF"))	{
		        	if(input_json.getString("CNWF").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("CONFIRM_WIFI_ENABLED", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CONFIRM_WIFI_ENABLED");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CONFIRM_WIFI_ENABLED", true);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CONFIRM_WIFI_ENABLED true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("CNWF").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("CONFIRM_WIFI_ENABLED", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CONFIRM_WIFI_ENABLED");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CONFIRM_WIFI_ENABLED", false);
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CONFIRM_WIFI_ENABLED false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("blck_ngps"))	{
		        	if(input_json.getString("blck_ngps").equals("yes"))	{
		        		this.mainActiv.block_none_gps=true;
		        	}
	        	}
	        	
	        	if(input_json.has("reqgps"))	{
		        	if(input_json.getString("reqgps").equals("yes"))	{
		        		this.mainActiv.block_none_gps=true;
		        	}
	        	}
	        	
	        	if(input_json.has("UTDBL"))	{
		        	if(input_json.getString("UTDBL").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("USE_TIME_DIST_BALANCE", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_TIME_DIST_BALANCE");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_TIME_DIST_BALANCE", true);
		        			edt.commit();
		        			this.mainActiv.USE_TIME_DIST_BALANCE=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_TIME_DIST_BALANCE true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("UTDBL").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("USE_TIME_DIST_BALANCE", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("USE_TIME_DIST_BALANCE");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("USE_TIME_DIST_BALANCE", false);
		        			edt.commit();
		        			this.mainActiv.USE_TIME_DIST_BALANCE=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек USE_TIME_DIST_BALANCE false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("TICAB"))	{
		        	if(input_json.getString("TICAB").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("TAXOMETR_INCCALL_ABORT", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("TAXOMETR_INCCALL_ABORT");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("TAXOMETR_INCCALL_ABORT", true);
		        			edt.commit();
		        			this.mainActiv.TAXOMETR_INCCALL_ABORT=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TAXOMETR_INCCALL_ABORT true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("TICAB").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("TAXOMETR_INCCALL_ABORT", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("TAXOMETR_INCCALL_ABORT");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("TAXOMETR_INCCALL_ABORT", false);
		        			edt.commit();
		        			this.mainActiv.TAXOMETR_INCCALL_ABORT=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TAXOMETR_INCCALL_ABORT false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	
	        	if(input_json.has("TASS"))	{
		        	if(input_json.getString("TASS").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("TAXOMETR_AS_SERVICE", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("TAXOMETR_AS_SERVICE");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("TAXOMETR_AS_SERVICE", true);
		        			edt.commit();
		        			//this.mainActiv.TAXOMETR_AS_SERVICE=true;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TAXOMETR_AS_SERVICE true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("TASS").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("TAXOMETR_AS_SERVICE", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("TAXOMETR_AS_SERVICE");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("TAXOMETR_AS_SERVICE", false);
		        			edt.commit();
		        			//this.mainActiv.TAXOMETR_AS_SERVICE=false;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек TAXOMETR_AS_SERVICE false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

	        	
	        	if(input_json.has("SCCR"))	{
		        	if(input_json.getString("SCCR").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("SEND_CURR_COORDS", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SEND_CURR_COORDS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("SEND_CURR_COORDS", true);
		        			edt.commit();
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SEND_CURR_COORDS true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("SCCR").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("SEND_CURR_COORDS", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SEND_CURR_COORDS");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("SEND_CURR_COORDS", false);
		        			edt.commit();
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SEND_CURR_COORDS false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("SLTSD"))	{
		        	if(input_json.getString("SLTSD").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("SLEEP_TIME_STDIST", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SLEEP_TIME_STDIST");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("SLEEP_TIME_STDIST", true);
		        			edt.commit();
		        			this.mainActiv.SLEEP_TIME_STDIST=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SLEEP_TIME_STDIST true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("SLTSD").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("SLEEP_TIME_STDIST", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SLEEP_TIME_STDIST");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("SLEEP_TIME_STDIST", false);
		        			edt.commit();
		        			this.mainActiv.SLEEP_TIME_STDIST=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SLEEP_TIME_STDIST false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	
	        	if(input_json.has("CSLDN"))	{
		        	if(input_json.getString("CSLDN").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("CALC_SALE_DINAMYC", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CALC_SALE_DINAMYC");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CALC_SALE_DINAMYC", true);
		        			edt.commit();
		        			this.mainActiv.CALC_SALE_DINAMYC=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CALC_SALE_DINAMYC true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("CSLDN").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("CALC_SALE_DINAMYC", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CALC_SALE_DINAMYC");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CALC_SALE_DINAMYC", false);
		        			edt.commit();
		        			this.mainActiv.CALC_SALE_DINAMYC=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CALC_SALE_DINAMYC false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("RESTTXM"))	{
		        	if(input_json.getString("RESTTXM").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("RESTORE_TAXOMETR", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("RESTORE_TAXOMETR");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("RESTORE_TAXOMETR", true);
		        			edt.commit();
		        			this.mainActiv.RESTORE_TAXOMETR=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек RESTORE_TAXOMETR true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("RESTTXM").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("RESTORE_TAXOMETR", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("RESTORE_TAXOMETR");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("RESTORE_TAXOMETR", false);
		        			edt.commit();
		        			this.mainActiv.RESTORE_TAXOMETR=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек RESTORE_TAXOMETR false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("CHKTAR"))	{
		        	if(input_json.getString("CHKTAR").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("CHECK_TARIF_AREA", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CHECK_TARIF_AREA");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CHECK_TARIF_AREA", true);
		        			edt.commit();
		        			this.mainActiv.CHECK_TARIF_AREA=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CHECK_TARIF_AREA true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("CHKTAR").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("CHECK_TARIF_AREA", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("CHECK_TARIF_AREA");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("CHECK_TARIF_AREA", false);
		        			edt.commit();
		        			this.mainActiv.CHECK_TARIF_AREA=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек CHECK_TARIF_AREA false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

	        	if(input_json.has("SKINS"))	{
		        	if(input_json.getString("SKINS").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("SOCKET_IN_SERVICE", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SOCKET_IN_SERVICE");
		        			} catch(Exception rex)	{

		        			}
		        			edt.putBoolean("SOCKET_IN_SERVICE", true);
		        			edt.commit();
		        			//this.mainActiv.SOCKET_IN_SERVICE=true;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SOCKET_IN_SERVICE true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}

		        	if(input_json.getString("SKINS").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("SOCKET_IN_SERVICE", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SOCKET_IN_SERVICE");
		        			} catch(Exception rex)	{

		        			}
		        			edt.putBoolean("SOCKET_IN_SERVICE", false);
		        			edt.commit();
		        			//this.mainActiv.SOCKET_IN_SERVICE=false;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SOCKET_IN_SERVICE false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

	        	if(input_json.has("SKINST"))	{
		        	if(input_json.getString("SKINST").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("SOCKET_IN_SERVICE", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SOCKET_IN_SERVICE");
		        			} catch(Exception rex)	{

		        			}
		        			edt.putBoolean("SOCKET_IN_SERVICE", true);
		        			edt.commit();
		        			//this.mainActiv.SOCKET_IN_SERVICE=true;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SOCKET_IN_SERVICE true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}

		        	if(input_json.getString("SKINST").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("SOCKET_IN_SERVICE", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("SOCKET_IN_SERVICE");
		        			} catch(Exception rex)	{

		        			}
		        			edt.putBoolean("SOCKET_IN_SERVICE", false);
		        			edt.commit();
		        			//this.mainActiv.SOCKET_IN_SERVICE=false;
		        			shutDownCriticalParams();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек SOCKET_IN_SERVICE false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("WDLGA"))	{
		        	if(input_json.getString("WDLGA").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("WAIT_DLG_AUTO", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("WAIT_DLG_AUTO");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("WAIT_DLG_AUTO", true);
		        			edt.commit();
		        			this.mainActiv.WAIT_DLG_AUTO=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек WAIT_DLG_AUTO true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("WDLGA").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("WAIT_DLG_AUTO", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("WAIT_DLG_AUTO");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("WAIT_DLG_AUTO", false);
		        			edt.commit();
		        			this.mainActiv.WAIT_DLG_AUTO=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек WAIT_DLG_AUTO false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("WDLWS"))	{
		        	if(input_json.getString("WDLWS").equals("yes")&&
		        			!this.mainActiv.prefs.getBoolean("WAIT_DLG_WITH_SECT", false))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("WAIT_DLG_WITH_SECT");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("WAIT_DLG_WITH_SECT", true);
		        			edt.commit();
		        			this.mainActiv.WAIT_DLG_WITH_SECT=true;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек WAIT_DLG_WITH_SECT true с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        	
		        	if(input_json.getString("WDLWS").equals("no")&&
		        			this.mainActiv.prefs.getBoolean("WAIT_DLG_WITH_SECT", true))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("WAIT_DLG_WITH_SECT");
		        			} catch(Exception rex)	{
	        				
		        			}
		        			edt.putBoolean("WAIT_DLG_WITH_SECT", false);
		        			edt.commit();
		        			this.mainActiv.WAIT_DLG_WITH_SECT=false;
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек WAIT_DLG_WITH_SECT false с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

                if(input_json.has("HOSWO"))	{
                    if(input_json.getString("HOSWO").equals("yes")&&
                            !this.mainActiv.prefs.getBoolean("HIDE_OTH_SECT_WAIT_ORDS", false))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("HIDE_OTH_SECT_WAIT_ORDS");
                            } catch(Exception rex)	{

                            }
                            edt.putBoolean("HIDE_OTH_SECT_WAIT_ORDS", true);
                            edt.commit();
                            this.mainActiv.HIDE_OTH_SECT_WAIT_ORDS=true;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек HIDE_OTH_SECT_WAIT_ORDS true с сервера! "+
                                    pex.getMessage());
                        }
                    }

                    if(input_json.getString("HOSWO").equals("no")&&
                            this.mainActiv.prefs.getBoolean("HIDE_OTH_SECT_WAIT_ORDS", true))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("HIDE_OTH_SECT_WAIT_ORDS");
                            } catch(Exception rex)	{

                            }
                            edt.putBoolean("HIDE_OTH_SECT_WAIT_ORDS", false);
                            edt.commit();
                            this.mainActiv.HIDE_OTH_SECT_WAIT_ORDS=false;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек HIDE_OTH_SECT_WAIT_ORDS false с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }

                this.mainActiv.SHOW_ALL_SECT_WAIT_MANUAL = input_json.has("SASWM") &&
                        input_json.getString("SASWM").equals("yes");
                this.mainActiv.DONT_WAIT_ORDER_IN_BUSY_STATUS = input_json.has("DWIBS") &&
                        input_json.getString("DWIBS").equals("yes");
                this.mainActiv.TAXOMETR_BLOCK_WITHOUT_ON_PLACE = input_json.has("TBWOP") &&
                        input_json.getString("TBWOP").equals("yes");

	        	if(input_json.has("RPHC"))	{
		        	if(!input_json.getString("RPHC").equals(
		        			this.mainActiv.prefs.getString("REGION_PHONE_CODE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("REGION_PHONE_CODE");
		        			} catch(Exception rex)	{
		        				
		        			}
		        			edt.putString("REGION_PHONE_CODE", input_json.getString("RPHC"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек REGION_PHONE_CODE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }
	        	
	        	if(input_json.has("DSPH"))	{
		        	if(!input_json.getString("DSPH").equals(
		        			this.mainActiv.prefs.getString("DISP_PHONE", "---")))	{
		        		try	{
		        			Editor edt = this.mainActiv.prefs.edit();
		        			try	{
		        				edt.remove("DISP_PHONE");
		        			} catch(Exception rex)	{
		        				
		        			}
		        			edt.putString("DISP_PHONE", input_json.getString("DSPH"));
		        			edt.commit();
		        		} catch (Exception pex)	{
		        			this.showMyMsg("Неудачное присваивание настроек DISP_PHONE с сервера! "+
		        					pex.getMessage());
		        		}
		        	}
		        }

                if(input_json.has("cur_shr"))	{
                    if(!input_json.getString("cur_shr").equals(
                            this.mainActiv.prefs.getString("CURRENCY_SHORT", "---")))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("CURRENCY_SHORT");
                            } catch(Exception rex)	{

                            }
                            edt.putString("CURRENCY_SHORT", input_json.getString("cur_shr"));
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек CURRENCY_SHORT с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }

                if(input_json.has("sphc"))	{
                    if(!input_json.getString("sphc").equals(
                            this.mainActiv.prefs.getString("STATE_PHONE_CODE", "---")))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("STATE_PHONE_CODE");
                            } catch(Exception rex)	{

                            }
                            edt.putString("STATE_PHONE_CODE", input_json.getString("sphc"));
                            edt.commit();
                            shutDownCriticalParams();
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек STATE_PHONE_CODE с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }

                if(input_json.has("LCFOI"))	{
                    if(input_json.getString("LCFOI").equals("yes"))	{
                        try	{
                            this.mainActiv.LOCK_FREE_ORDERS_INFO=true;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек LOCK_FREE_ORDERS_INFO true с сервера! "+
                                    pex.getMessage());
                        }
                    }

                    if(input_json.getString("LCFOI").equals("no"))	{
                        try	{
                            this.mainActiv.LOCK_FREE_ORDERS_INFO=false;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек LOCK_FREE_ORDERS_INFO false с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }

                if(input_json.has("ADS"))	{
                    if(input_json.getString("ADS").equals("yes")&&
                            !this.mainActiv.prefs.getBoolean("AUTO_DETECT_SECTOR", false))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("AUTO_DETECT_SECTOR");
                            } catch(Exception rex)	{

                            }
                            edt.putBoolean("AUTO_DETECT_SECTOR", true);
                            edt.commit();
                            this.mainActiv.AUTO_DETECT_SECTOR=true;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек AUTO_DETECT_SECTOR true с сервера! "+
                                    pex.getMessage());
                        }
                    }

                    if(input_json.getString("ADS").equals("no")&&
                            this.mainActiv.prefs.getBoolean("AUTO_DETECT_SECTOR", true))	{
                        try	{
                            Editor edt = this.mainActiv.prefs.edit();
                            try	{
                                edt.remove("AUTO_DETECT_SECTOR");
                            } catch(Exception rex)	{

                            }
                            edt.putBoolean("AUTO_DETECT_SECTOR", false);
                            edt.commit();
                            this.mainActiv.AUTO_DETECT_SECTOR=false;
                        } catch (Exception pex)	{
                            this.showMyMsg("Неудачное присваивание настроек AUTO_DETECT_SECTOR false с сервера! "+
                                    pex.getMessage());
                        }
                    }
                }

                //USE_NEW_COORD_LOC_ALGORYTHM
	        			
        	}	catch(Exception ex)	{
        		this.addToLogMessage(
                        " \nНеудачный парсинг SETTINGS-JSON! "+ex.getMessage());
        	}

        	this.mainActiv.settingsLoaded = true;
        	checkClientVersion();
        }
        
        public void checkClientVersion()	{
        	if (this.srvClientVersion<0)
        		this.srvClientVersion=this.minSrvClientVersion;
        	if (this.minSrvClientVersion<0)
        		this.minSrvClientVersion=this.srvClientVersion;
        	if (this.minSrvClientVersion>this.srvClientVersion)
        		this.minSrvClientVersion=this.srvClientVersion;
        	if (this.srvClientVersion>=2102)	{
        		if ((this.clientVersion<this.minSrvClientVersion)||
        			((this.clientVersion<this.srvClientVersion)&&this.mandatorySrvUpdate)
        			)	{
        			this.showServerMsg("Обязательно обновите программу, высока вероятность сбоя!");
        			Message msg = new Message();
                	msg.obj = this.mainActiv;
                	msg.arg1 = ConnectionActivity.SHOTDOWN_SIGNAL;
                	//Bundle bnd = new Bundle();
                	this.mainActiv.handle.sendMessage(msg);
        		}
        		else if (this.clientVersion<this.srvClientVersion)	{
        			this.showServerMsg("Программа требует обновления, лучше его провести!");
        		}
        	}
        }
        
        public void startGPSCRequest()	{
        	//this.showMyMsg("sock show timer");
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.START_GPSC_REQUEST;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showTimerVal()	{
        	//this.showMyMsg("sock show timer");
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_TIMER_VAL;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void checkTimerConflict()	{
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.CHECK_TIMER_CONFLICT;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void RESETTMETERPROCESS()	{
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.RESET_TMETER_PROCESS;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void shutDownSvrSide()	{
        	this.showServerMsg("Сервер сообщил о неудачном соединении!");
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOTDOWN_SVR_SIDE;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void shutDownCriticalParams()	{
        	this.showServerMsg("Сервер передал критически важные параметры!");
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOTDOWN_CRITICAL_PRM;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void parseOutStat(JSONObject input_json)	{
        	
        	if(input_json.has("succ")) {
        		try {
					if(input_json.
					        getString("succ").equals("yes"))	{
						Message msg = new Message();
						msg.obj = this.mainActiv;
						msg.arg1 = ConnectionActivity.SHOTDOWN_LINEOUT;
						//Bundle bnd = new Bundle();
						this.mainActiv.handle.sendMessage(msg);
					}
					else
		        		this.showServerMsg("Сервер сообщил о неудачном снятии с линии!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.showServerMsg("Сервер сообщил о неудачном снятии с линии!");
				}
        	}
        	else
        		this.showServerMsg("Сервер сообщил о неудачном снятии с линии!");
        }
        
        public void changeSector()	{
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.CHANGE_SECTOR;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void setSectorDirection()	{
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.CHANGE_SECTOR_DIRECTION;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void saleOrder()	{
			Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.PAY_ORDER;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public int getActiveOrderIndex()	{
        	for(int i=0;i<activeOrders.size();i++)	{
        		if(activeOrders.elementAt(i).orderId.equals(
        				this.activeOrderID))	{
        			return i;
        		}
        	}
        	return -1;
        }

        /*
         * Возвращает признак того, что водитель на точке
         * для активного заказа
         * @return boolean
         */
        public boolean getActiveOrderDrOnPlace() {
            int activeOrderIndex = getActiveOrderIndex();
            if (activeOrderIndex >= 0) {
                return activeOrders
                        .elementAt(activeOrderIndex)
                        .driverOnPlace;
            }
            return false;
        }
        
        public void resetTMVars()	{
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.RESET_TM_VARS;
        	//Bundle bnd = new Bundle();
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public double calculateTOSumm(double time, int distance, int start_distance, int prev_summ)	{
        	double res=0;
        	double time_summ=0, distance_summ=0;
        	int tmp_distance=distance;
        	if(prev_summ>0)
        		start_distance=0;
        	if((start_distance>0)&&prev_summ<=0)	{
        		if(start_distance>=distance)
        			distance=0;
        		else	{
        			if(this.mainActiv.FIXED_OVERST_DSUMM>0)	{
        				if (this.mainActiv.MAX_ORDER_PRICE>0)	{
        					this.mainActiv.last_summ=(this.mainActiv.FIXED_OVERST_DSUMM>=this.mainActiv.MAX_ORDER_PRICE?this.mainActiv.MAX_ORDER_PRICE:this.mainActiv.FIXED_OVERST_DSUMM);
        					return (this.mainActiv.FIXED_OVERST_DSUMM>=this.mainActiv.MAX_ORDER_PRICE?this.mainActiv.MAX_ORDER_PRICE:this.mainActiv.FIXED_OVERST_DSUMM);
        				}
        				else	{
        					this.mainActiv.last_summ=this.mainActiv.FIXED_OVERST_DSUMM;
        					return this.mainActiv.FIXED_OVERST_DSUMM;
        				}
        			}
        			else
        				distance=distance-start_distance;
        		}
        	}
        	
        	int aindx=getActiveOrderIndex();
        	int part_distance=0;
        	double part_tariff=0;
    		if(aindx>=0)	{
    			boolean def_tariff=false;
    			int atar_id=activeOrders.elementAt(aindx).ordTariffId;
    			String optComb=activeOrders.elementAt(aindx).ordOptComb;

    			if(atar_id>0)	{
    				
    				for(int i=0;i<ordersTarifs.size();i++)	{
    					if((ordersTarifs.elementAt(i).id==atar_id))	{
    						OrderTarif currTarif = ordersTarifs.
                                    elementAt(i);
    					    int tar_start_dist = ordersTarifs.
    								elementAt(i).start_dist,
                                missEveryKmValue = currTarif.missEveryKmValue;
    						if((start_distance<=0||tar_start_dist>start_distance)&&tar_start_dist>0)	{
    							if(tar_start_dist>=tmp_distance)
    			        			distance=0;
    			        		else
    			        			distance=tmp_distance-tar_start_dist;
    						}
    						time_summ=time*ordersTarifs.
    								elementAt(i).time_tariff;
    						if(distance>0) {
    						    if (missEveryKmValue > 0) {
    						        int missEveryMeterValue = missEveryKmValue * 1000;
                                    int fullMissCountDistance = (int) (distance / missEveryMeterValue) * 1000,
                                        missModDivideDistance = missEveryMeterValue - (int) (distance % missEveryMeterValue);

                                    distance = distance - fullMissCountDistance - (missModDivideDistance > 1000 ?
                                            0 : 1000 - missModDivideDistance);
                                }
                                distance_summ = distance * ordersTarifs.
                                        elementAt(i).tmeter_tariff / 1000;
                            }
    						else {
                                distance_summ = 0;
                            }
    						def_tariff=true;
    						if(distance>0)	{
    							part_distance=ordersTarifs.
    								elementAt(i).part_dist;
    							part_tariff=ordersTarifs.
    								elementAt(i).part_dist_tariff;
    							if(part_distance>0&&part_tariff>0)	{
    								distance_summ=distance_summ+
    									((distance % part_distance)+1)*part_tariff;
    			
    								this.mainActiv.dpart_count=distance % part_distance;
    							}	else	{
    								this.mainActiv.dpart_count=0;
    							}
    						}
    						break;
    					}
    					
    				}
    			}
    			
    			res=time_summ+distance_summ;

                if (this.calcTaxometerSummWithBonus) {
                    res -= activeOrders.elementAt(aindx).bonusUse;
                }
    			
    			if((optComb.length()>0)&&def_tariff) {
    				final int options_cnt = ordersOptions.size();
    				final boolean[] checkedOptItems = new boolean[options_cnt];
    				
    				for(int i=0;i<options_cnt;i++)	{
    					checkedOptItems[i]=false;
    				}
    				
    				StringTokenizer st = new StringTokenizer(optComb, " \t\n\r,."); 

    				while(st.hasMoreTokens())	{
    					// Получаем слово и что-нибудь делаем с ним, например,
    					// просто выводим на экран
    					int optsId = strToIntDef(st.nextToken(),0);
    					if(optsId>0)
    						for(int j=0;j<options_cnt;j++)	{
    							if(ordersOptions.
    									elementAt(j).id==optsId)
    								checkedOptItems[j]=true;
    						}

    					}
    				
    				/*int j=0;
    				while((optComb>0)&&(j<options_cnt))	{
    					if((optComb%2)!=0)
    						checkedOptItems[j]=true;
    					j++;
    					optComb = (int)(optComb/2);
    				}*/
    				
    				//boolean def_opts=false;
    				
    				for(int i=0;i<ordersOptions.size();i++)	{
    					if(checkedOptItems[i])	{
    						//def_opts=true;
    						res*=ordersOptions.elementAt(i).opt_coeff;
    						if(prev_summ<=0)
    							res+=ordersOptions.elementAt(i).opt_composed;
    					}
    				}
    			}

    			
    		}
        	if (this.mainActiv.MAX_ORDER_PRICE>0)	{
        		this.mainActiv.last_summ=((int)(res+prev_summ)>this.mainActiv.MAX_ORDER_PRICE?
            			this.mainActiv.MAX_ORDER_PRICE:(int)(res+prev_summ));
        		return ((int)(res+prev_summ)>this.mainActiv.MAX_ORDER_PRICE?
        			this.mainActiv.MAX_ORDER_PRICE:(int)(res+prev_summ));
        	}
        	else	{
        		this.mainActiv.last_summ=(int)(res+prev_summ);
        		//this.showMyMsg("!!!!"+(int)(res+prev_summ)+"aindx="+aindx+
        		//		"aocnt="+activeOrders.size()+"tcnt="+ordersTarifs.size()+
        		//		"oocnt="+ordersOptions.size());
        		return this.mainActiv.SHOW_KOPS_IN_SUMM ? (res+prev_summ) : (int)(res+prev_summ);
        	}
        }
        
        public void generateTOString()	{
        	String to_string="";
        	if(clientStatus==Driver.IN_WORKING)	{
        		int aindx=getActiveOrderIndex();
        		if(aindx>=0)	{
        			int tp_id=activeOrders.elementAt(aindx).tplan_id;
        			int atar_id=activeOrders.elementAt(aindx).ordTariffId;
        			String optComb=activeOrders.elementAt(aindx).ordOptComb;
        			
        			if(tp_id>0)	{
        				boolean def_tp=false;
        				for(int i=0;i<tarifPlans.size();i++)	{
        					if((tarifPlans.elementAt(i).id==tp_id)&&
        							(tarifPlans.elementAt(i).short_name.length()>0))	{
        						to_string+=(tarifPlans.elementAt(i).
        								short_name+"|");
        						def_tp=true;
        						break;
        					}
        					
        				}
        				if(!def_tp)
    						to_string+="?|";
        			}
        			else
        				to_string+="?|";
        			
        			if(atar_id>0)	{
        				boolean def_tariff=false;
        				for(int i=0;i<ordersTarifs.size();i++)	{
        					if((ordersTarifs.elementAt(i).id==atar_id)&&
        							(ordersTarifs.elementAt(i).short_name.length()>0))	{
        						to_string+=(ordersTarifs.elementAt(i).
        								short_name+"|");
        						def_tariff=true;
        						break;
        					}
        					
        				}
        				if(!def_tariff)
    						to_string+="0|";
        			}
        			else
        				to_string+="0|";
        			if(optComb.length()>0) {
        				final int options_cnt = ordersOptions.size();
        				final boolean[] checkedOptItems = new boolean[options_cnt];
        				
        				for(int i=0;i<options_cnt;i++)	{
        					checkedOptItems[i]=false;
        				}
        				
        				StringTokenizer st = new StringTokenizer(optComb, " \t\n\r,."); 

        				while(st.hasMoreTokens())	{
        					// Получаем слово и что-нибудь делаем с ним, например,
        					// просто выводим на экран
        					int optsId = strToIntDef(st.nextToken(),0);
        					if(optsId>0)
        						for(int j=0;j<options_cnt;j++)	{
        							if(ordersOptions.
        									elementAt(j).id==optsId)
        								checkedOptItems[j]=true;
        						}

        					}
        				
        				/*int j=0;
        				while((optComb>0)&&(j<options_cnt))	{
        					if((optComb%2)!=0)
        						checkedOptItems[j]=true;
        					j++;
        					optComb = (int)(optComb/2);
        				}*/
        				
        				boolean def_opts=false;
        				
        				for(int i=0;i<ordersOptions.size();i++)	{
        					if(checkedOptItems[i]&&(ordersOptions.elementAt(i).
        							short_name.length()>0))	{
        						def_opts=true;
        						to_string+=(ordersOptions.elementAt(i).
        								short_name+",");
        					}
        				}
        				if(!def_opts)
        					to_string+="0---";
        			}
        			else
        				to_string+="0---";
        			this.showOrderOptions(to_string);
        		}
        	}
        	else	{
        		this.showOrderOptions("0|0,0");
        	}
        }
        
        public boolean assignStatusValues(JSONObject input_json)    {
            int inputStatus, order_count=0;
            boolean alreadyTone = false;
            String prevActOid = this.activeOrderID;
            int prev_tarif_id=-1;
            int prev_stat=clientStatus;
            if(clientStatus==Driver.IN_WORKING||clientStatus==Driver.ON_ORDER_COMPLETING)	{
    			//if(ordersTarifs.size()>0)	{
    			int prev_act_ord_index = getActiveOrderIndex();
    			if(prev_act_ord_index>=0)	{
    				prev_tarif_id=activeOrders.
    						elementAt(prev_act_ord_index).ordTariffId;
    			}
    			//}
            }
            String driverStatus;
            boolean hasActiveOrder=false;
            boolean hasNextOrder=false;
            boolean hasFirstNewActOrder=false, hasNextNewActOrder=false;
            String newFirstActiveOrderID="", newFirstOrderData="";
            String newNextActiveOrderID="", newNextOrderData="";
            boolean hasChanges=false;
            String statusMessage="";
            
            hasChanges = false;
            if (this.mainActiv.none_gps_ecount>0&&
            		this.mainActiv.block_none_gps)	{
            	this.startGPSCRequest();
            }
            
            try {
            if(input_json.has("did")) {
                if(strToIntDef(input_json.
                        getString("did"), -1)>=0)    {
                    inputStatus = Driver.FREE_DRIVER;
                    driverStatus = input_json.
                        getString("dst");
                    if (driverStatus.equals("onln")) {
                        inputStatus = Driver.ON_REST;
                    }
                    if (driverStatus.equals("busy")) {
                        inputStatus = Driver.IN_WORKING;
                    }
                    
                    this.position = input_json.
                            getString("dp");
                    
                    if (!this.activeSectorID.equals(input_json.
                        getString("sid")))    {
                        this.activeSectorID = input_json.
                            getString("sid");
                        this.autoDetectSendedSID = "";
                        this.activeSectorName = input_json.
                            getString("scn");
                        
                        statusMessage+="ВЫ ПЕРЕМЕЩЕНЫ НА СЕКТОР \""+
                                input_json.getString
                                ("scn")+"\"! ";
                        
                        hasChanges = true;
                        
                    }
                    
                    this.activeOrders.removeAllElements();
                                        
                    hasActiveOrder = false;
                    hasNextOrder = false;
                    hasFirstNewActOrder=false;
                    hasNextNewActOrder=false;
                    newFirstActiveOrderID="";
                    newFirstOrderData="";
                    newNextActiveOrderID="";
                    newNextOrderData="";
                    
                    int currentBaseStatus;
                    this.saveCrashData();
                    currentBaseStatus = this.lastConnectClientStatus;
                    order_count=0;
                    
                    if(strToIntDef(input_json.
                        getString("ocn"), -1)>0)    {
                        order_count = strToIntDef(input_json.
                        getString("ocn"), -1);
                        
                        for(int i=0;i<order_count;i++)  {

                            String ordInfo = "";

                            if (input_json.has("oppr"+i))	{
                                double prevSumm=
                                        this.strToDoubleDef(
                                                input_json.getString("oppr"+i), -1);
                                if(prevSumm>0)
                                    ordInfo+="("+prevSumm+"р.)";
                            }

                            if (input_json.has("opdn"+i))	{
                                double prevDistance=
                                        this.strToDoubleDef(
                                                input_json.getString("opdn"+i), -1);
                                if(prevDistance>0)
                                    ordInfo+="("+prevDistance+"км.)";
                            }

                            if (input_json.has("obus"+i))	{
                                double bonusUse =
                                        this.strToDoubleDef(
                                                input_json.getString("obus"+i), -1);
                                if (bonusUse > 0) {
                                    ordInfo += "(бонусы: " + bonusUse + ")";
                                }
                            }

                            String cargoDesc = "";
                            if (input_json.has("ocrd"+i))	{
                                cargoDesc=input_json.getString("ocrd"+i);
                                if(cargoDesc.length()>0)
                                    ordInfo+="("+cargoDesc+")";
                            }

                            if (input_json.has("oprd"+i) && cargoDesc.length()>0)	{
                                long prevDT=
                                        this.strToLongDef(
                                                input_json.getString("oprd"+i), -1);
                                if(prevDT>0) {
                                    Date date = new Date((prevDT - 10800)*1000L); // *1000 is to convert seconds to milliseconds
                                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm"); // the format of your date
                                    //sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                    //give a timezone reference for formating (see comment at the bottom
                                    ordInfo += "(" + sdf.format(date) + ")";
                                }
                            }

                            if (input_json.has("oena"+i))	{
                                String endAdres=input_json.getString("oena"+i);
                                if(endAdres.length()>0)
                                    ordInfo+="(->"+endAdres+")";
                            }
                        	
                        	if (input_json.has("sn"+i)) {
                                boolean isEarlyOrder = input_json.has("ie" + i) &&
                                        input_json.getString("ie" + i).equals("1");
                                sendOrderAcknow(input_json.getString("oid" + i),
                                        input_json.getString("odt" + i),
                                        (input_json.has("wtr" + i) && !isEarlyOrder
                                                ? (input_json.getString("wtr" + i).equals("0")
                                                    ? true
                                                    : false)
                                                : false)
                                );
                            }
                            
                            if ((input_json.getString("oid"+i).
                                equals(this.activeOrderID)) && 
                                (currentBaseStatus==Driver.IN_WORKING)) {
                                hasActiveOrder = true;
                            }
                            else if ((input_json.getString("oid"+i).
                                equals(this.nextOrderID))&&
                                    this.occupateNextOrder && 
                                (currentBaseStatus==Driver.IN_WORKING)){
                                hasNextOrder = true;
                            }
                            else if (!hasFirstNewActOrder) {
                                hasFirstNewActOrder=true;
                                newFirstActiveOrderID=
                                    input_json.getString("oid"+i);
                                newFirstOrderData=
                                    input_json.getString("odt"+i)+ordInfo;
                    
                            }
                            else if (!hasNextNewActOrder) {
                                hasNextNewActOrder=true;
                                newNextActiveOrderID=
                                    input_json.getString("oid"+i);
                                newNextOrderData=
                                    input_json.getString("odt"+i)+ordInfo;
                            }
                            
                            VectorIstructItem ord_item =
                                new VectorIstructItem(1);
                            ord_item.orderId =
                                input_json.getString("oid"+i);
                            ord_item.orderData =
                                input_json.getString("odt"+i)+ordInfo;
                            if (input_json.has("tar"+i))	{
                            	ord_item.ordTariffId=
                            			this.strToIntDef(
                            					input_json.getString("tar"+i), 0);
                            }
                            if (input_json.has("oo"+i))	{
                            	ord_item.ordOptComb=
                            			input_json.getString("oo"+i);
                            }
                            
                            if (input_json.has("otpid"+i))	{
                            	ord_item.tplan_id=
                            			this.strToIntDef(
                            					input_json.getString("otpid"+i), -1);
                            }

                            if (input_json.has("oppr"+i))	{
                                ord_item.prevSumm=
                                        this.strToDoubleDef(
                                                input_json.getString("oppr"+i), -1);
                            }

                            if (input_json.has("opdn"+i))	{
                                ord_item.prevDistance=
                                        this.strToDoubleDef(
                                                input_json.getString("opdn"+i), -1);
                            }

                            if (input_json.has("obus"+i))	{
                                ord_item.bonusUse=
                                        this.strToDoubleDef(
                                                input_json.getString("obus"+i), -1);
                            }

                            if (input_json.has("ocrd"+i))	{
                                ord_item.cargoDesc=input_json.getString("ocrd"+i);
                            }

                            if (input_json.has("oena"+i))	{
                                ord_item.endAdres=input_json.getString("oena"+i);
                            }

                            if (input_json.has("ocln"+i))	{
                                ord_item.clientName=input_json.getString("ocln"+i);
                            }

                            if (input_json.has("dopl" + i) &&
                                    input_json.getString("dopl" + i).equals("1"))	{
                                ord_item.driverOnPlace = true;
                            }

                            if (input_json.has("stlat"+i))	{
                                ord_item.clientLat=
                                        this.strToDoubleDef(
                                                input_json.getString("stlat"+i), -1);
                            }

                            if (input_json.has("stlon"+i))	{
                                ord_item.clientLon=
                                        this.strToDoubleDef(
                                                input_json.getString("stlon"+i), -1);
                            }

                            if (input_json.has("dlat"+i))	{
                                ord_item.destLat=
                                        this.strToDoubleDef(
                                                input_json.getString("dlat"+i), -1);
                            }

                            if (input_json.has("dlon"+i))	{
                                ord_item.destLon=
                                        this.strToDoubleDef(
                                                input_json.getString("dlon"+i), -1);
                            }
                            
                            this.activeOrders.addElement(ord_item);
                            
                        }
                        
                    }
                    
                    //this.sendAcceptStatus();
                    
                    //Р•СЃР»Рё СЃРµСЂРІРµСЂ РѕС€РёР±СЃСЏ, РјР°Р»Рѕ Р»Рё
                    if ((order_count>0)&&(inputStatus!=Driver.IN_WORKING))
                        inputStatus = Driver.IN_WORKING;
                    if (order_count==0) {
                        if ((currentBaseStatus==Driver.ON_REST)&&
                                (inputStatus==Driver.IN_WORKING))
                            inputStatus = Driver.ON_REST;
                        if (inputStatus==Driver.IN_WORKING)
                            inputStatus = Driver.FREE_DRIVER;
                    }        
                    
                    //this.checkServerActiveOrders();
                    
                    //order_count
                    //this.occupateNextOrder - РµСЃС‚СЊ РІС‚РѕРѕСЂР№ Р·Р°РєР°Р·
                    //currentBaseStatus==midlet.IN_WORKING - РµСЃС‚СЊ РїРµСЂРІС‹Р№ Р·Р°РєР°Р·
                    //hasActiveOrder - РЅР° СЃРµСЂРІРµСЂРµ РµСЃС‚СЊ С‚РµРє. Р°РєС‚ Р·Р°РєР°Р·
                    //hasNextOrder - РЅР° СЃРµСЂРІРµСЂРµ РµСЃС‚СЊ С‚РµРє. РІС‚РѕСЂРѕР№ Р·Р°РєР°Р·
                    //hasFirstNewActOrder=false;
                    //hasNextNewActOrder=false;
                    //newFirstActiveOrderID="";
                    //newFirstOrderData="";
                    //newNextActiveOrderID=""; 
                    //newNextOrderData="";
                    
                    //РЎРјРµРЅР° СЃС‚Р°С‚СѓСЃР°
                        
                    //РЈСЃС‚Р°РЅРѕРІРєР° С‚РµРєСѓС‰РµРіРѕ Рё СЃР»РµРґСѓСЋС‰РµРіРѕ (order_count>=2)
                    //, С‚РѕР»СЊРєРѕ С‚РµРєСѓС‰РµРіРѕ (order_count=1),
                    //Р»РёР±Рѕ РїРѕР»РЅС‹Р№ СЃР±СЂРѕСЃ Р·Р°РєР°Р·РѕРІ (РЅР° РїРµСЂРµСЂС‹РІРµ, СЃРІРѕР±РѕРґРЅС‹Р№ Рё 
                    //order_count=0)
                    
                    if(order_count==0)  {
                        if(currentBaseStatus==Driver.IN_WORKING)    {
                            hasChanges = true;
                            statusMessage+="Все заказы завершены. ";
                            //alreadyTone = true;
                            //this.playMP3(this.mainActiv.assignOrdTonePref);
                        }
                        if(currentBaseStatus!=inputStatus)  {
                            hasChanges = true;
                            if(inputStatus==Driver.ON_REST) {
                                statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'НА ПЕРЕРЫВЕ'!";
                            }
                            else if(inputStatus==Driver.FREE_DRIVER) {
                                if (currentBaseStatus == Driver.IN_WORKING) {
                                    alreadyTone = true;
                                    this.playMP3(this.mainActiv.reportOrdTonePref);
                                }
                                statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'СВОБОДЕН'!";
                            }
                            else    {
                                
                            }
                            this.clientStatus = inputStatus;
                                
                        }
                        if (this.occupateNextOrder) {
                            hasChanges = true;
                            this.occupateNextOrder = false;
                        }
                            
                    }
                    else    //order_count>0
                    {
                        if(currentBaseStatus==Driver.IN_WORKING)    {
                            if(!hasActiveOrder)  {
                                if(hasNextOrder)    {
                                    //Р·Р°РјРµРЅРµРЅ Р°РєС‚РёРІРЅС‹Р№
                                    statusMessage+="Заменен активный заказ. ";
                                    this.activeOrderID = this.nextOrderID;
                                    this.activeOrderData = this.nextOrderData;
                                    hasChanges = true;
                                    this.occupateNextOrder = false;
                                    alreadyTone = true;
                                    this.playMP3(this.mainActiv.assignOrdTonePref);
                                    if (hasFirstNewActOrder)    {
                                        //Р·Р°РјРµРЅРµРЅ РІС‚РѕСЂРѕР№ Р·Р°РєР°Р·
                                        statusMessage+="Заменен второй заказ. ";
                                        this.nextOrderID = newFirstActiveOrderID;
                                        this.nextOrderData = newFirstOrderData;
                                        this.occupateNextOrder = true;
                                    }
                                    else    {
                                        //СЂРµР°Р»РёР·РѕРІР°РЅ РІС‚РѕСЂРѕР№ Р·Р°РєР°Р·
                                        statusMessage+="Реализован второй заказ. ";
                                    }
                                }
                                else    {
                                    if (hasFirstNewActOrder)    {
                                        //Р·Р°РјРµРЅРµРЅ Р°РєС‚РёРІРЅС‹Р№
                                        statusMessage+="Заменен активный заказ. ";
                                        this.activeOrderID = newFirstActiveOrderID;
                                        this.activeOrderData = newFirstOrderData;
                                        hasChanges = true;
                                        alreadyTone = true;
                                        this.playMP3(this.mainActiv.assignOrdTonePref);
                                        if (hasNextNewActOrder)    {
                                            this.nextOrderID = newNextActiveOrderID;
                                            this.nextOrderData = newNextOrderData;
                                            if(this.occupateNextOrder)  { 
                                                //Р·Р°РјРµРЅРµРЅ СЃР»РµРґСѓСЋС‰РёР№
                                                statusMessage+="Заменен второй заказ. ";
                                            }
                                            this.occupateNextOrder = true;
                                        }   else    {
                                            if(this.occupateNextOrder)  { 
                                                //РѕС‚РјРµРЅРµРЅ СЃР»РµРґСѓСЋС‰РёР№
                                                statusMessage+="Отменен второй заказ. ";
                                                this.occupateNextOrder = false;
                                            }
                                        }
                                    }
                                    else    {
                                        //СЃР±СЂРѕСЃ РѕР±РѕРёС… РїР°СЂР°РјРµС‚СЂРѕРІ, РёР·РјРµРЅРµРЅРёСЏ РµСЃС‚СЊ
                                        //РѕС‚РјРµРЅРµРЅС‹ Р°РєС‚РёРІРЅС‹Р№ Рё СЃР»РµРґСѓСЋС‰РёР№ Р·Р°РєР°Р·С‹
                                        statusMessage+="Все заказы завершены. ";
                                        hasChanges = true;
                                        if(this.occupateNextOrder)
                                            this.occupateNextOrder = false;
                                    }
                                }
                            }
                            else //hasActiveOrder==true
                            {
                                if(hasNextOrder)    {
                                    //РЅРёС‡РµРіРѕ РЅРµ РјРµРЅСЏРµС‚СЃСЏ
                                }
                                else //!hasNextOrder
                                {
                                    
                                    if (hasFirstNewActOrder)    {
                                        //Р·Р°РјРµРЅРµРЅ СЃР»РµРґСѓСЋС‰РёР№
                                        statusMessage+="Заменен второй заказ. ";
                                        hasChanges = true;
                                        this.nextOrderID = newFirstActiveOrderID;
                                        this.nextOrderData = newFirstOrderData;
                                        this.occupateNextOrder = true;
                                        alreadyTone = true;
                                        this.playMP3(this.mainActiv.assignOrdTonePref);
                                    }   else    {
                                        if(this.occupateNextOrder)  {
                                            //РѕС‚РјРµРЅРµРЅ СЃР»РµРґСѓСЋС‰РёР№
                                            statusMessage+="Отменен второй заказ. ";
                                            hasChanges = true;
                                            this.occupateNextOrder = false;
                                        }
                                    }
                                    
                                }
                            }
                            
                            if(currentBaseStatus!=inputStatus)  {
                                hasChanges = true;
                                if(inputStatus==Driver.ON_REST) {
                                    statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'НА ПЕРЕРЫВЕ'!";
                                }
                                else if(inputStatus==Driver.FREE_DRIVER) {
                                    if (currentBaseStatus == Driver.IN_WORKING) {
                                        alreadyTone = true;
                                        this.playMP3(this.mainActiv.reportOrdTonePref);
                                    }
                                    statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'СВОБОДЕН'!";
                                }
                                else    {

                                }
                                this.clientStatus = inputStatus;
                            }
                            else
                                if (hasChanges) {
                                    this.clientStatus = inputStatus;
                                }
                            
                        }
                        else //currentBaseStatus == FREE or REST
                        {
                            if (hasFirstNewActOrder)    {
                                this.activeOrderID = newFirstActiveOrderID;
                                this.activeOrderData = newFirstOrderData;
                                hasChanges = true;
                                if(this.occupateNextOrder)
                                    this.occupateNextOrder = false;
                                if (hasNextNewActOrder)    {
                                    this.nextOrderID = newNextActiveOrderID;
                                    this.nextOrderData = newNextOrderData;
                                    this.occupateNextOrder = true;
                                }
                            }
                            
                            if(hasChanges)  {
                                statusMessage+=
                                    "ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'НА ЗАКАЗЕ'!";
                                this.clientStatus = Driver.IN_WORKING;
                                alreadyTone = true;
                                this.playMP3(this.mainActiv.assignOrdTonePref);
                            }
                            else    {
                                if(currentBaseStatus!=inputStatus)  {
                                    hasChanges = true;
                                    if(inputStatus==Driver.ON_REST) {
                                        statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'НА ПЕРЕРЫВЕ'!";
                                    }
                                    else if(inputStatus==Driver.FREE_DRIVER) {
                                        statusMessage+="ВЫ ПЕРЕВЕДЕНЫ В СТАТУС 'СВОБОДЕН'!";
                                    }
                                    else    {

                                    }
                                    this.clientStatus = inputStatus;

                                }
                            }
                            
                        }
                    }
                    
                    this.clientStatus = inputStatus;
                    //if (!occupateNextOrder)
                    //	this.nextOrderID = "";
                    //	this.nextOrderData = "Нет второго заказа";
                    
                
                    if (hasChanges)
                    {
                        if (!alreadyTone) {
                            this.playMTones(4);
                        }
                        this.showServerMsg(statusMessage);
                    }
                    
                //РљРѕРЅРµС† СѓСЃР»РѕРІРёСЏ РїСЂРѕРІРµСЂРєРё Р�Р” РІРѕРґРёС‚РµР»СЏ
                }
            }
            }   catch(Exception ex) {
                this.addToLogMessage(
                    " \nНеудачный парсинг STATUS-JSON! "+ex.getMessage());
                }
            
            try	{
            
            this.viewDriverStatus();
            this.generateTOString();
            
            for (int o_counter=0;o_counter<
                    inputOrders.size();o_counter++)    {
                VectorIstructItem o_checkItem =
                    (VectorIstructItem)inputOrders.
                        elementAt(o_counter);
                if (o_checkItem.has_acknowlegment)	
                if((o_checkItem.orderId.equals(
                		this.activeOrderID)&&
                		(this.clientStatus == Driver.IN_WORKING))||
                		(o_checkItem.orderId.equals(
                        this.nextOrderID)&&
                        this.occupateNextOrder))    {
                	}
                	else
                     o_checkItem.has_acknowlegment=false;
                }
            
            checkTimerConflict();
            
            //if (this.mainActiv.hasRestoreData&&(this.clientStatus == Driver.IN_WORKING))	{
            //	this.mainActiv.hasRestoreData=false;
            //	restoreTMeter();
            //}
            
            if (this.clientStatus == Driver.IN_WORKING)	{
            	
	            if(prevActOid.length()==0||this.strToIntDef(prevActOid, -1)<=0||
	            		!(prev_stat==Driver.IN_WORKING||
	            		prev_stat==Driver.ON_ORDER_COMPLETING))	{
	            	
	            } 
	            else if(!prevActOid.equals(this.activeOrderID)&&(this.strToIntDef(prevActOid, -1)>0))	{
	            	//
	            	//this.mainActiv.last_summ=0;
	            	//this.mainActiv.orderHistory = "Заказ #"+activeOrderID;
	            	
	            	String aaa = this.mainActiv.getCurrTMData();	
	            	if(prev_tarif_id>0&&this.mainActiv.CHECK_TARIF_AREA&&this.mainActiv.last_summ>0)	{
	            			this.mainActiv.orderHistory+=" Тариф №"+prev_tarif_id+", сумма="+this.mainActiv.last_summ+
	            					aaa;
	            			sendOrderHistory(prevActOid, this.mainActiv.orderHistory,
	            					this.mainActiv.last_summ);
	            			
	            		}
	            	//if (restTMNextOrd)
    				RESETTMETERPROCESS();
	            	this.mainActiv.prev_summ=0;
	            	
	            	Message rtmsg = new Message();
	            	rtmsg.obj = this.mainActiv;
	            	rtmsg.arg1 = ConnectionActivity.RESET_TMETER;
	            	this.mainActiv.handle.sendMessage(rtmsg);
	            }
	            else if(prevActOid.equals(this.activeOrderID))	{
	            	//Тут проверяем не изменился ли тариф
	            	int now_tarif_id=-1;
	            	int now_act_ord_index = getActiveOrderIndex();
	    			if(now_act_ord_index>=0)	{
	    				now_tarif_id=activeOrders.
	    						elementAt(now_act_ord_index).ordTariffId;
	    			}
	    			if(prev_tarif_id>0&&prev_tarif_id!=now_tarif_id&&now_tarif_id>0&&this.mainActiv.CHECK_TARIF_AREA&&this.mainActiv.last_summ>0)	{
	    				this.mainActiv.orderHistory+=" Тариф №"+prev_tarif_id+", сумма="+this.mainActiv.last_summ+
	    						this.mainActiv.getCurrTMData();
	    				this.mainActiv.prev_summ=this.mainActiv.last_summ;
	    				sendOrderHistory(this.activeOrderID, this.mainActiv.orderHistory,
            					this.mainActiv.last_summ);
	    				this.mainActiv.startMillis = System.currentTimeMillis();
	    				resetTMVars();
	    				
	    			}
	    			
	            }
            }	else	{
            	//this.mainActiv.hasRequestToRestore=true;
            	
            	String aaa = this.mainActiv.getCurrTMData();	
            	if(prev_tarif_id>0&&this.mainActiv.CHECK_TARIF_AREA&&this.mainActiv.last_summ>0)	{
            			this.mainActiv.orderHistory+=" Тариф №"+prev_tarif_id+", сумма="+this.mainActiv.last_summ+
            					aaa;
            			sendOrderHistory(prevActOid, this.mainActiv.orderHistory,
            					this.mainActiv.last_summ);
            		}
            	
            	String message = "0 км";
        		Message msg = new Message();
            	msg.obj = this.mainActiv;
            	msg.arg1 = ConnectionActivity.SHOW_TAXMETER;
            	Bundle bnd = new Bundle();
            	bnd.putString("tmeter_text", message);
            	bnd.putInt("ltype", TaxometrSrvMode.LOCATION_NONE);
            	bnd.putDouble("tmeter_val", 0);
            	msg.setData(bnd);
            	this.mainActiv.handle.sendMessage(msg);
            	//this.mainActiv.orderHistory="";
            	this.mainActiv.timerValue=0;
            	this.mainActiv.prev_summ=0;
            	Message tmsg = new Message();
            	tmsg.obj = this.mainActiv;
            	tmsg.arg1 = ConnectionActivity.SHOW_TIMER_VAL;
            	this.mainActiv.handle.sendMessage(tmsg);
            }
            
            }   catch(Exception ex) {
            	this.showMyMsg("Неудачный постпарсинг! "+ex.getMessage());
            }
           
            return hasChanges;
            
        }

        public void stopSocket()    {
            try {
                if (wrapper != null) {
                    wrapper.destroyWrapper();
                    //wrapper = null;
                }
            } catch (Exception exw) {
            }
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException ex1) {
            }
            try {
                if (os != null) {
                    os.close();
                    os = null;
                }
            } catch (IOException ex1) {
            }
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException ex1) {
            }
            
        }
        
        public void proceedSyncIntruction(String instruction)   {
            try {
            JSONObject json = new JSONObject(instruction);
            
            if (instruction.indexOf("{\"sync\":\"ok\"")!=-1)    {
                //wrapper.resetNoHasASK();
                String oper_type = json.getString("oper_type");
		if (oper_type.equals("order_accepting"))
                    if (this.operateFreezeState && this.potentialOrderID.
                        equals(json.getString("order_id")) && 
                        this.clientStatus==Driver.IN_ACCEPT_SYNC)   {
                    
                    
                    this.operateFreezeState = false;
                    
                    //try {
                        String request = "{\"sync_end\":\"ok\""+
                            ",\"order_id\":\""+this.potentialOrderID+
                            "\",\"oper_type\":\"order_accepting\","+
                            "\"msg_end\":\"ok\"}";

                        //os.write(request.getBytes());
                        //os.flush();
                        //wrapper.sendToServer(request);
                        
                        if(this.directSend(request, "СИСТЕМНОЕ ПОДТВ. ЗАЯВКИ")) {
                        
                        this.activeOrderID = this.potentialOrderID;
                        this.activeOrderData = this.potentialOrderData;
                        this.clientStatus = Driver.IN_WORKING;
                        this.viewDriverStatus();
                        
                        showDialogElement(
                          TDDialog.TB_SHOW_MSG,"ВЫ НА ЗАКАЗЕ \""+
                          this.activeOrderData+"\"");
                        //playMTones(4);
                            this.playMP3(this.mainActiv.assignOrdTonePref);
                        } else  {
                    //} catch(Exception ex)   {
                      //  midlet.ConnectionFormMenu.messageLabel.setText(
                      //      midlet.ConnectionFormMenu.messageLabel.getText()+
                      //      " \nРќРµСѓРґР°С‡РЅР°СЏ РїРµСЂРµРґР°С‡Р° ASK-OK! "+ex.getMessage());
                        this.fastConnection = false;
                        this.clientStatus = Driver.FREE_DRIVER;
                        this.viewDriverStatus();
                        
                        showDialogElement(
                           TDDialog.TB_SHOW_MSG,"СБОЙ ПРИ ПОСТАНОВКЕ НА ЗАКАЗ \""+
                           this.potentialOrderData+"\"");
                        playMTones(4);
                        }
                    //} 
                    
                    
                } else if (this.nextOrderID.
                        equals(json.getString("order_id")) && 
                        this.acceptSyncNextOrder)   {
                    
                    
                    //try {
                        String request = "{\"sync_end\":\"ok\""+
                            ",\"order_id\":\""+this.nextOrderID+
                            "\",\"oper_type\":\"order_accepting\","+
                            "\"msg_end\":\"ok\"}";

                        //os.write(request.getBytes());
                        //os.flush();
                        //wrapper.sendToServer(request);
                        
                        //this.activeOrderID = this.potentialOrderID;
                        //this.activeOrderData = this.potentialOrderData;
                        //this.clientStatus = midlet.IN_WORKING;
                        
                        if(this.directSend(request, "СИСТЕМНОЕ ПОДТВ. ЗАЯВКИ")) {
                        
                            if ((clientStatus==Driver.FREE_DRIVER)||
                                (clientStatus==Driver.IN_DECISION)||
                                (clientStatus==Driver.IN_ACCEPT_DECITION)) {

                                this.activeOrderID = this.nextOrderID;
                                this.activeOrderData = this.nextOrderData;
                                this.clientStatus = Driver.IN_WORKING;
                                this.viewDriverStatus();

                                showDialogElement(
                                   TDDialog.TB_SHOW_MSG,"ВЫ НА ВТОРОМ (ЗАРАНЕЕ"+
                                   " ПРИНЯТОМ) ЗАКАЗЕ \""+
                                   this.activeOrderData+"\"");
                                //playMTones(4);
                                this.playMP3(this.mainActiv.assignOrdTonePref);
                            }
                            else
                                this.occupateNextOrder=true;
                        
                        
                        } else  {
                            this.fastConnection = false;
                            this.showedNextOrder = false;
                            this.acceptNextOrder = false;
                            this.acceptSyncNextOrder = false;
                            this.occupateNextOrder=false;

                            showDialogElement(
                               TDDialog.TB_SHOW_MSG,
                               "СБОЙ ПРИ ПОСТАНОВКЕ "+
                               "НА ВТОРОЙ ЗАКАЗ ЗАРАНЕЕ \""+
                               this.nextOrderData+"\"");
                            playMTones(4);
                        }
                        
                        this.acceptSyncNextOrder = false;
                    
                }
            }
            else if (instruction.indexOf("{\"sync\":\"no\"")!=-1)    {
                //wrapper.resetNoHasASK();
            }
            else if (instruction.indexOf("{\"sync_ask\":\"ok\"")!=-1)    {
                //wrapper.resetNoHasASK();
            }
            else if (instruction.indexOf("{\"sync_ask\":\"no\"")!=-1)    {
                //wrapper.resetNoHasASK();
            }
            else if (instruction.indexOf("{\"sync_end\":\"ok\"")!=-1)    {
                //wrapper.resetNoHasASK();
            }
            else if (instruction.indexOf("{\"sync_end\":\"no\"")!=-1)    {
                //wrapper.resetNoHasASK();
            }
            
            } catch(Exception ex) {
                this.addToLogMessage(
                    " \nНеудачный парсинг SYNC-JSON! "+
                    ex.getMessage());
                }
            
        }
        
        public void saveCrashData() {
            this.lastConnectOrderID="";
            this.lastConnectOrderData="";
            if ((this.clientStatus==Driver.FREE_DRIVER)||
                    (this.clientStatus==Driver.IN_ACCEPT_SYNC)||
                    (this.clientStatus==Driver.IN_DECISION)||
                    (this.clientStatus==Driver.ON_REST_ATTEMPT)||
                    (this.clientStatus==Driver.OUT_FROM_LINE)||
                    (this.clientStatus==Driver.IN_ACCEPT_DECITION))
                this.lastConnectClientStatus = Driver.FREE_DRIVER;
            else if ((this.clientStatus==Driver.IN_CANCELING)||
                    (this.clientStatus==Driver.IN_SELF_CANCELING)||
                    (this.clientStatus==Driver.ON_ORDER_COMPLETING)||
                    (this.clientStatus==Driver.IN_WORKING))
            {
                this.lastConnectOrderID = this.activeOrderID;
                this.lastConnectOrderData = this.activeOrderData;
                this.lastConnectClientStatus = Driver.IN_WORKING;
            }
            else if ((this.clientStatus==Driver.FROM_REST_ATTEMPT)||
                    (this.clientStatus==Driver.ON_REST))
                this.lastConnectClientStatus = Driver.ON_REST;
            else
              this.lastConnectClientStatus = Driver.FREE_DRIVER;  
        }
        
        public void restoreCrashData()   {
            this.activeOrderID = this.lastConnectOrderID;
            this.activeOrderData = this.lastConnectOrderData;
            this.clientStatus = this.lastConnectClientStatus;
        }
        
        public void showPaymentDialog(String msg_text)	{
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_PAYMENT_DLG;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_text", msg_text);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void playMP3(int res_id)	{
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.PLAY_MP3;
        	Bundle bnd = new Bundle();
        	bnd.putInt("res_id", res_id);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void rebuildSocketThread(boolean interrupt, boolean testCrash)	{
        	
        	if(this.interrupt||this.sockActReload)	{
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.REBUILD_SOCKET;
        	Bundle bnd = new Bundle();
        	bnd.putInt("res_id", 0);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        	this.interrupt = true;
        	}	else	{
        		try	{
        			showMyMsg(
                            "Сброс входного буфера сокета! " +
                                    (interrupt ? "interrupt " : "") +
                                    (testCrash ? "testCrash " : "") );
        		this.fastConnection=false;
        		testAttemptsCount=0;
        		this.clientStatus = Driver.NOT_CONNECTED;
                this.lastConnectOrderID="";
                this.lastConnectOrderData="";
                this.lastConnectClientStatus=Driver.FREE_DRIVER;
                this.operateFreezeState = false;
                this.first_sect_request = true;
                this.send_ask = false;
                this.dataProcessor = new SocketDataProcessorSrvMode(this);
                this.wrapper = new SocketWrapperSrvMode(this);
                this.has_IO_activity = true;
        		stopSocket();
        		//this.socket.shutdownInput();
        		} catch(Exception cex)	{
            		showMyMsg(
                            "===>Сброс входного буфера сокета!");
            	}
        	}
        }
        
        public void showOrderOptions(String ooptions)   {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SET_ORD_OPTS;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", ooptions);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showServerMsg(String message)   {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_SERVER_MSG;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", message);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showCustomMsg(String caption, String message)   {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_CUSTOM_MSG;
        	Bundle bnd = new Bundle();
        	bnd.putString("caption", caption);
        	bnd.putString("msg_lbl_text", message);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showMyMsg(String message)   {
        	//try	{
        	//int i=this.mainActiv.dpart_count;
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_MY_MSG;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", message);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showWaitOrderDlg()   {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.AUTO_SHOW_WAIT_ORD_DLG;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", "---");
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public boolean connGood()	{
        	if(!this.fastConnection)	{
        		showDialogElement(TDDialog.TB_SHOW_MSG,
        				"Восстанавливается соединение с сервером, подождите...");
        		return false;
        	}
        	else
        		return true;
        }
        
        public void setLogMessage(String text)    {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SET_MSG_LABEL;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", text);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void setMainFormTitle(String title)  {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SET_TITLE_LABEL;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", title);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void setActiveOrderLabel(String text)	{
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SET_ORDER_LABEL;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", text);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void setSectorLabel(String sector)   {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SET_SECTOR_LABEL;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", sector);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void showDialogElement(int dlg_type, String tmsg) {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.SHOW_DLG;
        	Bundle bnd = new Bundle();
        	bnd.putInt("dlg_type", dlg_type);
        	bnd.putString("msg_lbl_text", tmsg);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }
        
        public void addToLogMessage(String text)    {
        	Message msg = new Message();
        	msg.obj = this.mainActiv;
        	msg.arg1 = ConnectionActivity.ADD_LOG_MSG;
        	Bundle bnd = new Bundle();
        	bnd.putString("msg_lbl_text", text);
        	msg.setData(bnd);
        	this.mainActiv.handle.sendMessage(msg);
        }

        public void run() {
        
        String sect_request="ok";
        String name = this.server+":"+this.port;
        String alt_name = this.alternativeServer+":"+this.alternativePort;
        String end_str="";
        boolean use_utf8 = true;
        int conntout=8000;
        
        //this.showMyMsg("Try to stArt service socket!");
       
        this.clientStatus = Driver.NOT_AUTORIZED;
            
        while (true && !interrupt)    {

        if (!this.fastConnection)    {
        	
        	if (this.mainActiv.SOCK_CONN_TIMEOUT>0&&this.mainActiv.SOCK_CONN_TIMEOUT>8&&
        			this.mainActiv.SOCK_CONN_TIMEOUT<=90)	{
        		conntout=this.mainActiv.SOCK_CONN_TIMEOUT*1000;
        	}
            
        	setMainFormTitle("Соединяюсь...");
	        setLogMessage("Стартуем сокет.......");
	        this.saveCrashData();
	        stopSocket();
	        end_str = "";
	        this.has_IO_activity = true;
	        this.autorized = false;
	        if(this.badAttempts>10)
	        	this.clientStatus = Driver.NOT_AUTORIZED;
	        this.clientId = "";
	        if (this.wrapper.wait_ask)
	            wrapper.resetNoHasASK();

	        try {
	            
	            //socket = new Socket(this.server, 
	        		//this.strToIntDef(this.port,-1)); 
	        	socket = new Socket();
	        	socket.connect(new InetSocketAddress(
	        			this.server, 
	        			this.strToIntDef(this.port,6030)), conntout);
	            os = socket.getOutputStream(); is = socket.getInputStream();
	            bis = new BufferedInputStream(is);
	            
	        } catch (Exception ex) {
	        	
	        	setMainFormTitle("["+this.badAttempts+
	        			"]Неусп. подкл... ("+name+") "+ex.getMessage());
	        	try	{
	        		
	        		//socket = new Socket(this.alternativeServer, 
	    	        //    this.strToIntDef(this.port,-1));
	        		socket = new Socket();
	        		
	        		socket.connect(new InetSocketAddress(
		        			this.alternativeServer, 
		        			this.strToIntDef(this.port,6030)), conntout);
	        		os = socket.getOutputStream(); is = socket.getInputStream();
		            bis = new BufferedInputStream(is);
		            
	        	}	catch (Exception alt_ex) {
	        		
	        		setMainFormTitle("["+this.badAttempts+
	        				"]Неусп. подкл... ("+alt_name+")");
	        		if(this.badAttempts<=this.RECONNECT_NUMBERS) {
		                this.setLogMessage("СИНХР. С СЕРВЕРОМ.(RCN): "+alt_ex.getMessage());
		            }   else	{
		                this.setLogMessage("Сокет не инициализирован за более чем "+
		                	RECONNECT_NUMBERS+" попыток!"+
		                    " Выйдите из программы и попробуйте позже. Ошибка:"+
		                    ex.getMessage());
		                playMTones(4); this.interrupt = true; break;
		            }
		            this.badAttempts++; this.fastConnection=false;
		            try {
		            	Thread.sleep(5000);
		            } catch (Exception wex)  {
		                this.showMyMsg("\nОшибка ожидания!"+wex.getMessage());
		            }
	        		continue;
	        		
	        	}
	            
	        }
	        
	        setLogMessage("Стартуем сокет...OK");
	        this.fastConnection = true;
	        wrapper.startWrapper(os);
	        if(!this.first_sect_request)
	            sect_request="no";
	        String utf8_req = "\"uu\":\"no\",";
	        if(use_utf8)
	        	utf8_req = "\"uu\":\"ok\",";
	        String autr_instr = "{\"cmd\":\"autr\","+
	                "\"lg\":\""+this.login+
	                "\",\"pw\":\""+this.psw+"\","+
	                "\"sl\":\""+sect_request+"\","+utf8_req+
	                "\"msg_end\":\"ok\"}";
	        wrapper.sendToServer(autr_instr);
	        this.first_sect_request = false;
	        this.badAttempts=0;
	        //playMTones(4);
            this.playMP3(this.mainActiv.connectingTonePref);
            setMainFormTitle("Усп. подключение...");
        }
        else { }

        try {
        	boolean use_reader = true;
        	
        	do	{
                
                String reply;
                
                if (use_reader)	{
                	
                	final char[] in_buf = new char[8192];
                    final StringBuilder s_bld = new StringBuilder();
                    final Reader rin;
                    //if (use_utf8)
                    //	rin = new InputStreamReader(bis,"UTF-8");
                    //else
                    	rin = new InputStreamReader(bis);
                    int buf_len=0;
                
                	s_bld.append(end_str);
                	reply = s_bld.toString();
                	this.setLogMessage("<<<...");
                	while (reply.indexOf("}")==-1)	{
                		buf_len = rin.read(in_buf);
                		this.setLogMessage("<<<...<...");
                		if(buf_len<0)	{
                			this.fastConnection=false;
                			break;
                		}
                		s_bld.append(in_buf, 0, buf_len);
                		reply  = s_bld.toString();	
                	}
                
                	if (this.fastConnection)
                	{
                		if (reply.length()>(reply.indexOf("}")+1))
                			end_str=reply.substring
                				(reply.indexOf("}")+1);
                		else
                			end_str="";
                		reply = reply.substring(0,reply.indexOf("}")+1);
                	}
                		
                }
                else	{
                	
                	StringBuffer sb = new StringBuffer();
                    int c=-1; 
                    boolean start_input=true;
                
                    this.setLogMessage("<<<...");
                	while (((c = bis.read()) != '\n') && (c != -1) ) { //&&(c>0)&& (c!=0)
                    //if (((char)c!='0'))
                		if (start_input)    {
                			this.setLogMessage("<<<...<...");
                			start_input = false;
                		}
                		sb.append((char)c);
                		this.has_IO_activity = true;
                    
                		if ((c==-1)||//(is.available()<=0)||
                                ((char)c=='}'))
                			break;

                	}
                
                	if (c==-1)
                		this.fastConnection=false;
                	reply = sb.toString();
                
                }
                
                this.testAttemptsCount = 0;
                setLogMessage("<<<...OK");
                //setLogMessage("<<<...OK:" + sb.toString());

                if (this.fastConnection)	{
            
                	if (this.send_ask)
                		if (reply.indexOf("\"ask\":\"ok\"}")==-1)      
                			this.addToLogMessage("\nПередаю ASK-OK!");
            
                	try {
                	    if (this.mainActiv.loadDataFromSocketIO) {
                	        if (reply.indexOf("to_lst") != -1 ||
                                    reply.indexOf("s_lst") != -1) {
                	            continue;
                            }
                        }

                		JSONObject json = new JSONObject(reply);

                		if (reply.indexOf("\"ask\":\"ok\"}")!=-1)    {
                			if (this.wrapper.wait_ask)  
                				wrapper.resetNoHasASK();
                		}   
                		else if (reply.indexOf("{\"sync")!=-1)    {
                			proceedSyncIntruction(reply);
                		} 
                		else    {
                
                
                			if (this.send_ask) {
                				try {
                					String request = "{\"ask\":\"ok\"}";

                					os.write(request.getBytes());
                					os.flush();
                    
                					//РћР±СЂР°Р±Р°С‚С‹РІР°РµРј С‚РѕР»СЊРєРѕ РїРѕСЃР»Рµ СѓРґР°С‡РЅРѕРіРѕ РїРѕСЃС‹Р»Р° ASK
                					dataProcessor.proceedInputInstruction(json);
                					//wrapper.sendToServer(request);
                				} catch(Exception ex)   {
                					this.addToLogMessage(
                							"\nНеудачная передача ASK-OK! "+
                									ex.getMessage());
                					this.fastConnection = false;
                				} 
                
                			}
                			else
                				dataProcessor.proceedInputInstruction(json);  
             
                		}
            
                	} catch(Exception ex) {
                		this.fastConnection = false;
                		this.showMyMsg(
                				" \nНеудачный парсинг JSON! "+
                						ex.getMessage());
                	}
                }
                else
                {
                	this.showMyMsg(
            				"Обнаружен разрыв соединения при чтении!");
                }
            
        	} while(this.fastConnection && (end_str.indexOf("}")>=0) && use_reader);
            
            //midlet.ConnectionFormMenu.messageLabel.setText(
            //   midlet.ConnectionFormMenu.messageLabel.getText()+reply);
        }
        catch (ConnectException err)  {
           this.fastConnection = false;
           this.showMyMsg("СИНХР. С СЕРВЕРОМ.(CNFE)"+
                err.getMessage());
        }
        catch (IOException ex) {
            this.fastConnection = false;
            this.showMyMsg("СИНХР. С СЕРВЕРОМ.(IOE)"+
                ex.getMessage());
        }
        catch (Exception aex)    {
            this.fastConnection = false;
            this.showMyMsg("СИНХР. С СЕРВЕРОМ.(EX)"+
                    aex.getMessage());
        }

        if (!this.fastConnection) {
            // Close open streams and the socket
            this.saveCrashData();
            stopSocket();
          }
        else
        {
            
        }
        
        counter++;
        //addToLogMessage("\nРћР¶РёРґР°РЅРёРµ!");
        try {
            Thread.sleep(200);
        } catch (Exception ex)  {
            this.showMyMsg("\nОшибка ожидания!");
        }

        //break;
       
        }//РљРѕРЅРµС† РіР»Р°РІРЅРѕРіРѕ С†РёРєР»Р° РѕР±РјРµРЅР°-РїРѕРґРєР»СЋС‡РµРЅРёСЏ-РїРµСЂРµРїРѕРєР»СЋС‡РµРЅРёСЏ

        stopSocket();
        this.interrupt= true;
        
     }
    
    }
