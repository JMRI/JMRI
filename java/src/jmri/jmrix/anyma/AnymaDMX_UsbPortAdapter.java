package jmri.jmrix.anyma;

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

    /**
     * constructor
     */
    public AnymaDMX_UsbPortAdapter() {
        super(new AnymaDMX_SystemConnectionMemo());
        log.debug("*    Constructor");

        setVendorID((short) 0x16C0);
        setProductID((short) 0x05DC);
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        log.debug("*    configure() called.");

        // connect to the traffic controller
        AnymaDMX_TrafficController controller = new AnymaDMX_TrafficController();
        controller.connectPort(this);

        AnymaDMX_SystemConnectionMemo memo = (AnymaDMX_SystemConnectionMemo) getSystemConnectionMemo();

        memo.setTrafficController(controller);
        memo.configureManagers();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_UsbPortAdapter.class);
}
