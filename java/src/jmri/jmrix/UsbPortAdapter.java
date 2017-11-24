package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import jmri.util.USBUtil;
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

    public UsbDevice getUsbDevice() {
        if (usbDevice == null) {
            String errorString = openPort(port, null);
            if (errorString != null) {
                log.error(errorString);
            }
        }
        return usbDevice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("*	connect()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        log.debug("*	getInputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        log.debug("*	getOutputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() {
        log.debug("*	recover()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        log.debug("*	configure()");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getPortNames() {
        log.debug("*	getPortNames()");

        List<String> results = new ArrayList<>();
        List<UsbDevice> usbDevices = USBUtil.getMatchingDevices(vendorID, productID);
        for (UsbDevice usbDevice : usbDevices) {
            results.add(USBUtil.getLocationID(usbDevice));
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    public String openPort(String portName, String appName) {
        String result = null;   // assume success (optimist!)

        log.debug("*	openPort('{}','{}')", portName, appName);
        usbDevice = USBUtil.getMatchingDevice(vendorID, productID, portName);
        if (usbDevice == null) {
            // didn't find one at that location... see if we can find any anywhere
            List<UsbDevice> usbDevices = USBUtil.getMatchingDevices(vendorID, productID);
            if (usbDevices.size() == 1) {   // if we found one...
                // ...its location must have changed so...
                usbDevice = usbDevices.get(0);  // use it
            } else {    // otherwise... return error string
                result = String.format(
                        "USB device at location ID %s not found.", portName);
            }
        }
        return result;
    }

    private String port = null;

    /**
     * {@inheritDoc}
     */
    public void setPort(String s) {
        log.debug("*	setPort('{}')", s);
        port = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        log.debug("*	getCurrentPortName()");
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

                //log.debug("sendControlTransfer,  requestType: {}, request: {}, value: {}, index: {}, data: {}", requestType, request, value, index, getByteString(data));
                usbDevice.syncSubmit(usbControlIrp);
                result = true; // it's good!
            } catch (IllegalArgumentException | UsbException | UsbDisconnectedException e) {
                //log.error("Exception " + e);
                //e.printStackTrace();
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(UsbPortAdapter.class);
}
