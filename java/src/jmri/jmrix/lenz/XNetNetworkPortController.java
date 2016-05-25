// XNetNetworkPortController.java
package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010,2011
 * @version	$Revision$
 */
public abstract class XNetNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements XNetPortController {

    public XNetNetworkPortController() {
        super(new XNetSystemConnectionMemo());
        allowConnectionRecovery = true; // all classes derived from this class
        // can recover from a connection failure
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    public abstract boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     */
    public abstract boolean okToSend();

    /**
     * We need a way to say if the output buffer is empty or not
     */
    public void setOutputBufferEmpty(boolean s) {
    } // Maintained for compatibility with XNetPortController. Simply ignore calls !!!

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void dispose() {
        super.dispose();
        log.debug("Dispose called");
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
        this.getSystemConnectionMemo().getXNetTrafficController().connectPort(this);
    }

    private final static Logger log = LoggerFactory.getLogger(XNetNetworkPortController.class.getName());

}


/* @(#)XNetNetworkPortController.java */
