// RfidNetworkPortController.java
package jmri.jmrix.rfid;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/*
 * Identifying class representing a network communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2015
 * @version $Revision: 28746 $
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


/* @(#)RfidNetworkPortController.java */
