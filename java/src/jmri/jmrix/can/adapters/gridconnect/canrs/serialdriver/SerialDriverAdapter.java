// SerialDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;

/**
 * Implements SerialPortAdapter for the MERG CAN-RS or CAN-USB.
 * <P>
 * This connects to the MERG adapter via a serial com port
 * (real or virtual).
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 *
 * @author			Andrew Crosland Copyright (C) 2008
 * @author			Bob Jacobsen Copyright (C) 2009
 * @version			$Revision$
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter  implements jmri.jmrix.SerialPortAdapter {

    /**
     * set up all of the other objects to operate with a CAN RS adapter
     * connected to this port
     */
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        adaptermemo.setTrafficController(MergTrafficController.instance());
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        MergTrafficController.instance().connectPort(this);

        adaptermemo.setProtocol(mOpt1);

        // do central protocol-specific configuration    
        //jmri.jmrix.can.ConfigurationManager.configure(mOpt1);
        adaptermemo.configureManagers();

    }

    /**
     * Option 2 is CAN identifier to be used by adapter
     */
    public String[] validOption2() { return new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return "CAN ID for CAN-USB"; }

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}
