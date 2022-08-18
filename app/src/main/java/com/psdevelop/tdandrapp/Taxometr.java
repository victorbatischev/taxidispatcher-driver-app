package com.psdevelop.tdandrapp;

import android.os.Bundle;
import android.os.Message;

public class Taxometr extends Object {
	
	public final static int LOCATION_NONE = 1;
	public final static int LOCATION_SATELLITE = 2;
	public final static int LOCATION_NETWORK = 3;

	private ConnectionActivity mainActiv;
	static boolean serviceActive=false;
	static boolean singleGPSActivating=false;

	public Taxometr(ConnectionActivity mActiv) {
		this.mainActiv=mActiv;
	}

}
