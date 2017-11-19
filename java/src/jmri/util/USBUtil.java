package jmri.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbPipe;
import javax.usb.UsbPort;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
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
     * @param idVendor  the vendor id to match (or zero to always match)
     * @param idProduct the product id to match (or zero to always match)
     * @return a list of matching UsbDevices
     */
    public static List<UsbDevice> getMatchingDevices(short idVendor, short idProduct) {
        List<UsbDevice> result = new ArrayList<>();
        recursivelyCollectUSBDevices(null, idVendor, idProduct, result);
        return result;
    }

    /**
     * get matching USB device
     *
     * @param idVendor   the vendor id to match (or zero to always match)
     * @param idProduct  the product id to match (or zero to always match)
     * @param idLocation the location id to match (never zero)
     * @return a list of matching UsbDevices
     */
    public static UsbDevice getMatchingDevice(short idVendor, short idProduct, String idLocation) {
        UsbDevice result = null;    // assume failure (pessimist!)

        if ((idLocation != null) && !idLocation.isEmpty()) {
            long longLocationID = Long.decode(idLocation).longValue();
            if (longLocationID > 0) {
                List<UsbDevice> usbDevices = new ArrayList<>();
                recursivelyCollectUSBDevices(null, idVendor, idProduct, usbDevices);
                for (UsbDevice usbDevice : usbDevices) {
                    String locationID = getLocationID(usbDevice);
                    if (locationID.equals(idLocation)) {
                        result = usbDevice;
                        break;
                    }
                }
            }
        }
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
        } catch (UsbException
                | UnsupportedEncodingException
                | UsbDisconnectedException ex) {
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
    @Nonnull
    private static List<UsbDevice> recursivelyCollectUSBDevices(
            @Nullable UsbHub usbHub, short idVendor,
            short idProduct, @Nonnull List<UsbDevice> devices) {
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

    /**
     * read message (synchronious)
     *
     * @param iface    the interface
     * @param endPoint the end point
     */
    public static void readMessage(@Nonnull UsbInterface iface, int endPoint) {

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                @Override
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();
            pipe.open();

            byte[] data = new byte[8];
            int received = pipe.syncSubmit(data);
            System.out.println(received + " bytes received");

            pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException
                    | UsbDisconnectedException
                    | UsbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * read message asynchronious
     *
     * @param iface    the interface
     * @param endPoint the end point
     */
    public static void readMessageAsynch(@Nonnull UsbInterface iface, int endPoint) {

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                @Override
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();

            pipe.open();

            pipe.addUsbPipeListener(new UsbPipeListener() {
                @Override
                public void errorEventOccurred(UsbPipeErrorEvent event) {
                    UsbException error = event.getUsbException();
                    error.printStackTrace();
                }

                @Override
                public void dataEventOccurred(UsbPipeDataEvent event) {
                    byte[] data = event.getData();

                    System.out.println(data + " bytes received");
                }
            });
//			pipe.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException
                    | UsbDisconnectedException
                    | UsbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get usb device interface
     *
     * @param device the usb device
     * @param index  the usb interface index
     * @return the usb interface
     */
    public static UsbInterface getDeviceInterface(@Nonnull UsbDevice device, int index) {
        UsbInterface result = null;  // assume failure (pessimist!)
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        if (configuration != null) {
            result = (UsbInterface) configuration.getUsbInterfaces().get(index); // there can be more 1,2,3..
        }
        return result;
    }

    /**
     * send bulk message
     *
     * @param iface   the interface
     * @param message the message
     * @param index   the index
     */
    public static void sendBulkMessage(@Nonnull UsbInterface iface, @Nonnull String message, int index) {

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                @Override
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(index);
            pipe = endpoint.getUsbPipe();
            pipe.open();

            byte[] initEP = new byte[]{0x1b, '@'};
            byte[] cutP = new byte[]{0x1d, 'V', 1};

            String str = "nnnnnnnnn";

            pipe.syncSubmit(initEP);
            int sent = pipe.syncSubmit(message.getBytes());
            pipe.syncSubmit(str.getBytes());
            pipe.syncSubmit(cutP);

            System.out.println(sent + " bytes sent");
            pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException
                    | UsbDisconnectedException
                    | UsbException e) {
                e.printStackTrace();
            }
        }
    }
}   // class USBUtil
