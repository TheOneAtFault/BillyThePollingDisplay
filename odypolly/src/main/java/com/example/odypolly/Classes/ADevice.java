package com.example.odypolly.Classes;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;

import com.example.odypolly.CommunicationInterfaces.UsbSerialDriver;
import com.example.odypolly.CommunicationInterfaces.UsbSerialPort;
import com.example.odypolly.Helpers.UsbSerialProber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


public abstract class ADevice {
    protected UsbSerialPort Port;

    ////////////////////////////////Constructors Begin////////////////////////////////////////
    protected ADevice(Context context) {
        this.context = context;
        musbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }
    protected ADevice(Context context, int deviceId) {
        this.context = context;
        DeviceId = deviceId;
        musbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }
    ////////////////////////////////Constructors End////////////////////////////////////////

    ////////////////////////////////Event Listener Begin////////////////////////////////////////
    protected ReentrantLock lock = new ReentrantLock();
    private Runnable _UpdateAction = new Runnable() {
        @Override
        public void run() {
            List<UsbSerialPort> result = GetPorts();
            for (int i = 0; i < result.size(); i++) {
                UsbSerialPort port = result.get(i);
                UsbDevice _device = port.getDriver().getDevice();

                if (VID ==_device.getVendorId()) {
                    Port = port;
                    continue;
                }
            }
            UpdateAction();
        }
    };
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    protected abstract void UpdateAction();
    ////////////////////////////////Event Listener End////////////////////////////////////////

    ////////////////////////////////Observer Pattern Begin////////////////////////////////////////
    public List<ADevice> Observers = new ArrayList<>();
    protected void Notify(byte[] data) {
        Value = data;

        if (!new String(Value).equals(new String(PreValue))) {
            PreValue = Value;
            for (ADevice device : Observers) {

                device.Update(data);
            }
        }
    }
    protected void Update(final byte[] result) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(Sender.isEmpty()){
                    SystemClock.sleep(100);
                }
                for (UsbEndpoint ep:Sender ) {
                    usbDeviceConnection.bulkTransfer(ep, new byte[]{0x0C}, 1, 0);
                    SystemClock.sleep(10);
                    usbDeviceConnection.bulkTransfer(ep, result, result.length, 0);
                    SystemClock.sleep(100);
                }
            }
        }).start();
    }
    ////////////////////////////////Observer Pattern End////////////////////////////////////////

    ////////////////////////////////Device Registration Begin////////////////////////////////////////
    protected boolean LoadCompleted;
    protected UsbManager musbManager;
    private  final String INTENT_ACTION_GRANT_USB = getClass().getName() + ".GRANT_USB";
    public boolean deviceRegistered;
    protected int VID;
    protected int PID;
    protected List<UsbEndpoint> Receiver = new ArrayList<>();
    private List<UsbEndpoint> Sender= new ArrayList<>();
    protected final Context context;
    protected UsbDeviceConnection usbDeviceConnection;
    protected UsbDevice ___device;
    private int DeviceId;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ReceiveAction(intent);
        }
    };
    private void ReceiveAction(Intent intent) {
        if (INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
            synchronized (this) {
                ___device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Finalize(___device);
                }
            }
        }

        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
            // Device removed
            synchronized (this) {
                // ... Check to see if usbDevice is yours and cleanup ...
            }
        }
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            // Device attached
            synchronized (this) {
                // Qualify the new device to suit your needs and request permission

            }
        }
    }
    private void Authorize(UsbDevice device) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_GRANT_USB);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        context.registerReceiver(broadcastReceiver, filter);
        if (device != null) {
            if (!musbManager.hasPermission(device))
            {
                Intent intent = new Intent(INTENT_ACTION_GRANT_USB);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                ___device = device;
                musbManager.requestPermission(device, pendingIntent);
            } else {
                Finalize(device);
            }
        }
    }
    private void Finalize(UsbDevice device) {


        UsbInterface anInterface = device.getInterface(0);
        for (int i = 0; i < anInterface.getEndpointCount(); i++) {
            UsbEndpoint end = anInterface.getEndpoint(i);
            if (end.getDirection() == UsbConstants.USB_DIR_IN) {
                Receiver.add(end);
            }
        }

        for (int i = 0; i < anInterface.getEndpointCount(); i++) {
            UsbEndpoint end = anInterface.getEndpoint(i);
            if (end.getDirection() == UsbConstants.USB_DIR_OUT) {
                Sender.add(end);
            }
        }
        usbDeviceConnection = musbManager.openDevice(device);
        usbDeviceConnection.claimInterface(anInterface, true);

        deviceRegistered = true;
        ___device = device;
       // mExecutor.submit(_UpdateAction);
        new Thread(_UpdateAction).start();
    }
    public List<UsbSerialPort> GetPorts() {
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(musbManager);
        final List<UsbSerialPort> result = new ArrayList<>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            result.addAll(ports);
        }
        return result;
    }
    private static List<UsbSerialPort> OpenPorts = new ArrayList<>();
    protected void GetDevice() {
        HashMap<String, UsbDevice> deviceList = musbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice _device = deviceIterator.next();
                if (DeviceId!= 0) {
                    int deviceId = _device.getVendorId();
                    if (deviceId==VID) {
                        Authorize(_device);
                        return;
                    }
                    continue;
                }

//                if (_device.getVendorId() == VID && _device.getProductId() == PID) {
//                    Authorize(_device);
//                    return;
//                }

            }
        }


    }
    ////////////////////////////////Device Registration End////////////////////////////////////////

    ////////////////////////////////Device Identification Begin////////////////////////////////////////
    protected String Name = "";
    public String toString() {
        if (___device == null) {
            return "No Device";
        }
        return Name;
    }
    ////////////////////////////////Device Identification End////////////////////////////////////////

    ////////////////////////////////Device Values Begin////////////////////////////////////////
    protected byte[] PreValue = new byte[0];
    protected byte[] Value = new byte[0];
    public byte[] GetResult() {
        return Value;
    }
    ////////////////////////////////Device Values End////////////////////////////////////////

}
