package com.psdevelop.tdandrapp;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Vector;

/**
 *
 * @author РђРґРјРёРЅРёСЃС‚СЂР°С‚РѕСЂ
 */
public class SocketWrapperSrvMode extends Thread   {

        private SocketClientSrvMode ownerSockClient;
        OutputStream os = null;
        Vector<VectorIstructItem> sendVector;
        boolean lastSendindNOHasASK = false;
        boolean active = false;
        boolean wait_ask = false;
        int activity_counter = 0;
        int order_confirm_alarm_counter=0;

        public SocketWrapperSrvMode(SocketClientSrvMode sockClient)   {
            this.ownerSockClient = sockClient;
            this.sendVector = new Vector<VectorIstructItem>();
            this.lastSendindNOHasASK = false;
            this.active = false;
            this.wait_ask = false;
            this.activity_counter = 0;
            this.start();
        }
        
        public String getCurrDateTime()	{
        	int mHour, mMinute;
            int mYear;
            int mMonth;
            int mDay;
            
            // получаем текущее время
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            
            return mDay+"-"
    		+mMonth+"-"+mYear+" "+mHour+":"+mMinute;
        }
        
        public void startWrapper(OutputStream sock_os)  {
            this.os = sock_os;
            this.active = true;
        }

        public void run() {
            while (true)    {

                try {

                if (!this.lastSendindNOHasASK&&
                    ownerSockClient.
                    fastConnection&&
                    this.active)  {
                    if(!this.sendVector.isEmpty())    {
                        ownerSockClient.setLogMessage(">>>...");
                        VectorIstructItem out_data = 
                                (VectorIstructItem)this.
                                sendVector.firstElement();
                        String request_str =
                            out_data.JSONInstr;
                        os.write(request_str.getBytes());
                        os.flush();
                        out_data.onDeleted = true;
                        ownerSockClient.addToLogMessage(
                            "\nJSON trasmitted! "+request_str);

                        ownerSockClient.setLogMessage(">>>...OK");
                        if (this.wait_ask)
                            this.lastSendindNOHasASK = true;
                        else
                            this.sendVector.removeElementAt(0);
                        
                        ownerSockClient.has_IO_activity = true;
                        
                        }
                    }

                 }
                 catch (Exception ex)    {
                    ownerSockClient.showMyMsg(
                      "\nСИНХРОНИЗ С СЕРВЕРОМ (OUT DT)! "
                      +ex.getMessage());
                    ownerSockClient.saveCrashData();
                    ownerSockClient.fastConnection = false;
                    this.active = false;
                    //break;
                }
                
                if (ownerSockClient.interrupt||(ownerSockClient.testAttemptsCount>1))	{
                	ownerSockClient.setMainFormTitle("Пересоздание нити сокета...");
                	//ownerSockClient.playMP3(R.raw.guitar);
                    ownerSockClient.playMP3(this.ownerSockClient.mainActiv.connectingTonePref);
                	ownerSockClient.rebuildSocketThread(ownerSockClient.interrupt,
                            (ownerSockClient.testAttemptsCount>1));
                	break;
                }
                
                if (ownerSockClient.CHECK_CONNECTION)    {
                    
                    if ( (!ownerSockClient.has_IO_activity)&&
                            (this.activity_counter<
                            (ownerSockClient.CHECK_CONN_TIME+1)) )
                        this.activity_counter++;
                    
                    if (ownerSockClient.has_IO_activity)
                    {
                        ownerSockClient.
                                has_IO_activity = false;
                        this.activity_counter = 0;
                    }
                    else    {
                        if (this.activity_counter>=
                                ownerSockClient.CHECK_CONN_TIME)   {
                        	ownerSockClient.testAttemptsCount++;
                            ownerSockClient.setMainFormTitle(getCurrDateTime()+" Тест связи("+
                            		ownerSockClient.testAttemptsCount+")...");
                            ownerSockClient.sendStatusQuery();
                            
                            ownerSockClient.has_IO_activity = true;
                            this.activity_counter = 0;
                        }
                    }
                         
                }
                

                try {
                    sleep(100);
                } catch (Exception e) {
                    ownerSockClient.showMyMsg(
                    "\nОшибка ожидания отравки!"+e.getMessage());  
                } finally	{
                	
                }
                
                
                if (this.order_confirm_alarm_counter>0)	{
                	this.order_confirm_alarm_counter--;
                	if ((this.order_confirm_alarm_counter%50)==0)	{
                		ownerSockClient.alarmUncheckConfirm();
                	}
                }

            }
        }

        public void sendToServer(String JSONData)   {

            VectorIstructItem out_data = new 
                    VectorIstructItem(JSONData,0);
            this.sendVector.addElement(out_data);

        }

        public void resetNoHasASK() {
            if(!this.sendVector.isEmpty()&&
                        this.lastSendindNOHasASK)   {
                VectorIstructItem out_data = 
                        (VectorIstructItem)this.
                                sendVector.firstElement();
                if(out_data.onDeleted) ///?????    
                    this.sendVector.removeElementAt(0);
            }
            
            //????
            this.lastSendindNOHasASK = false;
            
        }
        
        public void destroyWrapper()    {
            try {
                this.sendVector.removeAllElements();
                this.active = false;
                //this.innterrupt();
            } catch (Exception ex) {
                
            }
        }
    }
