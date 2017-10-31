package jmri.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbPort;
import org.apache.commons.lang3.StringUtils;

/**
 * useful usb utilities
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public final class USBUtil {

    /**
     * get all USB devices
     *
     * @return a list of all UsbDevice's
     */
    public static List<UsbDevice> getAllDevices() {
        return getMatchingDevices((short) 0, (short) 0);
    }

    /**
     * get matching USB devices
     *
     * @param idVendor  the vendor id to match
     * @param idProduct the product id to match
     * @return a list of matching UsbDevices
     */
    public static List<UsbDevice> getMatchingDevices(short idVendor, short idProduct) {
        List<UsbDevice> result = new ArrayList<>();
        recursivelyCollectUSBDevices(null, idVendor, idProduct, result);
        return result;
    }

    /**
     * get a USB device's full product (manufacturer + product) name
     *
     * @param usbDevice the usb device you want to full product name of
     * @return the full product name (String)
     */
    public static String getFullProductName(@Nonnull UsbDevice usbDevice) {
        String result = "";
        try {
            String manufacturer = usbDevice.getManufacturerString();
            manufacturer = (manufacturer == null) ? "" : manufacturer;
            String product = usbDevice.getProductString();
            product = (product == null) ? "" : product;
            if (product.startsWith(manufacturer)) {
                result = product;
            } else {
                result = manufacturer + " " + product;
            }
        } catch (UsbException
                | UnsupportedEncodingException
                | UsbDisconnectedException ex) {
            // Nothing to see here... move along...
        }
        return result;
    }

    /**
     * get a USB device's serial number (String)
     *
     * @param usbDevice the usb device who's serial number you want
     * @return serial number (String)
     */
    public static String getSerialNumber(@Nonnull UsbDevice usbDevice) {
        String result = "";
        try {
            result = usbDevice.getSerialNumberString();
            result = (result == null) ? "" : result;
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            // Nothing to see here... move along...
        }
        return result;
    }

    /**
     * get a USB device's location id (String)
     *
     * @param usbDevice the usb device who's location id you want
     * @return location id (String)
     */
    public static String getLocationID(@Nonnull UsbDevice usbDevice) {
        String result = "";
        while (usbDevice != null) {
            UsbPort usbPort = usbDevice.getParentUsbPort();
            if (usbPort == null) {
                break;
            }
            result = "" + usbPort.getPortNumber() + result;
            usbDevice = usbPort.getUsbHub();
        }
        result = "0x" + StringUtils.rightPad(result, 8, "0");
        return result;
    }

    /*
     * recursive routine to collect USB devices
     * @param usbHub the hub whos devices we want to collect (null for root)
     * @param idVendor the vendor id to match against
     * @param idProduct the product id to match against
     * @param devices the list of USB devices to add matching devices to
     */
    private static List<UsbDevice> recursivelyCollectUSBDevices(
            @Nullable UsbHub usbHub, short idVendor,
            short idProduct, List<UsbDevice> devices) {
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
        return devices;
    }   // recursivelyCollectUSBDevices
}   // class USBUtil
