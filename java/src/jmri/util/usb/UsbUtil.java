package jmri.util.usb;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * USB utilities.
 *
 * @author George Warner Copyright (c) 2017-2018
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
        return getMatchingDevices((short) 0, (short) 0, null);
    }

    /**
     * Get matching USB devices.
     *
     * @param idVendor     the vendor id to match (zero matches any)
     * @param idProduct    the product id to match (zero matches any)
     * @param serialNumber the serial number to match (null matches any)
     * @return a list of matching UsbDevices
     */
    public static List<UsbDevice> getMatchingDevices(short idVendor, short idProduct, @CheckForNull String serialNumber) {
        return findUsbDevices(null, idVendor, idProduct, serialNumber);
    }

    /**
     * Get matching USB device.
     *
     * @param idVendor     the vendor id to match (zero matches any)
     * @param idProduct    the product id to match (zero matches any)
     * @param serialNumber the serial number to match (null matches any)
     * @param idLocation   the location to match
     * @return the matching UsbDevice or null if no match could be found
     */
    @CheckForNull
    public static UsbDevice getMatchingDevice(short idVendor, short idProduct, @CheckForNull String serialNumber, @Nonnull String idLocation) {
        for (UsbDevice usbDevice : findUsbDevices(null, idVendor, idProduct, serialNumber)) {
            String locationID = getLocation(usbDevice);
            if (locationID.equals(idLocation)) {
                return usbDevice;
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
        } catch (UsbException | UnsupportedEncodingException ex) {
            log.error("Unable to read data from {}", usbDevice, ex);
        } catch (UsbDisconnectedException ex) {
            log.error("Unable to read data from disconnected device {}", usbDevice);
        }
        return result;
    }

    /**
     * Get a USB device's serial number.
     *
     * @param usbDevice the USB device to get the serial number of
     * @return serial number
     */
    @CheckForNull
    public static String getSerialNumber(@Nonnull UsbDevice usbDevice) {
        try {
            return usbDevice.getSerialNumberString();
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            log.error("Unable to get serial number of {}", usbDevice);
        }
        return null;
    }

    /**
     * Get a unique value that represents the device's location in the USB
     * device topology.
     * <p>
     * The location is a series of USB ports separated by colons (:) starting
     * from the root hub (a virtual hub maintained by the operating system),
     * represented as {@code USB} in the location, passing through hubs (which
     * may be virtual or physical), to the port the requested device is plugged
     * into.
     * <p>
     * <strong>Note:</strong> this method should only be used to uniquely
     * identify USB devices in combination with consideration of the USB device
     * product ID, vendor ID, and serial number, as using this alone could mean
     * that two devices with the same product and vendor IDs, but different
     * serial numbers could be misidentified if unplugged and reconnected in
     * ports previously used by the other device, or if the hub does not
     * consistently enumerate ports the same way.
     *
     * @param usbDevice the device to get the location of
     * @return the location
     */
    public static String getLocation(@Nonnull UsbDevice usbDevice) {
        UsbDevice device = usbDevice;
        StringBuilder path = new StringBuilder();
        while (device != null) {
            UsbPort port = device.getParentUsbPort();
            if (port == null) {
                break;
            }
            path.append(Byte.toString(port.getPortNumber())).append(':');
            device = port.getUsbHub();
        }
        return String.format("USB%s", path.reverse().toString());
    }

    /**
     * Recursive routine to collect USB devices.
     *
     * @param usbHub       the hub who's devices we want to collect (null for
     *                     root)
     * @param idVendor     the vendor id to match against
     * @param idProduct    the product id to match against
     * @param serialNumber the serial number to match against
     */
    @Nonnull
    private static List<UsbDevice> findUsbDevices(
            @CheckForNull UsbHub usbHub,
            short idVendor,
            short idProduct,
            @CheckForNull String serialNumber) {
        if (usbHub == null) {
            try {
                return findUsbDevices(UsbHostManager.getUsbServices().getRootUsbHub(), idVendor, idProduct, serialNumber);
            } catch (UsbException | SecurityException ex) {
                log.error("Exception: {}", ex.toString());
                return new ArrayList<>(); // abort with an empty list
            }
        }

        @SuppressWarnings("unchecked") // cast required by UsbHub API
        List<UsbDevice> usbDevices = usbHub.getAttachedUsbDevices();
        
        List<UsbDevice> devices = new ArrayList<>();
        usbDevices.forEach((usbDevice) -> {
            if (usbDevice instanceof UsbHub) {
                UsbHub childUsbHub = (UsbHub) usbDevice;
                devices.addAll(findUsbDevices(childUsbHub, idVendor, idProduct, serialNumber));
            } else {
                UsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
                try {
                    if (((idVendor == 0) || (idVendor == usbDeviceDescriptor.idVendor()))
                            && ((idProduct == 0) || (idProduct == usbDeviceDescriptor.idProduct()))
                            && ((serialNumber == null) || serialNumber.equals(usbDevice.getSerialNumberString()))) {
                        devices.add(usbDevice);
                    }
                } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
                    log.error("Unable to request serial number from device {}", usbDevice, ex);
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
    public static void readMessage(@Nonnull UsbInterface iface, byte endPoint) {

        try {
            iface.claim((UsbInterface usbInterface) -> true);
            UsbPipe pipe = iface.getUsbEndpoint(endPoint).getUsbPipe();
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
    public static void readMessageAsynch(@Nonnull UsbInterface iface, byte endPoint) {

        try {
            iface.claim((UsbInterface usbInterface) -> true);

            UsbPipe pipe = iface.getUsbEndpoint(endPoint).getUsbPipe();

            pipe.open();

            pipe.addUsbPipeListener(new UsbPipeListener() {
                @Override
                public void errorEventOccurred(UsbPipeErrorEvent event) {
                    log.error("UsbPipeErrorEvent: {}", event, event.getUsbException());
                }

                @Override
                public void dataEventOccurred(UsbPipeDataEvent event) {
                    byte[] data = event.getData();
                    if (log.isDebugEnabled()) { // avoid array->string conversion unless debugging
                        log.debug("bytes received: {}", Arrays.toString(data));
                    }
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
    public static UsbInterface getDeviceInterface(@Nonnull UsbDevice device, byte index) {
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        if (configuration != null) {
            return configuration.getUsbInterface(index);
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(UsbUtil.class);
}
