package jmri.jmrix.anyma_dmx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.usb.UsbDevice;
import jmri.util.USBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * AnymaDMX_ managers to be handled.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender Copyright (C) 2015
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_Adapter extends jmri.jmrix.AbstractPortController
        implements jmri.jmrix.PortAdapter {

    private DMX_Controller dmx = null;
    private String[] option1Values = null;

    public AnymaDMX_Adapter() {
        super(new AnymaDMX_SystemConnectionMemo());
        log.debug("*	AnymaDMX_Adapter Constructor called");
        this.manufacturerName = AnymaDMX_ConnectionTypeList.ANYMA_DMX;
        try {
            dmx = DMX_Factory.getInstance();
            opened = true;
        } catch (UnsatisfiedLinkError er) {
            log.error("Expected to run on Anyma DMX, but does not appear to be.");
        }
        option1Name = "USB Device"; // NOI18N

        List<String> productNames = new ArrayList<>();
        List<UsbDevice> usbDevices = USBUtil.getMatchingDevices((short) 0x16C0, (short) 0x05DC);
        for (UsbDevice usbDevice : usbDevices) {
            String fullProductName = USBUtil.getFullProductName(usbDevice);
            String serialNumber = USBUtil.getSerialNumber(usbDevice);
            if (!serialNumber.isEmpty()) {
                fullProductName += " (" + serialNumber + ")";
            }
            String location = USBUtil.getLocationID(usbDevice);
            if (!location.isEmpty()) {
                fullProductName += " (" + location + ")";
            }
            productNames.add(fullProductName);
        }
        option1Values = new String[productNames.size()];
        option1Values = productNames.toArray(option1Values);

        options.put(option1Name, new Option(option1Name + ":", option1Values, false));
    }

    @Override
    public String getCurrentPortName() {
        return "DMX";
    }

    @Override
    public void dispose() {
        super.dispose();
        dmx.shutdown(); // terminate all DMX connections.
    }

    @Override
    public void connect() {
    }

    @Override
    public void configure() {
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public DataInputStream getInputStream() {
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return null;
    }

    @Override
    public AnymaDMX_SystemConnectionMemo getSystemConnectionMemo() {
        return (AnymaDMX_SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {
    }

    /*
    * Get the DMX Controller associated with this object.
    *
    * @return the associaed DMX Controller or null if none exists
     */
    @CheckForNull
    public DMX_Controller getDMX_Controller() {
        return dmx;
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_Adapter.class
    );
}
