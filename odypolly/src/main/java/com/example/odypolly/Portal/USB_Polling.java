package com.example.odypolly.Portal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.example.odypolly.Service.UsbService;

import java.lang.ref.WeakReference;
import java.util.Set;

public class USB_Polling extends Activity {
    private String write = "";
    private UsbService usbService;
    private TextView display;//temp
    private EditText editText;//temp
    private MyHandler mHandler;
    private Context _context;
    private String _data;
    private int _deviceId;
    private static USB_Polling instance;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            //usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    public static void init(Context context, String Data, String DeviceId) {
        USB_Polling request = new USB_Polling();
        request._context = context;
        request.startUp();
    }

    private void startUp() {
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(new byte[]{0x0C});
            usbService.write("bullshit".getBytes());
        }
    }

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
            startService(startService);
        }
        Intent bindingIntent = new Intent(_context, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    //Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    Log.i("serialComs", "UsbService.ACTION_USB_PERMISSION_GRANTED");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    //Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    Log.i("serialComs", "UsbService.ACTION_USB_PERMISSION_NOT_GRANTED");
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    //Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    Log.i("serialComs", "UsbService.ACTION_NO_USB");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    //Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    Log.i("serialComs", "UsbService.ACTION_USB_DISCONNECTED");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    //Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    Log.i("serialComs", "UsbService.ACTION_USB_NOT_SUPPORTED");
                    break;
            }
        }
    };

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Context activity) {
            mActivity = new WeakReference<Activity>((Activity) activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    //Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    //Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
