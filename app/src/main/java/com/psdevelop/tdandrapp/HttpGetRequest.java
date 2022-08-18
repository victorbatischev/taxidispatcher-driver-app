package com.psdevelop.tdandrapp;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends Thread {
	
	GpsScanner gpsScan;
	String getUri;

	public HttpGetRequest(GpsScanner gpsScn, String getU) {
		// TODO Auto-generated constructor stub
		this.gpsScan = gpsScn;
		this.getUri = getU;
	}
	
	public void showMyMsg(String message)   {
    	if (this.gpsScan!=null)
    		this.gpsScan.showMyMsg(message);
    }

	public String runGetRequest(String addres) {
		try {
			String url = addres;

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public void run() {
		try {
			this.runGetRequest(this.getUri);
		} catch (Exception e) {
			 showMyMsg("Ошибка во время отправки! Сервис B."+e.getMessage());
		}
	}

}
