package jmri.jmrix.can.adapters.gridconnect.net;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;


/**
 * Implements NetworkDriverAdapter for the MERG system network connection.
 * <p>
 * This connects via a telnet connection.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * 
 */
@API(status = EXPERIMENTAL)
public class MergNetworkDriverAdapter extends NetworkDriverAdapter {

    public MergNetworkDriverAdapter() {
        super();
        options.put("CANID", new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
        setManufacturer(jmri.jmrix.merg.MergConnectionTypeList.MERG);
    }

}
