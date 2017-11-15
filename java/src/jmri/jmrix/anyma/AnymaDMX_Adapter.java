package jmri.jmrix.anyma;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.usb.UsbDevice;
import jmri.jmrix.USBPortAdapter;
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
public class AnymaDMX_Adapter extends USBPortAdapter {

    private AnymaDMX_Controller dmx = null;

    public AnymaDMX_Adapter() {
        super(new AnymaDMX_SystemConnectionMemo());

        log.info("*	AnymaDMX_Adapter Constructor called");

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
            log.info("*full product name: " + fullProductName);
            productNames.add(fullProductName);
        }
        option1Name = "USB Device"; // NOI18N
        String[] option1Values = productNames.toArray(new String[productNames.size()]);
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

        String opt1 = getOptionState(option1Name);
        log.info("*opt1: " + opt1);
        String opt2 = getOptionState(option2Name);
        log.info("*opt2: " + opt2);
        String opt3 = getOptionState(option3Name);
        log.info("*opt3: " + opt3);
        String opt4 = getOptionState(option4Name);
        log.info("*opt4: " + opt4);

        // Why is memo null here when it was new'd in the constructor for this class?!?
        if (getSystemConnectionMemo() != null) {
            getSystemConnectionMemo().configureManagers();
        }
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

    protected String[] commandStationOptions() {
        return new String[]{"Goodby", "cruel", "world"};
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_Adapter.class
    );
}
