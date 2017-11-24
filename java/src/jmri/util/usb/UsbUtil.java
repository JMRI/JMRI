package jmri.util.usb;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
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
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbPort;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * USB utilities.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public final class UsbUtil {

    /**
     * Prevent construction, since this is a stateless utility class
     */
    private UsbUtil() {
        // prevent construction, since this is a stateless utility class
    }

    /**
     * Get all USB devices.
     *
     * @return a list of all UsbDevice's
     */
    public static List<UsbDevice> getAllDevices() {
        return getMatchingDevices((short) 0, (short) 0);
    }

    /**
     * Get matching USB devices.
     *
     * @param idVendor  the vendor id to match (or zero to always match)
     * @param idProduct the product id to match (or zero to always match)
     * @return a list of matching UsbDevices
     */
    public static List<UsbDevice> getMatchingDevices(short idVendor, short idProduct) {
        return recursivelyCollectUsbDevices(null, idVendor, idProduct);
    }

    /**
     * Get matching USB device.
     *
     * @param idVendor   the vendor id to match (or zero to always match)
     * @param idProduct  the product id to match (or zero to always match)
     * @param idLocation the location id to match (never zero)
     * @return a list of matching UsbDevices
     */
    public static UsbDevice getMatchingDevice(short idVendor, short idProduct, String idLocation) {
        if ((idLocation != null) && !idLocation.isEmpty()) {
            long longLocationID = Long.decode(idLocation);
            if (longLocationID > 0) {
                for (UsbDevice usbDevice : recursivelyCollectUsbDevices(null, idVendor, idProduct)) {
                    String locationID = getLocationID(usbDevice);
                    if (locationID.equals(idLocation)) {
                        return usbDevice;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get a USB device's full product (manufacturer + product) name.
     *
     * @param usbDevice the USB device to get the full product name of
     * @return the full product name or null if the product name is not encoded
     *         in the device
     */
    @CheckForNull
    public static String getFullProductName(@Nonnull UsbDevice usbDevice) {
        String result = null;
        try {
            String manufacturer = usbDevice.getManufacturerString();
            String product = usbDevice.getProductString();
            if (product != null) {
                if (manufacturer == null || product.startsWith(manufacturer)) {
                    result = product;
                } else {
                    result = Bundle.getMessage("UsbDevice", manufacturer, product);
                }
            }
        } catch (UsbException
                | UnsupportedEncodingException
                | UsbDisconnectedException ex) {
            log.error("Unable to read data from {}", usbDevice, ex);
        }
        return result;
    }

    /**
     * Get a USB device's serial number.
     *
     * @param usbDevice the USB device to get the serial number of
     * @return serial number
     */
    @Nullable
    public static String getSerialNumber(@Nonnull UsbDevice usbDevice
    ) {
        try {
            return usbDevice.getSerialNumberString();
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            log.error("Unable to get serial number of {}", usbDevice);
        }
        return null;
    }

    /**
     * Get a USB device's location id. This encoding the USB device location
     * does not match any operating system's encoding scheme; it is not intended
     * to be displayed to the end user.
     *
     * @param usbDevice the USB device who's location id you want
     * @return location id
     */
    public static String getLocationID(@Nonnull UsbDevice usbDevice) {
        StringBuilder result = new StringBuilder();
        UsbDevice device = usbDevice;
        while (device != null) {
            UsbPort usbPort = usbDevice.getParentUsbPort();
            if (usbPort == null) {
                break;
            }
            result.append(usbPort.getPortNumber());
            device = usbPort.getUsbHub();
        }
        return String.format("0x%s", StringUtils.rightPad(result.reverse().toString(), 8, "0"));
    }

    /**
     * Recursive routine to collect USB devices.
     *
     * @param usbHub    the hub who's devices we want to collect (null for root)
     * @param idVendor  the vendor id to match against
     * @param idProduct the product id to match against
     */
    @Nonnull
    private static List<UsbDevice> recursivelyCollectUsbDevices(
            @Nullable UsbHub usbHub, short idVendor, short idProduct) {
        if (usbHub == null) {
            try {
                return recursivelyCollectUsbDevices(UsbHostManager.getUsbServices().getRootUsbHub(), idVendor, idProduct);
            } catch (UsbException | SecurityException ex) {
                log.error("Exception: {}", ex.toString());
            }
        }
        List<UsbDevice> devices = new ArrayList<>();
        List<UsbDevice> usbDevices = usbHub.getAttachedUsbDevices();
        usbDevices.forEach((usbDevice) -> {
            if (usbDevice instanceof UsbHub) {
                UsbHub childUsbHub = (UsbHub) usbDevice;
                devices.addAll(recursivelyCollectUsbDevices(childUsbHub, idVendor, idProduct));
            } else {
                UsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
                if ((idVendor == 0) || (idVendor == usbDeviceDescriptor.idVendor())) {
                    if ((idProduct == 0) || (idProduct == usbDeviceDescriptor.idProduct())) {
                        devices.add(usbDevice);
                    }
                }
            }
        });
        return devices;
    }

    /**
     * Read message synchronously.
     *
     * @param iface    the interface
     * @param endPoint the end point
     */
    public static void readMessage(@Nonnull UsbInterface iface, int endPoint) {

        UsbPipe pipe = null;

        try {
            iface.claim((UsbInterface usbInterface) -> true);

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();
            pipe.open();

            byte[] data = new byte[8];
            int received = pipe.syncSubmit(data);
            log.debug("{} bytes received", received);

            pipe.close();

        } catch (IllegalArgumentException | UsbDisconnectedException | UsbException | UsbNotActiveException | UsbNotClaimedException | UsbNotOpenException ex) {
            log.error("Unable to read message", ex);
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException | UsbDisconnectedException | UsbException ex) {
                log.error("Unable to release USB device", ex);
            }
        }
    }

    /**
     * Read message asynchronously.
     *
     * @param iface    the interface
     * @param endPoint the end point
     */
    public static void readMessageAsynch(@Nonnull UsbInterface iface, int endPoint) {

        UsbPipe pipe;

        try {
            iface.claim((UsbInterface usbInterface) -> true);

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();

            pipe.open();

            pipe.addUsbPipeListener(new UsbPipeListener() {
                @Override
                public void errorEventOccurred(UsbPipeErrorEvent event) {
                    log.error("UsbPipeErrorEvent: {}", event, event.getUsbException());
                }

                @Override
                public void dataEventOccurred(UsbPipeDataEvent event) {
                    byte[] data = event.getData();
                    log.debug("{} bytes received: {}", data, data);
                }
            });
            pipe.close();
        } catch (UsbDisconnectedException | UsbException | UsbNotActiveException | UsbNotClaimedException | UsbNotOpenException ex) {
            log.error("Unable to read USB message.", ex);
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException | UsbDisconnectedException | UsbException ex) {
                log.error("Unable to release USB device.", ex);
            }
        }
    }

    /**
     * Get USB device interface.
     *
     * @param device the USB device
     * @param index  the USB interface index
     * @return the USB interface
     */
    public static UsbInterface getDeviceInterface(@Nonnull UsbDevice device, int index) {
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        if (configuration != null) {
            return (UsbInterface) configuration.getUsbInterfaces().get(index); // there can be more 1,2,3..
        }
        return null;
    }

    /**
     * Send bulk message.
     *
     * @param iface   the interface
     * @param message the message
     * @param index   index of the endpoint attached to the interface
     */
    public static void sendBulkMessage(@Nonnull UsbInterface iface, @Nonnull String message, int index) {

        UsbPipe pipe;

        try {
            iface.claim((UsbInterface usbInterface) -> true);

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

            log.debug("{} bytes sent", sent);
            pipe.close();

        } catch (IllegalArgumentException | UsbDisconnectedException | UsbException | UsbNotActiveException | UsbNotClaimedException | UsbNotOpenException ex) {
            log.error("Unable to send message.", ex);
        } finally {
            try {
                iface.release();
            } catch (UsbNotActiveException | UsbDisconnectedException | UsbException ex) {
                log.error("Unable to release USB device.", ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(UsbUtil.class);
}
