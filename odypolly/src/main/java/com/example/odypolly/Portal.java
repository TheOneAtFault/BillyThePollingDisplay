package com.example.odypolly;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.example.odypolly.Service.UsbService;

import java.util.Set;

public class Portal extends Activity {

    Context _context;
    String textToDisplay;
    int deviceToConnectTo;
    private UsbService usbService;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            try {
                Thread.sleep(300);
                clear();
                Thread.sleep(300);
                writeToDisplay();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private void writeToDisplay() {
        usbService.write(textToDisplay.getBytes());
    }

    private void clear() {
        usbService.write(new byte[]{0x0C});
    }

    public Portal(Context context, String data, int device){
        this._context = context;
        this.textToDisplay = data;
        this.deviceToConnectTo = device;
    }

    public void toPollingDisplay(){
        App.VID = deviceToConnectTo;
        startService(UsbService.class, usbConnection, null);
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(_context, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            _context.startService(startService);
        }
        Intent bindingIntent = new Intent(_context, service);
        _context.bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

}
