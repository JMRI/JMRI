package jmri.jmrix.roco.z21;


import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the XNetPacketizer for Roco the Roco Z21 XPressNet 
 * Tunnel.
 *
 * @author	Paul Bender Copyright (C) 2017 
 *
 */
public class Z21XNetPacketizer extends XNetPacketizer {

    /**
     * Must provide a LenzCommandStation reference at creation time
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     */
    Z21XNetPacketizer(LenzCommandStation pCommandStation) {
        super(pCommandStation);
    }

    @Override
    public void handleOneIncomingReply() throws java.io.IOException {
       try{
           super.handleOneIncomingReply();
       } catch(java.io.IOException ioe){
           log.info("Z21 XPressNet Connection Terminanted");
           ConnectionStatus.instance().setConnectionState(controller.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
       }
    }

    @Override
    protected void terminate(){
        // do nothing on terminate.
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetPacketizer.class.getName());
}
