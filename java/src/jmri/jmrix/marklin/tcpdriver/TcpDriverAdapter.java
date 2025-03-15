package jmri.jmrix.marklin.tcpdriver;

import jmri.jmrix.marklin.MarklinPortController;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficController;

/**
 * Implements NetworkPortAdapter for the Marklin system network TCP connection.
 * <p>
 * This connects a Marklin command station via a TCP connection. 
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008, 2025
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class TcpDriverAdapter extends MarklinPortController {

    public TcpDriverAdapter() {
        super(new MarklinSystemConnectionMemo());
        allowConnectionRecovery = true;
        manufacturerName = jmri.jmrix.marklin.MarklinConnectionTypeList.MARKLIN;
        m_port = 15731;
    }

    @Override //ports are fixed and not user set
    public void setPort(int p) {
    }

    @Override //ports are fixed and not user set
    public void setPort(String p) {
    }

    /**
     * Set up all of the other objects to operate with a Marklin command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        MarklinTrafficController control = new MarklinTrafficController();
        control.connectPort(this);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().setMarklinTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TcpDriverAdapter.class);

}
