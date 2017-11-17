package jmri.jmrix.anyma;


import java.io.DataInputStream;
import java.io.DataOutputStream;
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
public class AnymaDMX_UsbPortAdapter extends UsbPortAdapter {

    //private AnymaDMX_Controller dmx = null;

    public AnymaDMX_UsbPortAdapter() {
        super(new AnymaDMX_SystemConnectionMemo());
        log.debug("*    Constructor");

        setVendorID((short) 0x16C0);
        setProductID((short) 0x05DC);
    }

    @Override
    public void dispose() {
        log.debug("*    dispose() called.");
        //dmx.shutdown(); // terminate all DMX connections.
        super.dispose();
    }

    @Override
    public void configure() {
        log.debug("*    configure() called.");

      // connect to the traffic controller
        AnymaDMX_TrafficController control = new AnymaDMX_TrafficController();
        control.connectPort(this);

        getSystemConnectionMemo().setTrafficController(control);
        getSystemConnectionMemo().configureManagers();
    }

    @Override
    public DataInputStream getInputStream() {
        log.debug("*    getInputStream() called.");
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        log.debug("*    getOutputStream() called.");
        return null;
    }

    @Override
    public AnymaDMX_SystemConnectionMemo getSystemConnectionMemo() {
        log.debug("*    getSystemConnectionMemo() called.");
        return (AnymaDMX_SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_UsbPortAdapter.class);
}
