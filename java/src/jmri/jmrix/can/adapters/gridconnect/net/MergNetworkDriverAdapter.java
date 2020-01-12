package jmri.jmrix.can.adapters.gridconnect.net;


/**
 * Implements NetworkDriverAdapter for the MERG system network connection.
 * <p>
 * This connects via a telnet connection.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * 
 */
public class MergNetworkDriverAdapter extends NetworkDriverAdapter {

    public MergNetworkDriverAdapter() {
        super();
        options.put("CANID", new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
        setManufacturer(jmri.jmrix.merg.MergConnectionTypeList.MERG);
    }

}
