package com.psdevelop.tdandrapp;

import android.os.Bundle;
import android.os.Message;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetReq extends Thread {
	String getUri;
	GpsLocationDetector gld;

	public HttpGetReq(GpsLocationDetector parent, String getU) {
		// TODO Auto-generated constructor stub
		this.getUri = getU;
		this.gld = parent;
	}

	public void showMyMsg(String message)   {
		if (this.gld != null)
			this.gld.showMyMsg(message);
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
			//showMyMsg("Ошибка обмена! Сервис B2.0: " + e.getMessage());
			return "";
		}
	}

	public void run() {
		try {
		     String line = this.runGetRequest(this.getUri);
		} catch (Exception e) {
			 //showMyMsg("Ошибка обмена! Сервис B2: " + e.getMessage());
		}
	}

}
