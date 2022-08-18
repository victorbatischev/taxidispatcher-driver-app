package com.psdevelop.tdandrapp;

import android.os.Message;

public class CheckTimer extends Thread {
    private GpsLocationDetector ownerSrv;
    private long counter = 0;
    public int sendTaxometerParamsCounter = 0;

    public CheckTimer(GpsLocationDetector own) {
        this.ownerSrv = own;
        counter = 0;
        this.start();
    }

    public void checkConnect() {
        Message msg = new Message();
        msg.obj = this.ownerSrv;
        msg.arg1 = GpsLocationDetector.CHECK_SOCKET_CONNECT;
        this.ownerSrv.handle.sendMessage(msg);
    }

    public void run() {
        while (true) {
            if (counter > 2000000)
                counter = 0;
            counter++;
            try {
                sleep(1000);
                //if(counter%30==0)
                //    checkStatus();
                if (sendTaxometerParamsCounter < 30) {
                    sendTaxometerParamsCounter++;
                }
                if (counter % 3 == 0) {
                    checkConnect();
                }
                /*if (this.ownerSrv.inactiveTimeout < 350)
                    this.ownerSrv.inactiveTimeout++;
                else {
                    if (!this.ownerSrv.inactiveTimeoutBlock) {
                        this.ownerSrv.inactiveTimeoutBlock = true;
                        disconnectSocketIO();
                    }
                } */
            } catch (Exception e) {
                //showMyMsg(
                //        "\nОшибка таймера!" + e.getMessage());
            }

        }

    }
}
