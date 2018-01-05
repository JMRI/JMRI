package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import jmri.util.usb.UsbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables basic setup of a USB interface for a jmrix implementation.
 *
 * @author George Warner Copyright (C) 2017
 */
public class UsbPortAdapter extends AbstractPortController {

    private Short vendorID = 0;
    private Short productID = 0;
    private String serialNumber = null;
    protected UsbDevice usbDevice = null;

    public UsbPortAdapter(SystemConnectionMemo memo) {
        super(memo);
    }

    public Short getVendorID() {
        return vendorID;
    }

    public void setVendorID(Short value) {
        vendorID = value;
    }

    public Short getProductID() {
        return productID;
    }

    public void setProductID(Short value) {
        productID = value;
    }

    /**
     * Get the device serial number.
     *
     * @return the serial number or null if there is no serial number
     */
    public String getSerialNumber() {
        if (serialNumber != null && serialNumber.trim().isEmpty()) {
            serialNumber = null;
        }
        return serialNumber;
    }

    /**
     * Set the device serial number.
     *
     * @param serialNumber the serial number; if null, empty, or only containing
     *                     whitespace, sets property to null
     */
    public void setSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            this.serialNumber = null;
        } else {
            this.serialNumber = serialNumber;
        }
    }

    public UsbDevice getUsbDevice() {
        if (usbDevice == null) {
            log.debug("Getting device at {}", port);
            String error = openPort(port, serialNumber);
            if (error != null) {
                log.error(error);
            }
        }
        return usbDevice;
    }

    public String openPort(String portName, String serialNumber) {
        usbDevice = UsbUtil.getMatchingDevice(vendorID, productID, serialNumber, portName);
        if (usbDevice == null) {
            List< UsbDevice> usbDevices = UsbUtil.getMatchingDevices(vendorID, productID, serialNumber);
            if (usbDevices.size() == 1) {
                usbDevice = usbDevices.get(0);
            } else {
                // search for device with same vendor/product ID, but possibly different serial number
                usbDevices = UsbUtil.getMatchingDevices(vendorID, productID, null);
                if (usbDevices.size() == 1) {
                    usbDevice = usbDevices.get(0);
                } else {
                    return String.format("Single USB device with vendor id %s and product id %s not found.", vendorID, productID);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("connect()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        log.debug("getInputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        log.debug("getOutputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() {
        log.debug("recover()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        log.debug("configure()");
    }

    /**
     * Get the list of USB locations with devices matching a single
     * vendor/product ID combination. These are "portNames" to match the calling
     * API.
     *
     * @return the list of locations with matching devices; this is an empty
     *         list if there are no matches
     */
    @Nonnull
    public List<String> getPortNames() {
        log.debug("getPortNames()");

        List<String> results = new ArrayList<>();
        List<UsbDevice> usbDevices = UsbUtil.getMatchingDevices(vendorID, productID, null);
        usbDevices.forEach((device) -> {
            results.add(UsbUtil.getLocation(device));
        });

        return results;
    }

    private String port = null;

    public void setPort(String s) {
        log.debug("setPort('{}')", s);
        port = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        log.debug("getCurrentPortName()");
        return port;
    }

    /**
     * send USB control transfer
     *
     * @param requestType the request type
     * @param request     the request
     * @param value       the value
     * @param index       the index
     * @param data        the data
     * @return true if successful sent
     */
    public boolean sendControlTransfer(int requestType, int request, int value, int index, byte[] data) {
        boolean result = false;    // assume failure (pessimist!)
        if (usbDevice != null) {
            try {
                UsbControlIrp usbControlIrp = usbDevice.createUsbControlIrp(
                        (byte) requestType, (byte) request,
                        (short) value, (short) index);
                if (data == null) {
                    data = new byte[0];
                }
                usbControlIrp.setData(data);
                usbControlIrp.setLength(data.length);

                //log.trace("sendControlTransfer,  requestType: {}, request: {}, value: {}, index: {}, data: {}", requestType, request, value, index, getByteString(data));
                usbDevice.syncSubmit(usbControlIrp);
                result = true; // it's good!
            } catch (IllegalArgumentException | UsbException | UsbDisconnectedException e) {
                log.error("Exception transferring control", e);
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(UsbPortAdapter.class
    );
}
