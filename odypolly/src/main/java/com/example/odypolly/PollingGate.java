package com.example.odypolly;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.example.odypolly.driver.UsbSerialDriver;
import com.example.odypolly.driver.UsbSerialPort;
import com.example.odypolly.driver.UsbSerialProber;

import java.io.IOException;


public class PollingGate extends Activity implements SerialListener {
    @Override
    public void onSerialConnect() {

    }

    @Override
    public void onSerialConnectError(Exception e) {

    }

    @Override
    public void onSerialRead(byte[] data) {

    }

    @Override
    public void onSerialIoError(Exception e) {

    }

    private enum Connected { False, Pending, True }

    static PollingGate pollingGate;
    Context context;
    int deviceVID;
    UsbDevice device;
    UsbDeviceConnection usbConnection;
    UsbSerialDriver driver;
    private UsbSerialPort usbSerialPort;
    private int deviceId, portNum, baudRate;

    //connecting
    private Connected connected = Connected.False;
    private SerialSocket socket;
    private SerialService service;

    //UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

    public static void enter(Context context, int deviceId) {
        pollingGate = new PollingGate();
        pollingGate.context = context;
        pollingGate.deviceVID = deviceId;

        pollingGate.GetDevice();

    }


    public void GetDevice() {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        for (UsbDevice v : usbManager.getDeviceList().values()) {
            if (v.getVendorId() == deviceVID)
                device = v;
            pollingGate.deviceId = v.getDeviceId() ;
            //pollingGate.portNum = v. getArguments().getInt("port");
            pollingGate.baudRate = 9600;
        }
        if (device != null){
            GetDeviceDriver(usbManager);
            Log.i("Polling", "GetDevice: Getting device driver.");
        }else {
            Log.i("Polling", "GetDevice: Device not found.");
        }
    }

    public void GetDeviceDriver(UsbManager usbManager) {
        driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        //connect with permission
        if(driver == null) {
            driver = UsbSerialProber.getDefaultProber().probeDevice(device);
            //driver = CustomProber.getCustomProber().probeDevice(device);
        }

        usbSerialPort = driver.getPorts().get(portNum);
        usbConnection = usbManager.openDevice(driver.getDevice());

        //permission
        if(usbConnection == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            String value = "]( .)>( .)[";
            port.open(usbConnection);
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            //new byte[]{0x0C}
            byte[] destination = new byte[new byte[]{0x0C}.length + value.getBytes().length];
            System.arraycopy(new byte[]{0x0C}, 0, destination, 0, new byte[]{0x0C}.length);
            System.arraycopy(value.getBytes(), 0, destination, new byte[]{0x0C}.length, value.getBytes().length);

            port.write(destination, 300);
            port.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
