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
public class AnymaDMX_Adapter extends UsbPortAdapter {

    private AnymaDMX_Controller dmx = null;

    public AnymaDMX_Adapter() {
        super(new AnymaDMX_SystemConnectionMemo());

        log.info("*	AnymaDMX_Adapter Constructor called");

        setVendorID((short) 0x16C0);
        setProductID((short) 0x05DC);
    }

    @Override
    public void dispose() {
        log.info("* dispose() called.");
        super.dispose();
        dmx.shutdown(); // terminate all DMX connections.
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

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_Adapter.class);
}
