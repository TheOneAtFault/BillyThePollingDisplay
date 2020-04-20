package com.example.odypolly.Internals.deviceids;


import static com.example.odypolly.Internals.deviceids.Helpers.createDevice;
import static com.example.odypolly.Internals.deviceids.Helpers.createTable;

public class CH34xIds
{
    private CH34xIds()
    {

    }

    private static final long[] ch34xDevices = createTable(
            createDevice(0x4348, 0x5523),
            createDevice(0x1a86, 0x7523),
            createDevice(0x1a86, 0x5523),
            createDevice(0x1a86, 0x0445)
    );

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        return Helpers.exists(ch34xDevices, vendorId, productId);
    }
}
