package jmri.jmrix.anyma_dmx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.usb.UsbDevice;
import jmri.jmrix.AbstractPortController;
import jmri.jmrix.PortAdapter;
import jmri.util.USBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * AnymaDMX_ managers to be handled.
 * <P>
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_Adapter extends AbstractPortController
        implements PortAdapter {

    private AnymaDMX_Controller dmx = null;
    private String[] option1Values = null;

    public AnymaDMX_Adapter() {
        super(new AnymaDMX_SystemConnectionMemo());
        log.info("*	AnymaDMX_Adapter Constructor called");
        this.manufacturerName = AnymaDMX_ConnectionTypeList.ANYMA_DMX;
        try {
            dmx = AnymaDMX_Factory.getInstance();
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

        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));
    }

    @Override
    public String getCurrentPortName() {
        log.info("*	AnymaDMX_Adapter.getCurrentPortName() called.");
        return "DMX";
    }

    @Override
    public void dispose() {
        log.info("*	AnymaDMX_Adapter.dispose() called.");
        super.dispose();
        dmx.shutdown(); // terminate all DMX connections.
    }

    @Override
    public void connect() {
        log.info("*	AnymaDMX_Adapter.connect() called.");
    }

    @Override
    public void configure() {
        log.info("*	AnymaDMX_Adapter.configure() called.");
        log.info("*         getOptionState(option2Name): {0}", getOptionState(option2Name));

        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public DataInputStream getInputStream() {
        log.info("*	AnymaDMX_Adapter.getInputStream() called.");
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        log.info("*	AnymaDMX_Adapter.getOutputStream() called.");
        return null;
    }

    @Override
    public AnymaDMX_SystemConnectionMemo getSystemConnectionMemo() {
        log.info("*	AnymaDMX_Adapter.getSystemConnectionMemo() called.");
        return (AnymaDMX_SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {
        log.info("*	AnymaDMX_Adapter.recover() called.");
    }

    /*
    * Get the DMX Controller associated with this object.
    *
    * @return the associaed DMX Controller or null if none exists
     */
    @CheckForNull
    public AnymaDMX_Controller getAnymaDMX_Controller() {
        log.info("*	AnymaDMX_Adapter.getAnymaDMX_Controller() called.");
        return dmx;
    }

    private String[] commandStationOptions() {
        return new String[] { "Goodby", "cruel", "world" };
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_Adapter.class
    );
}
