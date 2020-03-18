package com.example.odypolly;

import android.content.Context;
import android.os.SystemClock;


import com.example.odypolly.Classes.ADevice;

import java.io.IOException;
import java.util.Calendar;

public class DPole extends ADevice {
    public static DPole MySelf;

    public DPole(Context context, int deviceId) {
        super(context, deviceId);
        MySelf = this;
        VID = deviceId;
        PID = 8963;
        Name = "USB-Serial Controller";
        GetDevice();
    }

    @Override
    protected void UpdateAction() {
        if (Port == null) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            Port.open(musbManager.openDevice(___device));

            Port.setParameters(9600, 8, Port.STOPBITS_1, Port.PARITY_NONE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Port.write(new byte[]{0x0C}, 0);
                SystemClock.sleep(1000);
                Port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
        Update(Calendar.getInstance().getTime().toString().getBytes());
    }

    public void SetResult(byte[] s) {
        Update(s);
    }
}
