package jmri.jmrix.rfid;

/*
 * Identifying class representing a network communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2015
 */
public abstract class RfidNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {

    protected RfidNetworkPortController(RfidSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public RfidSystemConnectionMemo getSystemConnectionMemo() {
        return (RfidSystemConnectionMemo) super.getSystemConnectionMemo();
    }
}
