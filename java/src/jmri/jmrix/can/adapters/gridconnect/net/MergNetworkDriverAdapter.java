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

        options.put("CANID", new Option(Bundle.getMessage("JMRICANID"),
            jmri.jmrix.can.cbus.CbusConstants.getValidFixedCanIds(),
            jmri.jmrix.can.cbus.CbusConstants.DEFAULT_JMRI_CAN_ID_STRING ));
        super.setManufacturer(jmri.jmrix.merg.MergConnectionTypeList.MERG);
    }

}
