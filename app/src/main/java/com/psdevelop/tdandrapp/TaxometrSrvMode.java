package com.psdevelop.tdandrapp;

import java.text.DecimalFormat;

import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Message;

public class TaxometrSrvMode extends Object implements LocationListener {
	
	public final static int LOCATION_NONE = 1;
	public final static int LOCATION_SATELLITE = 2;
	public final static int LOCATION_NETWORK = 3;
	
	private LocationManager myManager;
	private GpsLocationDetector mainActiv;
	String tmeterOrderId="";
	static boolean serviceActive=false;
	Location lastLocation=null;
	long lastLocationTime=-1;
	Location lastGPSLocation=null;
	long lastGPSLocationTime=-1;
	Location lastNETLocation=null;
	long lastNETLocationTime=-1;
	float summaryDist=0;
	static boolean singleGPSActivating=false;
	static boolean singleGPSForMapRoute=false;
	boolean gpsAviable=false;
	boolean netLocAviable=false;
	boolean lastLocationIsGPS=false;
	boolean lastLocationIsNET=false;
    int satellites = 0;
    int satellitesInFix = 0;
	
	//@Override
	//public void run() {
		
	//}

	public TaxometrSrvMode(GpsLocationDetector mActiv) {
		this.mainActiv=mActiv;
		try	{
			myManager = (LocationManager) mainActiv.getSystemService(mainActiv.LOCATION_SERVICE);
			GpsStatus.Listener lGPS = new GpsStatus.Listener() {
				public void onGpsStatusChanged(int event) {
					try	{
					if( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
			            //GpsStatus status = lm.getGpsStatus(null); 
			            //Iterable<GpsSatellite> sats = status.getSatellites();
			            //doSomething();
			        	satellites = 0;
			            satellitesInFix = 0;
			            //int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
			            //Log.i(TAG, "Time to first fix = " + timetofix);
			            for (GpsSatellite sat : myManager.getGpsStatus(null).getSatellites()) {
			                if(sat.usedInFix()) {
			                    satellitesInFix++;              
			                }
			                satellites++;
			            }
			            //Log.i(TAG, satellites + " Used In Last Fix ("+satellitesInFix+")");
			        }
					} catch(Exception gpse)	{
						showMyMsg("Ошибка определения статуса GPS! "+gpse.getMessage());
					}
			    }
			};
			myManager.addGpsStatusListener(lGPS);
			//showMyMsg("Инициализирован объект T!");
		} catch(Exception le)	{
			showMyMsg("Ошибка инициализации объекта T!");
		}
	}
	
	public void requestLUpd(boolean singleReq)	{
		try	{
			if(!singleReq)	{
				lastLocation=null;
				lastGPSLocation=null;
				lastNETLocation=null;
				lastLocationTime=-1;
				lastGPSLocationTime=-1;
				lastNETLocationTime=-1;
				summaryDist=0;
				this.mainActiv.dpart_count=0;
				showTMeter(summaryDist, LOCATION_NONE);
			}
			if (!singleGPSActivating&&!serviceActive)
			if (mainActiv.USE_NETWORK_LOCATION||mainActiv.USE_BOTH_LOCATIONS)	{
				myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
						5000, 0, this);
			}
			if (!mainActiv.USE_NETWORK_LOCATION||mainActiv.USE_BOTH_LOCATIONS)	{	
				myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
						5000, 0, this);
			}
			if(!singleReq)	{
				serviceActive = true;
			}	else	{
				singleGPSActivating=true;
			}
		} catch(Exception le)	{
			showMyMsg("Ошибка запуска слушателя TLM!");
		}
	}
	
	public void removeLUpd(boolean anyWay)	{
		try	{
			if(!singleGPSActivating||anyWay)	{
				myManager.removeUpdates(this);
				//myManager.re
				if(anyWay) singleGPSActivating=false;
			}
			serviceActive = false;
			lastLocation=null;
			lastGPSLocation=null;
			lastNETLocation=null;
			lastLocationTime=-1;
			lastGPSLocationTime=-1;
			lastNETLocationTime=-1;
		} catch(Exception le)	{
			showMyMsg("Ошибка остановки слушателя TLM!");
		}
	}
	
	public void checkCurrTarif()   {
		if(this.mainActiv.SOCKET_IN_SERVICE)	{
			Message msg = new Message();
	    	msg.obj = this.mainActiv;
	    	msg.arg1 = ConnectionActivity.CHECK_CURR_TARIF;
	    	Bundle bnd = new Bundle();
	    	bnd.putString("msg_lbl_text", "---");
	    	msg.setData(bnd);
	    	this.mainActiv.handle.sendMessage(msg);
		}	else	{
			Intent rintent = new Intent(GpsLocationDetector.FROM_SERVICE);
	        rintent.putExtra(GpsLocationDetector.TYPE, ConnectionActivity.SRV_CHECK_CURR_TARIF);
	        rintent.putExtra("msg_lbl_text", "---");
	        this.mainActiv.sendBCast(rintent);
		}
    }
	
	public void showMyMsg(String message)   {
    	Message msg = new Message();
    	msg.obj = this.mainActiv;
    	msg.arg1 = ConnectionActivity.SHOW_MY_MSG;
    	Bundle bnd = new Bundle();
    	bnd.putString("msg_lbl_text", message);
    	msg.setData(bnd);
    	this.mainActiv.handle.sendMessage(msg);
    }
	
	public void showStatus(String message)   {
		Message msg = new Message();
    	msg.obj = this.mainActiv;
    	msg.arg1 = ConnectionActivity.SHOW_STATUS;
    	Bundle bnd = new Bundle();
    	bnd.putString("msg_lbl_text", message);
    	msg.setData(bnd);
    	this.mainActiv.handle.sendMessage(msg);
    }
	
	public void showTMeter(float tdistance, int ltype)   {
		String sbdtxt="";
		if(mainActiv.START_BACK_DISTANCE>0)	{
			if((mainActiv.START_BACK_DISTANCE>tdistance)&&mainActiv.SLEEP_TIME_STDIST)
				{
					mainActiv.startMillis=System.currentTimeMillis();
					mainActiv.resetHiddenTime();
				}
			}
		if(this.mainActiv.START_BACK_DISTANCE>0)	{
			if(this.mainActiv.START_BACK_DISTANCE>tdistance)
				sbdtxt="("+Math.round((tdistance/
						this.mainActiv.START_BACK_DISTANCE)*100)+"%)";
			else
				sbdtxt="(100%)";
		}
		if(this.mainActiv.dpart_count>0)
			sbdtxt+="("+this.mainActiv.dpart_count+"+)";
		DecimalFormat df = new DecimalFormat("#.##");
		String message = sbdtxt+df.format(tdistance/1000.0)+"км";
		Message msg = new Message();
    	msg.obj = this.mainActiv;
    	msg.arg1 = ConnectionActivity.SHOW_TAXMETER;
    	Bundle bnd = new Bundle();
    	bnd.putString("tmeter_text", message);
    	bnd.putInt("ltype", ltype);
    	bnd.putDouble("tmeter_val", tdistance);
    	msg.setData(bnd);
    	this.mainActiv.handle.sendMessage(msg);
    }
	
	public void sendTCoord(double latitude, double longitude, boolean isMap)   {
		DecimalFormat df = new DecimalFormat(isMap?"#.######":"#.###");
		String str_lat = df.format(latitude);
		String str_lon = df.format(longitude);
		if(isMap) { 
			str_lat=str_lat.replace(",", ".");
			str_lon=str_lon.replace(",", ".");
		}
		Message msg = new Message();
		msg.obj = this.mainActiv;
		msg.arg1 = ConnectionActivity.SEND_GPS_COORD;
		Bundle bnd = new Bundle();
		bnd.putString("str_lat", str_lat);
		bnd.putString("str_lon", str_lon);
		msg.setData(bnd);
		this.mainActiv.handle.sendMessage(msg);

    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		try	{
		if(location!=null)	{
		
		if(serviceActive)	{
		long currentLocTime=System.currentTimeMillis();
		float sensorLocSpeed = location.getSpeed()*36/10;
		float calcGPSSpeed = 0;
		//showMyMsg("LOC:"+location.getLatitude()+":"+location.getLongitude());
		if(mainActiv.USE_BOTH_LOCATIONS)	{
		
			mainActiv.lastLocation=location;
			checkCurrTarif();
			
			if(location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
				
				if(lastGPSLocation!=null)	{
					//if(serviceActive)	{
						float distance = location.distanceTo(lastGPSLocation);
						if(lastGPSLocationTime>0)	{
							if(currentLocTime!=lastGPSLocationTime)
								calcGPSSpeed = distance*3600/
									(currentLocTime-lastGPSLocationTime);
							else
								calcGPSSpeed=0;
						}
						if(
							(((calcGPSSpeed>mainActiv.TMETER_MIN_SPEED)&&
							(calcGPSSpeed<mainActiv.TMETER_MAX_SPEED))||
							!mainActiv.USE_CALC_SPEED_DIST)	&&	
							(((sensorLocSpeed>mainActiv.TMETER_MIN_SPEED)&&
							(sensorLocSpeed<mainActiv.TMETER_MAX_SPEED))||
							!mainActiv.USE_SENS_SPEED_DIST)
								)
							summaryDist+=distance;
						showTMeter(summaryDist, LOCATION_SATELLITE);
					//}
				}
				lastGPSLocation = location;
				lastGPSLocationTime = currentLocTime;
				lastNETLocation = null;
				
			}
		
			if(location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
				
				if((lastNETLocation!=null)&&!gpsAviable)	{
					 //if(serviceActive)	{
						float distance = location.distanceTo(lastNETLocation);
						if(lastNETLocationTime>0)	{
							if(currentLocTime!=lastNETLocationTime)
								calcGPSSpeed = distance*3600/
									(currentLocTime-lastNETLocationTime);
							else
								calcGPSSpeed=0;
						}
						if(
								(((calcGPSSpeed>mainActiv.TMETER_MIN_SPEED)&&
								(calcGPSSpeed<mainActiv.TMETER_MAX_SPEED))||
								!mainActiv.USE_CALC_SPEED_DIST)	&&	
								(((sensorLocSpeed>mainActiv.TMETER_MIN_SPEED)&&
								(sensorLocSpeed<mainActiv.TMETER_MAX_SPEED))||
								!mainActiv.USE_SENS_SPEED_DIST)
									)
							summaryDist+=distance;
						
						showTMeter(summaryDist, LOCATION_NETWORK);
					//}
				}
				lastNETLocation = location;
				lastNETLocationTime = currentLocTime;
				
			}
		
		}	else	{
			
			mainActiv.lastLocation=location;
			checkCurrTarif();
			
			if(lastLocation!=null)	{
				//if(serviceActive)	{
					float distance = location.distanceTo(lastLocation);
					if(lastLocationTime>0)	{
						if(currentLocTime!=lastLocationTime)
							calcGPSSpeed = distance*3600/
								(currentLocTime-lastLocationTime);
						else
							calcGPSSpeed = 0;
					}
					if(
							(((calcGPSSpeed>mainActiv.TMETER_MIN_SPEED)&&
							(calcGPSSpeed<mainActiv.TMETER_MAX_SPEED))||
							!mainActiv.USE_CALC_SPEED_DIST)	&&	
							(((sensorLocSpeed>mainActiv.TMETER_MIN_SPEED)&&
							(sensorLocSpeed<mainActiv.TMETER_MAX_SPEED))||
							!mainActiv.USE_SENS_SPEED_DIST)
								)
						summaryDist+=distance;
					if(mainActiv.USE_NETWORK_LOCATION)	{
						showTMeter(summaryDist, LOCATION_NETWORK);
					}
					else	{
						showTMeter(summaryDist, LOCATION_SATELLITE);
					}
				//}
			}
		
			lastLocation = location;
			lastLocationTime = currentLocTime;
		
		}
		
			this.showStatus((satellites>0?"|C"+satellites:"|")+
					(satellitesInFix>=0&&satellites>0?"["+satellitesInFix+"]|":"|")+
					"Ск.выч.:"+(int)calcGPSSpeed+
					",сен.:"+(int)sensorLocSpeed);
		
		}	else	{
			lastLocation=null;
			lastGPSLocation=null;
			lastNETLocation=null;
			lastLocationTime=-1;
			lastGPSLocationTime=-1;
			lastNETLocationTime=-1;
		}
		
		if (singleGPSActivating)	{
			if(singleGPSForMapRoute)	{
				sendTCoord(location.getLatitude(), 
						location.getLongitude(), true);
			}	else
				sendTCoord(location.getLatitude(), 
					location.getLongitude(), false);
			singleGPSActivating=false;
			if(!serviceActive)	{
				removeLUpd(false);
			}
		}
		
		if(location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
			this.lastLocationIsGPS=true;
		}
		
		if(location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
			this.lastLocationIsNET=true;
		}
		
		}
		} catch(Exception le)	{
			showMyMsg("Ошибка обработчика данных местоположения! "+
					le.getMessage());
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Если пользователь запретил локацию или с его согласия система отключила
		if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
			
		}
		if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
			
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Если пользователь разрешил локацию или с его согласия система включила
		if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
			
		}
		if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
			
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
			switch(status){
		      case LocationProvider.AVAILABLE:
		           //выводим уведомления ит.д.
		    	  gpsAviable=true;
		    	  //showMyMsg("Спутники доступны!");
		    	  break;
		      case LocationProvider.OUT_OF_SERVICE:
		           //выводим уведомления ит.д.
		    	  gpsAviable=false;
		    	  //showMyMsg("Спутники недоступны!");
		    	  break;
		      case LocationProvider.TEMPORARILY_UNAVAILABLE:
		           //выводим уведомления ит.д.
		    	  gpsAviable=false;
		    	  //showMyMsg("Спутники временно недоступны!");
		    	  break;
		      default:
		           //действия
		      } 
		}
		if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
			switch(status){
		      case LocationProvider.AVAILABLE:
		           //выводим уведомления ит.д.
		    	  netLocAviable=true;
		    	  break;
		      case LocationProvider.OUT_OF_SERVICE:
		           //выводим уведомления ит.д.
		    	  netLocAviable=false;
		    	  //lastNETLocation=null;
		    	  break;
		      case LocationProvider.TEMPORARILY_UNAVAILABLE:
		           //выводим уведомления ит.д.
		    	  netLocAviable=false;
		    	  //lastNETLocation=null;
		    	  break;
		      default:
		           //действия
		      }
		}
	}

}
