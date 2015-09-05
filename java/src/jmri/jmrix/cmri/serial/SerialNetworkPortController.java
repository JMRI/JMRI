// SerialNetworkPortController.java
package jmri.jmrix.cmri.serial;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/*
 * Identifying class representing a network communications port
 * @author			Bob Jacobsen    Copyright (C) 2001, 2015
 * @version $Revision: 28746 $
 */
public abstract class SerialNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {

    protected SerialNetworkPortController(CMRISystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public CMRISystemConnectionMemo getSystemConnectionMemo() {
        return (CMRISystemConnectionMemo) super.getSystemConnectionMemo();
    }
}


/* @(#)SerialNetworkPortController.java */
