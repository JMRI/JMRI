package jmri.jmrix.anyma;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.annotation.CheckForNull;
import jmri.jmrix.UsbPortAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * AnymaDMX_ managers to be handled.
 * <P>
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */
public class AnymaDMX_Adapter extends UsbPortAdapter {

    private AnymaDMX_Controller dmx = null;

    public AnymaDMX_Adapter() {
        super(new AnymaDMX_SystemConnectionMemo());

        log.info("*	AnymaDMX_Adapter Constructor called");

        //List<String> productNames = new ArrayList<>();
        //List<UsbDevice> usbDevices = USBUtil.getMatchingDevices((short) 0x16C0, (short) 0x05DC);
        //for (UsbDevice usbDevice : usbDevices) {
        //    String fullProductName = USBUtil.getFullProductName(usbDevice);
        //    String serialNumber = USBUtil.getSerialNumber(usbDevice);
        //    if (!serialNumber.isEmpty()) {
        //        fullProductName += " (" + serialNumber + ")";
        //    }
        //    String location = USBUtil.getLocationID(usbDevice);
        //    if (!location.isEmpty()) {
        //        fullProductName += " (" + location + ")";
        //    }
        //    log.info("*full product name: " + fullProductName);
        //    productNames.add(fullProductName);
        //}
        // option1Name = "USB Device"; // NOI18N
        // String[] option1Values = productNames.toArray(new String[productNames.size()]);
        // options.put(option1Name, new Option(option1Name + ":", option1Values, false));
        //
        // options.remove(option2Name);
        // options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));
        //option1Name = "Adapter"; // NOI18N
        //option2Name = "Concentrator-Range"; // NOI18N
        //option3Name = "Protocol"; // NOI18N
        //option4Name = "Device"; // NOI18N
        //options.put(option1Name, new Option("Adapter:", new String[]{"Generic Stand-alone", "MERG Concentrator"}, false)); // NOI18N
        //options.put(option2Name, new Option("Concentrator range:", new String[]{"A-H", "I-P"}, false)); // NOI18N
        //options.put(option3Name, new Option("Protocol:", new String[]{"CORE-ID", "Olimex", "Parallax", "SeeedStudio"}, false)); // NOI18N
        //options.put(option4Name, new Option("Device Type:", new String[] {"MOD-RFID125", "MOD-RFID1356MIFARE"}, false)); // NOI18N
    }

//    @Override
//    public String getCurrentPortName() {
//        log.info("* getCurrentPortName() called.");
//        return "DMX";
//    }

    @Override
    public void dispose() {
        log.info("* dispose() called.");
        super.dispose();
        dmx.shutdown(); // terminate all DMX connections.
    }

    @Override
    public void connect() {
        log.info("* connect() called.");
    }

    @Override
    public void configure() {
        log.info("* configure() called.");
        getSystemConnectionMemo().configureManagers();
    }

    @Override
    public DataInputStream getInputStream() {
        log.info("* getInputStream() called.");
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        log.info("* getOutputStream() called.");
        return null;
    }

    @Override
    public AnymaDMX_SystemConnectionMemo getSystemConnectionMemo() {
        log.info("* getSystemConnectionMemo() called.");
        return (AnymaDMX_SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {
        log.info("* recover() called.");
    }

    /*
    * Get the DMX Controller associated with this object.
    *
    * @return the associaed DMX Controller or null if none exists
     */
    @CheckForNull
    public AnymaDMX_Controller getAnymaDMX_Controller() {
        log.info("* getAnymaDMX_Controller() called.");
        return dmx;
    }

    protected String[] commandStationOptions() {
        return new String[]{"Goodby", "cruel", "world"};
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_Adapter.class);
}
