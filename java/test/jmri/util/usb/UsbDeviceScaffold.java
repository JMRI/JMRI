package jmri.util.usb;

import javax.usb.*;
import javax.usb.event.UsbDeviceListener;
import java.io.UnsupportedEncodingException;
import java.util.List;
/**
 * Provide a mock USB device used for tests.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class UsbDeviceScaffold implements UsbDevice {

    private String manufacturer;
    private String productString;

    public UsbDeviceScaffold(String manufacturer,String product){
        this.manufacturer = manufacturer;
        productString = product;
    }

    @Override
    public UsbPort getParentUsbPort() throws UsbDisconnectedException {
        return null;
    }

    @Override
    public boolean isUsbHub() {
        return false;
    }

    @Override
    public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
        return manufacturer;
    }

    @Override
    public String getSerialNumberString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
        return null;
    }

    @Override
    public String getProductString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
        return productString;
    }

    @Override
    public Object getSpeed() {
        return null;
    }

    @Override
    public List getUsbConfigurations() {
        return null;
    }

    @Override
    public UsbConfiguration getUsbConfiguration(byte b) {
        return null;
    }

    @Override
    public boolean containsUsbConfiguration(byte b) {
        return false;
    }

    @Override
    public byte getActiveUsbConfigurationNumber() {
        return 0;
    }

    @Override
    public UsbConfiguration getActiveUsbConfiguration() {
        return null;
    }

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public UsbDeviceDescriptor getUsbDeviceDescriptor() {
        return null;
    }

    @Override
    public UsbStringDescriptor getUsbStringDescriptor(byte b) throws UsbException, UsbDisconnectedException {
        return null;
    }

    @Override
    public String getString(byte b) throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
        return null;
    }

    @Override
    public void syncSubmit(UsbControlIrp usbControlIrp) throws UsbException, IllegalArgumentException, UsbDisconnectedException {

    }

    @Override
    public void asyncSubmit(UsbControlIrp usbControlIrp) throws UsbException, IllegalArgumentException, UsbDisconnectedException {

    }

    @Override
    public void syncSubmit(List list) throws UsbException, IllegalArgumentException, UsbDisconnectedException {

    }

    @Override
    public void asyncSubmit(List list) throws UsbException, IllegalArgumentException, UsbDisconnectedException {

    }

    @Override
    public UsbControlIrp createUsbControlIrp(byte b, byte b1, short i, short i1) {
        return null;
    }

    @Override
    public void addUsbDeviceListener(UsbDeviceListener usbDeviceListener) {

    }

    @Override
    public void removeUsbDeviceListener(UsbDeviceListener usbDeviceListener) {

    }
}
