package com.example.altodypolly;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.altodypolly.Internals.driver.UsbSerialDriver;
import com.example.altodypolly.Internals.driver.UsbSerialPort;
import com.example.altodypolly.Internals.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.List;

public class PortalAlt {
    private final String TAG = "AltPolly";

    private final String _data;
    private final int _terminalId;
    private Context _context;
    private UsbManager mUsbManager;
    private UsbSerialPort mSerialPort;
    private ListView mListView;
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;

    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    public PortalAlt(Context context, String data, int terminalId){
        _context = context;
        _data = data;
        _terminalId = terminalId;
    }

    public void main(){
        mUsbManager = (UsbManager) _context.getSystemService(Context.USB_SERVICE);
        refreshDeviceList();
    }

    private void refreshDeviceList() {

        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                Log.d(TAG, "Refreshing device list ...");
                SystemClock.sleep(1000);

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.d(TAG, String.format("+ %s: %s port%s",
                            driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);
                }

                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);
                Connect();
                Log.d(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }

        }.execute((Void) null);
    }

    private void Connect() {
        for (UsbSerialPort port: mEntries) {
            UsbDevice device = port.getDriver().getDevice();
            if(device.getVendorId() == _terminalId){
                if (!mUsbManager.hasPermission(device)) {
                    PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(_context, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                    mUsbManager.requestPermission(device, usbPermissionIntent);
                } else {

                }
            }
        }
    }
}
