package com.psdevelop.tdandrapp;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author РђРґРјРёРЅРёСЃС‚СЂР°С‚РѕСЂ
 */
import org.json.JSONObject;

public class SocketDataProcessorSrvMode extends Object {
    
    private SocketClientSrvMode ownerSockClient;
    
    public SocketDataProcessorSrvMode(
    		SocketClientSrvMode sockClient)  {
        this.ownerSockClient = sockClient;
    }
    
    public void proceedInputInstruction(JSONObject input_json)   {
            //Enumeration ken = input_json.keys();

            if (input_json.has("command")) {
                try {
                    String commandName =
                        input_json.getString("command");

                    if (commandName.equalsIgnoreCase("autr_ok")) {
                        ownerSockClient.autorized = true;
                        ownerSockClient.clientStatus =
                            Driver.FREE_DRIVER;
                        ownerSockClient.clientId =
                           input_json.getString
                           ("client_id");
                        ownerSockClient.restoreCrashData();
                        ownerSockClient.sendDrBalanceRequestStart();
                        ownerSockClient.sendWaitListRequest();
                    }
                    else if (commandName.equalsIgnoreCase
                            ("autr_uns"))    {
			//РќРµСѓРґР°С‡РЅР°СЏ Р°РІС‚РѕСЂРёР·Р°С†РёСЏ
                    	ownerSockClient.shutDownSvrSide();
                    }
                    
                    if (ownerSockClient.autorized)
                    if (commandName.equalsIgnoreCase("take_order")
                            )    {
                        //Предлагается заказ
                        boolean replyTake = false;
                        for (int o_counter=0;o_counter<
                                ownerSockClient.inputOrders.size();o_counter++)    {
                            VectorIstructItem o_checkItem =
                                (VectorIstructItem)ownerSockClient.inputOrders.
                                    elementAt(o_counter);
                            if(o_checkItem.orderId.equals(
                                    input_json.getString("order_id")))    {
                                 replyTake = true;
                                 break;
                            }
                        }
                        
                        if (!replyTake) {
                            VectorIstructItem ord_item =
                                new VectorIstructItem(1);
                            ord_item.orderId =
                                input_json.getString("order_id");
                            ord_item.orderData =
                                input_json.getString("order_data");
                            
                            boolean is_manual_accept = false;
                            
                            if (input_json.has("manual"))   {
                                if (input_json.getString("manual").
                                        equals("yes"))  {
                                    is_manual_accept = true;
                                }
                            }
                            
                            if (!is_manual_accept)
                               ord_item.auto_accepted = true; 
                            
                            ownerSockClient.inputOrders.addElement(ord_item);

                            //this.midlet.ConnectionFormMenu.
                            //    active_order_tf.setString(
                            //    ord_item.orderData);
                            
                            if (ownerSockClient.clientStatus==
                                    Driver.FREE_DRIVER)  {
                            
                                if(is_manual_accept)    {
                                   
                                    ownerSockClient.clientStatus = 
                                            Driver.IN_ACCEPT_DECITION;

                                    ownerSockClient.acceptOrderID = 
                                        input_json.getString("order_id");
                                    ownerSockClient.acceptOrderData = 
                                        input_json.getString("order_data");

                                    ownerSockClient.showDialogElement(
                                            TDDialog.TB_DECISION_WAIT,
                                            "Встать в очередь на заказ \""
                                            +ownerSockClient.acceptOrderData+"\"?");
                                    ownerSockClient.playMTones(6);
                                }
                                else    {
                                String v_obj = 
                                    "{\"command\":\"accept_order\","+
                                    "\"order_id\":\""+
                                    input_json.getString("order_id")+
                                    "\",\"client_id\":\""+
                                    ownerSockClient.clientId
                                    +"\",\"manual\":\"no\",\"msg_end\":\"ok\"}";
                                
                                ownerSockClient.wrapper.
                                    sendToServer(v_obj);
                                }
                                
                            } else if ((ownerSockClient.clientStatus 
                                == Driver.IN_WORKING)&&!this.
                                ownerSockClient.acceptNextOrder&&
                                !this.ownerSockClient.showedNextOrder&&
                                !this.ownerSockClient.acceptSyncNextOrder&&
                                !this.ownerSockClient.occupateNextOrder)    {
                            this.ownerSockClient.showedNextOrder=true;
                                    ownerSockClient.nextOrderID = 
                                        input_json.getString("order_id");
                                    ownerSockClient.nextOrderData = 
                                        input_json.getString("order_data");

                                    ownerSockClient.showDialogElement(TDDialog.TB_NEXT_DECISION, 
                                            "Встать сразу на второй заказ  \""+
                                            ownerSockClient.nextOrderData+"\"?");
                                    ownerSockClient.playMTones(6);
                            }
                            
                        }

                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "order_occuped"))    {
                    //РќРёС‡РµРіРѕ РЅРµ РґРµР»Р°С‚СЊ РїРѕРєР°
                      ownerSockClient.showDialogElement(TDDialog.TB_SHOW_MSG,
                           "Кто-то раньше перехватил заказ!");
                   
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "order_is_your"))    {
                    boolean its_manual=false;
                    its_manual = ownerSockClient.getOrderManualAccept(
                                    input_json.getString("order_id"));
                        //Р—Р°РєР°Р· Р·Р°РєСЂРµРїР»РµРЅ Р·Р° РєР»РёРµРЅС‚РѕРј
                        if(
                           ((ownerSockClient.clientStatus == Driver.FREE_DRIVER)||
                           (ownerSockClient.clientStatus == Driver.IN_ACCEPT_DECITION)||
                            ((ownerSockClient.clientStatus == Driver.IN_DECISION)
                                &&its_manual))
                           &&!(its_manual&&input_json.getString("order_id").
                                equals(ownerSockClient.nextOrderID)&&
                                ownerSockClient.acceptNextOrder)
                                //(this.clientStatus == midlet.IN_ACCEPT_SYNC)
                                //&&
                                //this.potentialOrderID.equals(
                                //input_json.getString("order_id"))
                                )  
                        {
                            //this.midlet.
                            //    ConnectionFormMenu.tbShowMsg.
                            //    setString(
                            //   "Р’С‹ РІС‹РїРѕР»РЅСЏРµС‚Рµ Р·Р°РєР°Р·: ");
                            
                            if  (!its_manual)  {
                                

                                ownerSockClient.clientStatus = Driver.IN_DECISION;

                                ownerSockClient.potentialOrderID = 
                                    input_json.getString("order_id");
                                ownerSockClient.potentialOrderData = 
                                    input_json.getString("order_data");

                                ownerSockClient.showDialogElement(
                                    TDDialog.TB_DECISION_WAIT,
                                    "!!!ВАМ НАЗНАЧЕН заказ \""+
                                    ownerSockClient.potentialOrderData+
                                    "\", подтвердить (желательно <=20 сек.)?");
                                ownerSockClient.playMTones(6);
                            }
                            else    {
                                
                                ownerSockClient.potentialOrderID = 
                                    input_json.getString("order_id");
                                ownerSockClient.potentialOrderData = 
                                    input_json.getString("order_data");
                                
                                ownerSockClient.clientStatus = 
                                    Driver.IN_ACCEPT_SYNC;
                                
                                ownerSockClient.operateFreezeState = true;
                                String v_obj = "{\"command\":\"order_is_my\","+
                                    "\"order_id\":\""+ownerSockClient.potentialOrderID+
                                    "\",\"client_id\":\""+ownerSockClient.clientId
                                    +"\",\"msg_end\":\"ok\"}";
                                ownerSockClient.wrapper.sendToServer(v_obj);
                                ownerSockClient.showDialogElement(
                                    TDDialog.TB_OPERATE_SYNC, 
                                    "Подождите, выполняется операция "+
                                    "постановки на ранее принятый заказ\""+
                                    ownerSockClient.potentialOrderData+"\"...");
                                ownerSockClient.playMTones(6);
                            }
                        } else if (its_manual&&input_json.getString("order_id").
                                equals(ownerSockClient.nextOrderID)&&
                                ownerSockClient.acceptNextOrder&&
                                !ownerSockClient.occupateNextOrder)    {
                            //this.ownerSockClient.showedNextOrder=true;
                            String v_obj = "{\"command\":\"order_is_my\","+
                                    "\"order_id\":\""+ownerSockClient.nextOrderID+
                                    "\",\"client_id\":\""+ownerSockClient.clientId
                                    +"\",\"msg_end\":\"ok\"}";
                            ownerSockClient.wrapper.sendToServer(v_obj);
                            ownerSockClient.acceptSyncNextOrder = true;
                            ownerSockClient.acceptNextOrder = false;
                        }
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "go_on_hand"))    {
                    //Р Р°Р·СЂРµС€РµРЅРѕ СЂР°Р±РѕС‚Р°С‚СЊ СЃ СЂСѓРєРё
                        //Р—Р°РєР°Р· Р·Р°РєСЂРµРїР»РµРЅ Р·Р° РєР»РёРµРЅС‚РѕРј
                        if((ownerSockClient.clientStatus == Driver.FREE_DRIVER)||
                           (ownerSockClient.clientStatus == Driver.IN_ACCEPT_DECITION)//||
                            //((ownerSockClient.clientStatus == midlet.IN_DECISION)
                            //    &&its_manual)
                                //(this.clientStatus == midlet.IN_ACCEPT_SYNC)
                                //&&
                                //this.potentialOrderID.equals(
                                //input_json.getString("order_id"))
                                )  
                        {
                            //this.midlet.
                            //    ConnectionFormMenu.tbShowMsg.
                            //    setString(
                            //   "Р’С‹ РІС‹РїРѕР»РЅСЏРµС‚Рµ Р·Р°РєР°Р·: ");
                            
                            //if  (!its_manual)  {
                                

                                ownerSockClient.clientStatus = 
                                        Driver.IN_DECISION;

                                ownerSockClient.potentialOrderID = 
                                    input_json.getString("order_id");
                                ownerSockClient.potentialOrderData = 
                                    input_json.getString("order_data");

                                ownerSockClient.showDialogElement(
                                    TDDialog.TB_DECISION_WAIT,
                                    "!!!Р’РђРњ Р РђР—Р Р•РЁР•Рќ Р·Р°РєР°Р· СЃ СЂСѓРєРё \""+
                                    ownerSockClient.potentialOrderData+
                                    "\", РїРѕРґС‚РІРµСЂРґРёС‚СЊ (Р¶РµР»Р°С‚РµР»СЊРЅРѕ <=20 СЃРµРє.)?");
                                ownerSockClient.playMTones(6);
                            //}
                            //else    {
                                
                                /*ownerSockClient.potentialOrderID = 
                                    input_json.getString("order_id");
                                ownerSockClient.potentialOrderData = 
                                    input_json.getString("order_data");
                                
                                midlet.TDSocketClient.clientStatus = 
                                    midlet.IN_ACCEPT_SYNC;
                                
                                ownerSockClient.operateFreezeState = true;
                                String v_obj = "{\"command\":\"order_is_my\","+
                                    "\"order_id\":\""+ownerSockClient.potentialOrderID+
                                    "\",\"client_id\":\""+ownerSockClient.clientId
                                    +"\",\"msg_end\":\"ok\"}";
                                ownerSockClient.wrapper.sendToServer(v_obj);
                                midlet.ConnectionFormMenu.
                                    tbOperateSync.setString(
                                    "РџРѕРґРѕР¶РґРёС‚Рµ, РІС‹РїРѕР»РЅСЏРµС‚СЃСЏ РѕРїРµСЂР°С†РёСЏ "+
                                    "РїРѕСЃС‚Р°РЅРѕРІРєРё РЅР° СЂР°РЅРµРµ РїСЂРёРЅСЏС‚С‹Р№ Р·Р°РєР°Р·\""+
                                    ownerSockClient.potentialOrderData+"\"...");
                                display.setCurrent(midlet.
                                        ConnectionFormMenu.tbOperateSync);
                                ownerSockClient.playMTones(6);*/
                            //}
                        }
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "onhand_block"))
                    {
                    //Р—Р°РїСЂРµС‰РµРЅРѕ СЂР°Р±РѕС‚Р°С‚СЊ СЃ СЂСѓРєРё
                        ownerSockClient.showDialogElement(
                           TDDialog.TB_SHOW_MSG,
                           "Вам не разрешили работать с руки!");
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "order_cancel"))    {
                    //Р”РёСЃРїРµС‚С‡РµСЂ СЃРґРµР»Р°Р» РѕС‚РјРµРЅСѓ
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "accept_working_abort"))    {
                    //Р”РёСЃРїРµС‚С‡РµСЂ РїРѕРґС‚РІРµСЂРґРёР» РѕС‚РјРµРЅСѓ РІРѕРґРёС‚РµР»РµРј
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "order_close"))    {
                        //Р”РёСЃРїРµС‚С‡РµСЂ РїРѕРґС‚РІРµСЂРґРёР» Р·Р°РєСЂС‹С‚РёРµ Р·Р°СЏРІРєРё
                        //String oper_type = input_json.getString("oper_type");
                        //if (oper_type.equals("order_comleting"))
                        if (ownerSockClient.operateFreezeState && 
                                ownerSockClient.activeOrderID.
                        equals(input_json.getString("order_id")) && 
                        ownerSockClient.clientStatus==Driver.ON_ORDER_COMPLETING)   {
                    
                    
                            ownerSockClient.operateFreezeState = false;

                            try {
                                String request = "{\"sync_end\":\"ok\""+
                                    ",\"order_id\":\""+ownerSockClient.potentialOrderID+
                                    "\",\"oper_type\":\"order_comleting\","+
                                    "\"msg_end\":\"ok\"}";

                                ownerSockClient.os.write(request.getBytes());
                                ownerSockClient.os.flush();
                                //wrapper.sendToServer(request);

                                //this.activeOrderID = this.potentialOrderID;
                                //this.activeOrderData = this.potentialOrderData;
                                if (ownerSockClient.occupateNextOrder)  {
                                    ownerSockClient.activeOrderID = 
                                            ownerSockClient.nextOrderID;
                                    ownerSockClient.activeOrderData = 
                                            ownerSockClient.nextOrderData;
                                    ownerSockClient.clientStatus = Driver.IN_WORKING;
                                    ownerSockClient.viewDriverStatus();

                                    ownerSockClient.showDialogElement(
                                        TDDialog.TB_SHOW_MSG,
                                        "!!!ОТЧЕТ ПРИНЯТ, ВЫ НА ВТОРОМ (ЗАРАНЕЕ"+
                                        " ПРИНЯТОМ) ЗАКАЗЕ \""+
                                        ownerSockClient.activeOrderData+"\"");
                                }
                                else    {
                                    ownerSockClient.clientStatus = Driver.FREE_DRIVER;
                                    ownerSockClient.viewDriverStatus();
                                
                                    ownerSockClient.showDialogElement(
                                        TDDialog.TB_SHOW_MSG,
                                        "Отчет по заказу \""+
                                        ownerSockClient.activeOrderData+
                                        "\" принят!");
                                    
                                }

                                ownerSockClient.playMTones(4);

                            } catch(Exception ex)   {
                                ownerSockClient.addToLogMessage(
                                    " \nНеудачная передача SYNC! "+
                                    ex.getMessage());
                                ownerSockClient.fastConnection = false;

                                
                                ownerSockClient.showDialogElement(
                                    TDDialog.TB_SHOW_MSG,
                                    "СБОЙ ПРИ ОТЧЕТЕ ПО ЗАКАЗУ \""+
                                    ownerSockClient.activeOrderData+"\"");
                                ownerSockClient.playMTones(4);

                            } 


                        }
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "error_order_close"))    {
                    //РћС€РёР±РєР° Р·Р°РєСЂС‹С‚РёСЏ Р·Р°СЏРІРєРё, РЅРµРІРµСЂРЅР°СЏ СЃСѓРјРјР°, РЅРѕ РµРµ РјРѕР¶РЅРѕ Р·Р°СЂР°РЅРµРµ
                    //РїСЂРѕРІРµСЂСЏС‚СЊ С‚СѓС‚
                        ownerSockClient.showDialogElement(
                            TDDialog.TB_SHOW_MSG,
                           "Ошибка закрытия заявки!");
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "remove_from_line"))    {
                    //Р”РёСЃРїРµС‚С‡РµСЂ РїРѕРґС‚РІРµСЂРґРёР» СЃРЅСЏС‚РёРµ СЃ Р»РёРЅРёРё
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "keep_on_line"))    {
                    //Р”РёСЃРїРµС‚С‡РµСЂ Р·Р°РїСЂРµС‚РёР» СЃРЅСЏС‚РёРµ СЃ Р»РёРЅРёРё
                        ownerSockClient.showDialogElement(
                           TDDialog.TB_SHOW_MSG,
                           "Вам не разрешили сняться с линии!");
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "go_on_rest"))    {
                        //Р”РёСЃРїРµС‚С‡РµСЂ РїРѕРґС‚РІРµСЂРґРёР» СѓС…РѕРґ РЅР° РїРµСЂРµСЂС‹РІ
                        this.ownerSockClient.clientStatus =
                                Driver.ON_REST;
                        this.ownerSockClient.viewDriverStatus();
                        this.ownerSockClient.showServerMsg
                                ("Вы установлены на перерыв!");
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "go_from_rest"))    {
                        //Р”РёСЃРїРµС‚С‡РµСЂ РїРѕРґС‚РІРµСЂРґРёР» РІС‹С…РѕРґ СЃ РїРµСЂРµСЂС‹РІР°
                        if ((ownerSockClient.clientStatus==
                        Driver.FREE_DRIVER)||
                        (ownerSockClient.clientStatus==
                        Driver.IN_DECISION)||
                        (ownerSockClient.clientStatus==
                        Driver.IN_ACCEPT_DECITION)||
                        (ownerSockClient.clientStatus==
                        Driver.FROM_REST_ATTEMPT)||
                        (ownerSockClient.clientStatus==
                        Driver.ON_REST)) {
                            this.ownerSockClient.clientStatus =
                                    Driver.FREE_DRIVER;
                            this.ownerSockClient.viewDriverStatus();
                            this.ownerSockClient.showServerMsg
                                    ("Вы сняты с перерыва!");
                        }
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "not_on_rest"))    {
                    //Р”РёСЃРїРµС‚С‡РµСЂ Р·Р°РїСЂРµС‚РёР» СѓС…РѕРґ РЅР° РїРµСЂРµСЂС‹РІ
                       ownerSockClient.showDialogElement(
                           TDDialog.TB_SHOW_MSG,
                           "Вам не разрешили уход на перерыв!");
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "queue_answer"))    {
                    //Р�РЅС„РѕСЂРјР°С†РёСЏ Рѕ РјРµСЃС‚РѕРїРѕР»РѕР¶РµРЅРёРё РІ РѕС‡РµСЂРµРґРё
                        ownerSockClient.showDialogElement(
                           TDDialog.TB_SHOW_MSG,
                           input_json.getString
                           ("msg"));
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "alert"))    {
                    //Р�РЅС„РѕСЂРјР°С†РёСЏ Рѕ РјРµСЃС‚РѕРїРѕР»РѕР¶РµРЅРёРё РІ РѕС‡РµСЂРµРґРё
                        ownerSockClient.showDialogElement(
                           TDDialog.TB_SHOW_MSG,
                           input_json.getString
                           ("msg"));
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "s_lst"))    {
                        if (ownerSockClient.mainActiv.loadDataFromSocketIO) {
                            return;
                        }
                        //ownerSockClient.showMyMsg("parseSectorList classic socket sectors");
                        ownerSockClient.parseSectorList(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "to_lst"))    {
                        if (ownerSockClient.mainActiv.loadDataFromSocketIO) {
                            return;
                        }
                        //ownerSockClient.showMyMsg("parse classic socket to list");
                        ownerSockClient.parseTarifOptionList(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "s_st"))    {
                        ownerSockClient.parseSectorsStatuses(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "goto_sector"))    {
                        if (ownerSockClient.
                                checkString(input_json.getString
                           ("sector_id")))  {
                            this.ownerSockClient.activeSectorID = 
                                input_json.getString
                                ("sector_id");
                            this.ownerSockClient.autoDetectSendedSID = "";
                            this.ownerSockClient.activeSectorName = 
                                input_json.getString
                                ("sector_name");
                            ownerSockClient.setSectorLabel(
                                    input_json.getString
                                    ("sector_name"));
                            
                            this.ownerSockClient.showServerMsg
                                    ("Вы на секторе \""+input_json.getString
                                    ("sector_name")+"\"!");                   
                        }
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "driver_status"))    {
                        if (ownerSockClient.mainActiv.loadDataFromSocketIO) {
                            ownerSockClient.mainActiv.sendActiveStatusRequest();
                            return;
                        }
                        ownerSockClient.
                                assignStatusValues(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "ford"))    {
                        if (ownerSockClient.mainActiv.loadDataFromSocketIO) {
                            return;
                        }
                        ownerSockClient.
                        	parseFreeOrders(input_json);
                    } else if (commandName.equalsIgnoreCase(
                                    "erlo")) {
                        if (ownerSockClient.mainActiv.loadDataFromSocketIO) {
                            ownerSockClient.mainActiv.sendEarlyStatusRequest();
                            return;
                        }
                        ownerSockClient.parseEarlyOrders(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "sets"))    {
                        ownerSockClient.
                                assignSettings(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "out_stat"))    {
                        ownerSockClient.
                                parseOutStat(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "gpsc_requ"))    {
                        ownerSockClient.startGPSCRequest();
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "opa"))    {
                    	if (input_json.getString("scs").
                    			equalsIgnoreCase("yes"))
                    		ownerSockClient.
                        		parseOperationAnswer(input_json);
                    }
                    else if (commandName.
                            equalsIgnoreCase(
                            "dpay_req"))    {
                    	ownerSockClient.
                    		showPaymentDialog(
                    				input_json.getString("txt"));
                    }
                    else    {
                    //Unsupported command
                    }

                }
                catch (Exception ex)    {
                    ownerSockClient.addToLogMessage(
                      "\nОшибка обработки входящей команды! "
                      +ex.getMessage());
                }
            }
            //String key_name;
            //while(ken.hasMoreElements())    {
            //key_name = ken.nextElement().toString();
            //}
        }
    
}
