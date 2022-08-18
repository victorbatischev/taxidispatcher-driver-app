package com.psdevelop.tdandrapp;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Vector;

/**
 *
 * @author Администратор
 */

public class VectorIstructItem extends Object {
        public String orderId = "";
        public String orderData = "";
        public String msgText = "";
        public int istructItemType;
        public boolean hasASK = false;
        public boolean onDeleted = false;
        public String JSONInstr = "";
        public boolean auto_accepted = false;
        public boolean manual_accepted = false;
        public boolean has_acknowlegment = false;
        public boolean defWaiting = false;
        public int ordTariffId = -1;
        public String ordOptComb = "";
        public int tplan_id = -1;
        public int sector_id = -1;
        public double prevSumm = 0;
        public double prevDistance = 0;
        public double bonusUse = 0;
        public String cargoDesc = "";
        public String endAdres = "";
        public String clientName = "";
        public double ratingBonus = 0;
        //@var boolean Признак того, что водитель прибыл на точку
        public boolean driverOnPlace = false;
        public boolean isForAll = false;
        public int companyId = -1;
        public double clientLat = 0;
        public double clientLon = 0;
        public double destLat = 0;
        public double destLon = 0;

        public VectorIstructItem(int itemType)  {
            this.msgText = "";
            this.istructItemType = itemType;
            this.hasASK = false;
            this.onDeleted = false;
            this.auto_accepted = false;
            this.manual_accepted = false;
            this.has_acknowlegment = false;
        }
        
        public VectorIstructItem(String oid, String odata)  {
            this.msgText = "";
            this.istructItemType = 0;
            this.hasASK = false;
            this.onDeleted = false;
            this.auto_accepted = false;
            this.manual_accepted = false;
            this.has_acknowlegment = false;
            this.orderId = oid;
            this.orderData = odata;
        }
        
        public VectorIstructItem(String JSON, int itemType)  {
            this.msgText = "";
            this.istructItemType = itemType;
            this.hasASK = false;
            this.onDeleted = false;
            this.JSONInstr = JSON;
            this.auto_accepted = false;
            this.manual_accepted = false;
        }
    }

