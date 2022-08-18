package com.psdevelop.tdandrapp;

public class NearConnection {

	String name="";
	double lat=0;
	double lon=0;
	String ip="";

	public NearConnection(String cname, double clat, double clon, String cip) {
		this.name = cname;
		this.lat = clat;
		this.lon = clon;
		this.ip = cip;
	}

}
