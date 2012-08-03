// SerialDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.SystemConnectionMemo;

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

    public SerialDriverAdapter(){
        super();
        option2Name = "CANID";
        options.put(option2Name, new Option("CAN ID for CAN-USB", new String[]{"127", "126", "125", "124", "123", "122", "121", "120"}));
    
    }
    /**
     * set up all of the other objects to operate with a CAN RS adapter
     * connected to this port
     */
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new MergTrafficController();
        try {
            tc.setCanId(Integer.parseInt(getOptionState(option2Name)));
        } catch (Exception e) {
            log.error("Cannot parse CAN ID - check your preference settings "+e);
            log.error("Now using default CAN ID");
        }
        
        adaptermemo.setTrafficController(tc);
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        adaptermemo.setProtocol(getOptionState(option1Name));

        // do central protocol-specific configuration    
        //jmri.jmrix.can.ConfigurationManager.configure(getOptionState(option1Name));
        adaptermemo.configureManagers();

    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
        
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}
