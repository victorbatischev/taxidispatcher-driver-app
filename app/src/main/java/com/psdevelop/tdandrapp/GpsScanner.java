package com.psdevelop.tdandrapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Bundle;
import android.os.Message;

public class GpsScanner extends Thread {
	
	GpsLocationDetector mainScan;
	boolean hasNewGPSDetection=false;
	
	public GpsScanner(GpsLocationDetector mainSc) {
		// TODO Auto-generated constructor stub
		this.mainScan = mainSc;
	}
	
	public void showMyMsg(String message)   {
    	Message msg = new Message();
    	msg.obj = this.mainScan;
    	msg.arg1 = ConnectionActivity.SHOW_MY_MSG;
    	Bundle bnd = new Bundle();
    	bnd.putString("msg_lbl_text", message);
    	msg.setData(bnd);
    	this.mainScan.handle.sendMessage(msg);
    }
	
	public static char getCheckStrOld(String checkingStr)	{
		char r = checkingStr.charAt(0);
		int ch_count = checkingStr.length();
		while (--ch_count>0)	{
			r^=checkingStr.charAt(ch_count);
		}
		try	{
			r = Integer.toHexString((int)r).charAt(0);
		} catch (Exception ex)	{
			r = '0';
		}
		return r;
	}
	
	public static String getCheckStr(String checkingStr)	{
		String res;
		char r = checkingStr.charAt(0);
		int ch_count = checkingStr.length();
		while (--ch_count>0)	{
			r^=checkingStr.charAt(ch_count);
		}
		try	{
			res = Integer.toHexString((int)r).charAt(0)+""+Integer.toHexString((int)r).charAt(1);
		} catch (Exception ex)	{
			res = "0";
		}
		return res;
	}
	
	public static String getCustomCurrTimeOpenGTSHttpRequest(String svrInetAddr, 
			String accountId, String devId, String A, String N, String W)	{
		String dataStr = "GPRMC,"+getCurrTime()+",A,"+A+",N,"+N+",W,"+W+","+
			getCurrDate()+",,";
		return "http://"+svrInetAddr+"/gprmc/Data?acct="+accountId+
				"&dev="+devId+"&gprmc=$"+dataStr+"*"+getCheckStr(dataStr);
	}
	
	public String getCurrTimeOpenGTSHttpRequest(String A, String N, String W)	{
		String dataStr = "GPRMC,"+getCurrTime()+",A,"+A+",N,"+N+",E,"+W+","+
			getCurrDate()+",,";
		return "http://"+this.mainScan.svrInetAddr+"/gprmc/Data?acct="+this.mainScan.accountId+
				"&dev="+this.mainScan.devId+"&gprmc=$"+dataStr+"*"+getCheckStr(dataStr);
	}
	
	public static String getTestOpenGTSHttpRequest(String svrInetAddr, String accountId, 
			String devId)	{
		return getCustomCurrTimeOpenGTSHttpRequest(svrInetAddr, 
				accountId, devId, "4453.4000", "03719.0000", "000.0,000.0");
	}
	
	public static String getCurrDate()	{
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        return dateFormat.format( new Date() ) ;
    }
    
    public static String getCurrTime()	{
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        return dateFormat.format( new Date() ) ; 
    }
    
    public static void sendTestGPSHTTPRequest(String svrInetAddr, String accountId, 
			String devId)	{
    	HttpGetRequest gpsGet = new HttpGetRequest(null,
    			getTestOpenGTSHttpRequest(svrInetAddr, 
    					accountId, devId));
			gpsGet.start();
    }
    
    public static String convertDoubleGCoordsToNMEA(double coord)	{
    	double flt = coord - (int)coord;
    	int dd = (int)coord;
    	int mm = (int)(60*flt);
    	double gg = 60*flt;
    	String ss = Integer.toString(((int)(10*gg))%10)+
    			Integer.toString(((int)(100*gg))%10)+
    			Integer.toString(((int)(1000*gg))%10)+
    			Integer.toString(((int)(10000*gg))%10);
    	return (dd>9?Integer.toString(dd):("0"+dd))
    			+(mm>9?Integer.toString(mm):("0"+mm))+"."
    			+ss;
    }
	
	public void run() {
		//int a=0;
		while(true)	{
			//a++;
			//if(a<10)	{	
			//}
			if (this.hasNewGPSDetection)	{
				try	{
					//DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
					//decimalFormatSymbols.setDecimalSeparator('.');
					//NumberFormat formatter = new DecimalFormat("#0.0000",decimalFormatSymbols);
					String AStr = convertDoubleGCoordsToNMEA(this.mainScan.lastLatitude);
					String NStr = convertDoubleGCoordsToNMEA(this.mainScan.lastLongitude);
					if (NStr.length()==9)
						NStr = "0"+NStr;
					
					HttpGetRequest gpsGet = new HttpGetRequest(this,
						getCurrTimeOpenGTSHttpRequest(AStr, NStr,
								//"4453.4000", "03719.00", 
								"000.0,000.0"));
					gpsGet.start();
					this.hasNewGPSDetection = false;
					if (!this.mainScan.hasFirstDetectSending&&
							this.mainScan.showGPSSysEvents)	{
						this.showMyMsg(getCurrTimeOpenGTSHttpRequest(AStr, NStr,
							"000.0,000.0"));
						this.mainScan.hasFirstDetectSending = true;
					}
				} catch (Exception ex)  {
		            this.showMyMsg("Ошибка трекинга сервис B!");
		        }
			}
			else	{ 	
				if (!this.mainScan.hasFirstDetectMissing&&
						this.mainScan.showGPSSysEvents)	{
					this.showMyMsg("Не определены данные сервиса В!");
					this.mainScan.hasFirstDetectMissing = true;
				}
			}
			try {
	            Thread.sleep(25000);
	        } catch (Exception ex)  {
	            this.showMyMsg("Ошибка ожидания сервис B!");
	        }
		}
	}

}
