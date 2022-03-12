package jmri.jmrix.rfid;

/*
 * Identifying class representing a network communications port
 * @author Bob Jacobsen Copyright (C) 2001, 2015
 */
public abstract class RfidNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {

    protected RfidNetworkPortController(RfidSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        allowConnectionRecovery = true;
    }

    @Override
    public RfidSystemConnectionMemo getSystemConnectionMemo() {
        return (RfidSystemConnectionMemo) super.getSystemConnectionMemo();
    }
    
    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
        if (status()) {
            this.getSystemConnectionMemo().getTrafficController().connectPort(this);
        }
    }
}
