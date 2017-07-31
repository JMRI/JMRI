package jmri.jmrix.ecos.networkdriver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.ecos.EcosPortController;
import jmri.jmrix.ecos.EcosTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import java.io.*;
 import java.net.*;
 import java.util.Vector;*/
/**
 * Implements SerialPortAdapter for the ECOS system network connection.
 * <P>
 * This connects an ECOS command station via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 */
public class NetworkDriverAdapter extends EcosPortController implements jmri.jmrix.NetworkPortAdapter {

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.ecos.EcosSystemConnectionMemo());
        allowConnectionRecovery = true;
        manufacturerName = jmri.jmrix.ecos.EcosConnectionTypeList.ESU;
    }

    /**
     * set up all of the other objects to operate with an ECOS command station
     * connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        EcosTrafficController control = new EcosTrafficController();
        control.connectPort(this);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().setEcosTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    @SuppressFBWarnings(value = "DE_MIGHT_IGNORE",
            justification = "We are closing the connection and not worried if it throws an exception as this stage")
    protected void closeConnection() {
        try {
            socketConn.close();
        } catch (Exception e) {
        }
        opened = false;
    }

    @Override
    protected void resetupConnection() {
        log.info("reconnected to ECoS after lost connection");
        if (opened) {
            this.getSystemConnectionMemo().getTrafficController().connectPort(this);
            this.getSystemConnectionMemo().getTurnoutManager().refreshItems();
            this.getSystemConnectionMemo().getSensorManager().refreshItems();
            this.getSystemConnectionMemo().getLocoAddressManager().refreshItems();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}
