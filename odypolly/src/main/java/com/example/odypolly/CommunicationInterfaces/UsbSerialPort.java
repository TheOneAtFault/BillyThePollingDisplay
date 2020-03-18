package com.example.odypolly.CommunicationInterfaces;

import android.hardware.usb.UsbDeviceConnection;

import java.io.Closeable;
import java.io.IOException;

public interface UsbSerialPort extends Closeable {

    public static final int DATABITS_5 = 5;

    public static final int DATABITS_6 = 6;

    public static final int DATABITS_7 = 7;

    public static final int DATABITS_8 = 8;

    public static final int FLOWCONTROL_NONE = 0;

    public static final int FLOWCONTROL_RTSCTS_IN = 1;

    public static final int FLOWCONTROL_RTSCTS_OUT = 2;

    public static final int FLOWCONTROL_XONXOFF_IN = 4;

    public static final int FLOWCONTROL_XONXOFF_OUT = 8;

    public static final int PARITY_NONE = 0;

    public static final int PARITY_ODD = 1;

    public static final int PARITY_EVEN = 2;

    public static final int PARITY_MARK = 3;

    public static final int PARITY_SPACE = 4;

    public static final int STOPBITS_1 = 1;

    public static final int STOPBITS_1_5 = 3;

    public static final int STOPBITS_2 = 2;

    public UsbSerialDriver getDriver();


    public int getPortNumber();


    public String getSerial();


    public void open(UsbDeviceConnection connection) throws IOException;


    public void close() throws IOException;


    public int read(final byte[] dest, final int timeout) throws IOException;


    public int write(final byte[] src, final int timeout) throws IOException;


    public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException;


    public boolean getCD() throws IOException;


    public boolean getCTS() throws IOException;


    public boolean getDSR() throws IOException;


    public boolean getDTR() throws IOException;


    public void setDTR(boolean value) throws IOException;


    public boolean getRI() throws IOException;


    public boolean getRTS() throws IOException;


    public void setRTS(boolean value) throws IOException;


    public boolean purgeHwBuffers(boolean purgeWriteBuffers, boolean purgeReadBuffers) throws IOException;

}
