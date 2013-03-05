// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NetworkDriverAdapter for the MERG system network connection.
 * <P>This connects via a telnet connection.
 *
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision: 21889 $
 */
public class MergNetworkDriverAdapter extends NetworkDriverAdapter {
    
    public MergNetworkDriverAdapter() {
        super();
        options.put("CANID", new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
        setManufacturer(jmri.jmrix.DCCManufacturerList.MERG);
    }
    
    static Logger log = LoggerFactory.getLogger(MergNetworkDriverAdapter.class.getName());

}
