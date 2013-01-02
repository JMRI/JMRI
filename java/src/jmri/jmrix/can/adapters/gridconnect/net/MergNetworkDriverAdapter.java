// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;

import java.util.Vector;

/**
 * Implements NetworkDriverAdapter for the MERG system network connection.
 * <P>This connects via a telnet connection.
 *
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision: 21889 $
 */
public class MergNetworkDriverAdapter extends NetworkDriverAdapter {

    //This should all probably be updated to use the AbstractNetworkPortContoller
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
    
    public MergNetworkDriverAdapter() {
        super();
        options.put("CANID", new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
        setManufacturer(jmri.jmrix.DCCManufacturerList.MERG);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergNetworkDriverAdapter.class.getName());

}
