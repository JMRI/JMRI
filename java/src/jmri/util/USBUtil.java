package jmri.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbPort;

/**
 *
 * useful usb methods
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public final class USBUtil {

    /**
     * @param a the first float
     * @param b the second float
     * @return true if a is equal to b
     */
    public static List<UsbDevice> getAllDevices() {
        return getMatchingDevices((short) 0, (short) 0);
    }

    public static List<UsbDevice> getMatchingDevices(short idVendor, short idProduct) {
        List<UsbDevice> result = new ArrayList<>();
        recursivelyCollectUSBDevices(null, idVendor, idProduct, result);
        return result;
    }

    public static String getFullProductName(UsbDevice usbDevice) {
        String result = "";
        try {
            String manufacturer = usbDevice.getManufacturerString();
            manufacturer = (manufacturer == null) ? "" : manufacturer;
            String product = usbDevice.getProductString();
            product = (product == null) ? "" : product;
            if (product.startsWith(manufacturer)) {
                result = product;
            } else {
                result = manufacturer + " - " + product;
            }
        } catch (UsbException
                | UnsupportedEncodingException
                | UsbDisconnectedException ex) {
            // Nothing to see here... move along...
        }
        return result;
    }

    public static String getSerialNumber(UsbDevice usbDevice) {
        String result = "";
        try {
            result = usbDevice.getSerialNumberString();
            result = (result == null) ? "" : result;
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            // Nothing to see here... move along...
        }
        return result;
    }

    public static String getLocation(UsbDevice usbDevice) {
        String result = "";
        String delimiter = "";
        while (usbDevice != null) {
            UsbPort usbPort = usbDevice.getParentUsbPort();
            if (usbPort == null) {
                break;
            }
            result = "" + usbPort.getPortNumber() + delimiter + result;
            delimiter = ".";
            usbDevice = usbPort.getUsbHub();
        }
        return result;
    }

    private static void recursivelyCollectUSBDevices(UsbHub usbHub,
            short idVendor, short idProduct, List<UsbDevice> devices) {
        if (usbHub == null) {
            try {
                usbHub = UsbHostManager.getUsbServices().getRootUsbHub();
            } catch (UsbException | SecurityException ex) {
                //log.error("Exception: " + ex);
            }
        }
        if (usbHub != null) {
            List<UsbDevice> usbDevices = usbHub.getAttachedUsbDevices();
            for (UsbDevice usbDevice : usbDevices) {
                if (usbDevice instanceof UsbHub) {
                    UsbHub childUsbHub = (UsbHub) usbDevice;
                    recursivelyCollectUSBDevices(childUsbHub, idVendor, idProduct, devices);
                } else {
                    UsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
                    if ((idVendor == 0) || (idVendor == usbDeviceDescriptor.idVendor())) {
                        if ((idProduct == 0) || (idProduct == usbDeviceDescriptor.idProduct())) {
                            devices.add(usbDevice);
                        }
                    }
                }
            }
        }
    }
}
