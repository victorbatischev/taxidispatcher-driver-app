package com.psdevelop.tdandrapp;

import java.util.Vector;

public class OrderTarif {
	
	int id=-1;
	String tarif_name="";
	double tmeter_tariff=0;
	double time_tariff=0;
	int tplan_id=-1;
	String short_name="";
	int part_dist=0;
	int start_dist=0;
	double part_dist_tariff=0;
	double stop_tariff=0;
	Vector<TarifAreaLine> areaLines;
	int outherAreaTarifId = -1;
	int outherAreaTPlanId = -1;
	int missEveryKmValue = 0;
	
	public OrderTarif(int oid, String name, double tmetert, double timet) {
		// TODO Auto-generated constructor stub
		this.id = oid;
		this.tarif_name = name;
		this.tmeter_tariff = tmetert;
		this.time_tariff = timet;
		areaLines = new Vector<TarifAreaLine>();
	}

	public boolean inTarifArea(double[] place, boolean to_other_in, int areaDetectMode)	{
		switch (areaDetectMode) {
			case 0:
				return this.inTarifAreaOld(place, to_other_in);
			case 1:
				return this.isPointInsidePolygon(place[1], place[0], to_other_in);
			default:
				return this.isPointInsidePolygon(place[1], place[0], to_other_in);
				//return false;
		}
	}
	
	public boolean inTarifAreaOld(double[] place, boolean to_other_in)	{
		if(areaLines.size()>2)	{
			double[][] P = new double[areaLines.size()][2];
			for(int i=0;i<areaLines.size();i++)	{
				P[i][0] = areaLines.elementAt(i).lat;
				P[i][1] = areaLines.elementAt(i).lon;
			}
			return pointloc(P, place);
		}

		return (!to_other_in);
	}
	
	public double rotate(double[] A, double[] B, double[] C)	{
		return (B[0]-A[0])*(C[1]-B[1])-(B[1]-A[1])*(C[0]-B[0]);
	}
	
	public boolean intersect(double[] A, double[] B, double[] C, double[] D)	{
		return (rotate(A,B,C)*rotate(A,B,D)<=0) && (rotate(C,D,A)*rotate(C,D,B)<0);
	}
	
	public boolean pointloc(double[][] P, double[] A)	{
		int n = P.length;

		if (rotate(P[0],P[1],A)<0 || rotate(P[0],P[n-1],A)>0) {
			return false;
		}

		int p = 1;
		int r = n-1;
		int q;

		while (r-p>1) {
			q = (p+r)/2;
			if (rotate(P[0],P[q],A)<0)	
				{ r = q; }
			else 
				{ p = q; }
		}

		return !intersect(P[0],A,P[p],P[r]);  
	}
	
	public boolean isPointInsidePolygon (double xd, double yd, boolean to_other_in) {
		int i1, i2, n, pcount;
		long S, S1, S2, S3, x, y;
		boolean flag = false;

		x = Math.round(xd * 1000);
		y = Math.round(yd * 1000);

		if(areaLines.size()<=2)	{
			return !to_other_in;
		}

		pcount = areaLines.size();
		long[][] p = new long[pcount][2];

		for (int i = 0; i < pcount; i++)	{
			p[i][1] = Math.round(areaLines.elementAt(i).lat * 1000);
			p[i][0] = Math.round(areaLines.elementAt(i).lon * 1000);
		}

		for (n = 0; n < pcount; n++) {
			
			flag = false;
			i1 = n < (pcount - 1) ? (n + 1) : 0;
			
			while (!flag) {
				i2 = i1 + 1;
				
				if (i2 >= pcount) {
					i2 = 0;
				}
				
				if (i2 == (n < (pcount - 1) ? (n + 1) : 0)) {
					break;
				}

				S = Math.abs( p[i1][0] * (p[i2][1] - p[n][1]) +
					p[i2][0] * (p[n][1] - p[i1][1]) +
					p[n][0]  * (p[i1][1] - p[i2][1]) );
				S1 = Math.abs( p[i1][0] * (p[i2][1] - y) +
					p[i2][0] * (y       - p[i1][1]) +
					x * (p[i1][1] - p[i2][1]) );
				S2 = Math.abs( p[n][0] * (p[i2][1] - y) +
					p[i2][0] * (y       - p[n][1]) +
					x * (p[n][1] - p[i2][1]) );
				S3 = Math.abs( p[i1][0] * (p[n][1] - y) +
					p[n][0] * (y       - p[i1][1]) +
					x * (p[i1][1] - p[n][1]) );
		
				if (S == S1 + S2 + S3) {
					flag = true;
					break;
				}
		
				i1 = i1 + 1;
				if (i1 >= pcount) {
					i1 = 0;
				}
			}
		
			if (!flag) {
				break;
			}
		}
	  
		return flag;
	}


	public boolean inPoly(double x, double y) {
		int pcount = areaLines.size();
		double[][] p = new double[pcount][2];

		for (int i = 0; i < pcount; i++)	{
			p[i][1] = areaLines.elementAt(i).lat;
			p[i][0] = areaLines.elementAt(i).lon;
		}
		int j = pcount - 1;
		boolean c = false;
		for (int i = 0; i < pcount;i++){
			if ((((p[i][1] <= y) && (y < p[j][1])) || ((p[j][1] <= y) && (y < p[i][1]))) &&
					(x > (p[j][0] - p[i][0]) * (y - p[i][1]) / (p[j][1] - p[i][1]) + p[i][0])) {
				c = !c;
			}
			j = i;
		}
		return c;
	}

}
