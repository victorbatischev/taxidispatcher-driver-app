package com.psdevelop.tdandrapp;

import android.os.Bundle;
import android.os.Message;

public class OrderTimerSrvMode extends Thread  {
	
	private GpsLocationDetector ownerSockClient;
	int timerCounter=0;
	int backPortionsCount=0;
	int hiddenTime=0;
	int prevBackTimeDistance=0;
	
	public OrderTimerSrvMode(GpsLocationDetector sockClient)   {
        this.ownerSockClient = sockClient;
        this.backPortionsCount = sockClient.backPortionsCount;
        this.start();
        ownerSockClient.startMillis=System.currentTimeMillis();
        timerCounter=0;
    }
	
	public void showTimerVal()	{
    	//this.showMyMsg("sock show timer");
		Message msg = new Message();
    	msg.obj = this.ownerSockClient;
    	msg.arg1 = ConnectionActivity.SHOW_TIMER_VAL;
    	//Bundle bnd = new Bundle();
    	this.ownerSockClient.handle.sendMessage(msg);
    }
	
	public void showMyMsg(String message)   {
    	Message msg = new Message();
    	msg.obj = this.ownerSockClient;
    	msg.arg1 = ConnectionActivity.SHOW_MY_MSG;
    	Bundle bnd = new Bundle();
    	bnd.putString("msg_lbl_text", message);
    	msg.setData(bnd);
    	this.ownerSockClient.handle.sendMessage(msg);
    }
	
	 public void run() {
         while (true)    {
         	 if (ownerSockClient.checkSectorCounter < 100) {
				 ownerSockClient.checkSectorCounter++;
			 }
        	 try {
                 sleep(3000);
                 timerCounter++;
             } catch (Exception e) {
                 showMyMsg(
                 "Ошибка цикла ожидания!"+e.getMessage());  
             } finally	{
             	if (ownerSockClient.timerIsActive && !(ownerSockClient.START_TIME_CALC_WITH_MENU &&
						ownerSockClient.CALC_TIME_STOPPED))	{
             		//if (ownerSockClient.timerValue<=0)
             		if ((ownerSockClient.BACK_TIME>0)&&ownerSockClient.USE_TIME_DIST_BALANCE)	{
             			ownerSockClient.BACK_TIME-=3;
             			if (ownerSockClient.BACK_TIME<0)
             				ownerSockClient.BACK_TIME=0;
             			ownerSockClient.startMillis=System.currentTimeMillis();
             			showTimerVal();
             		}	else	{ 
             			if(ownerSockClient.USE_TIME_DIST_BALANCE)	{
             				if((ownerSockClient.lastBackTimeDistance<
             						ownerSockClient.REGULAR_BACK_DISTANCE)
             						)	{
             					//if(prevBackTimeDistance>=ownerSockClient.REGULAR_BACK_DISTANCE)	{
             						
             					//}	else	{
             					ownerSockClient.BACK_TIME=0;
             					ownerSockClient.timerValue=
                 				(int)((System.currentTimeMillis()-
                 						ownerSockClient.startMillis)/1000)-this.hiddenTime;
             					showTimerVal();
             					//}
             				}
             				else	{
             					this.hiddenTime+=3;
             					ownerSockClient.BACK_TIME=0;
             					showTimerVal();
             			
             				}
             			} else
             			{
             				ownerSockClient.BACK_TIME=0;
                 			ownerSockClient.timerValue=
                 				(int)((System.currentTimeMillis()-
                 						ownerSockClient.startMillis)/1000);
                 			showTimerVal();
             			}
             		}
             		
             		
             	}	else if (!(ownerSockClient.START_TIME_CALC_WITH_MENU &&
						ownerSockClient.CALC_TIME_STOPPED))	{
             		this.hiddenTime=0;
             	}
             	if (ownerSockClient.timerValue>2000000000)
             		ownerSockClient.timerValue=0;
             }
        	 
        	 if(timerCounter>=6000000)
        		 timerCounter=0;
        	 
        	 
        	 if((backPortionsCount>0) && ownerSockClient.USE_TIME_DIST_BALANCE &&
					 !(ownerSockClient.START_TIME_CALC_WITH_MENU &&
							 ownerSockClient.CALC_TIME_STOPPED))	{
        		 prevBackTimeDistance=ownerSockClient.lastBackTimeDistance;
        		 ownerSockClient.backDistanceCalcIteration
        		 	(timerCounter%backPortionsCount);
        	 }
        	 
         }
         
	 }

}
