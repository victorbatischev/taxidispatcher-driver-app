package com.psdevelop.tdandrapp;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Vector;

/**
 *
 * @author 
 */
public class VectorSectorItem extends Object {
    public String sectorId = "";
    public String sectorName = "";
    public boolean forAll = false;
    public int companyId = -1;
    public int drCount = 0;
    public Vector<TarifAreaLine> areaLines;
        
    public VectorSectorItem(String Id, String Name) {
        this.sectorId = Id;
        this.sectorName = Name;
        this.areaLines = new Vector<TarifAreaLine>();
    }

    public boolean isPointInsideSector(double xd, double yd) {
        int i1, i2, n, pcount;
        long S, S1, S2, S3, x, y;
        boolean flag = false;

        if(areaLines.size()<=2)	{
            return false;
        }

        x = Math.round(xd * 1000);
        y = Math.round(yd * 1000);

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
        
}
