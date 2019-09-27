package jmri.jmrix.can.cbus;

import javax.annotation.Nonnull;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to lookup CBUS event names via the event table
 * <p>
 * Node names from the Node Manager
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNameService {
    
    private CbusEventTableDataModel eventModel;
    private CbusNodeTableDataModel nodeModel;

    public CbusNameService(){
    }

    /**
     * Return a formatted String attempting to use the event toString method
     * <p>
     * eg no event table  (123,456) will return NN:123 EN:456 
     * with event table instance would return   NN:123 Node Name EN:456 Event Name 
     * No node present returns just event (0,56) EN:56 
     * All with trailing space
     *
     * @param nn Node Number
     * @param en Event Number
     * @return Event and node number with event and node name if available
     */
    @Nonnull
    public String getEventNodeString( int nn, int en ){
        // log.debug("looking up node {} event {}",nn,en);
        try {
            eventModel = jmri.InstanceManager.getDefault(CbusEventTableDataModel.class);
            String addevbuf = eventModel.getEventString(nn,en);
            if ( !addevbuf.isEmpty() ) {
                return addevbuf;
            }
        } catch (NullPointerException e) {
        }
        return new CbusEvent(nn,en).toString();
    }

    /**
     * Return a formatted String attempting locate the event name
     * <p>
     * get the event name, empty string if event not on event table, or if event name is empty
     *
     * @param nn Node Number
     * @param en Event Number
     * @return Event name if available , else empty string
     */
    @Nonnull
    public String getEventName( int nn, int en ){
        try {
            eventModel = jmri.InstanceManager.getDefault(CbusEventTableDataModel.class);
            return eventModel.getEventName(nn,en);
        } catch (NullPointerException e) {
            return ("");
        }
    }

    /**
     * Return a formatted String attempting locate the node name
     * <p> 1st attempt - Node Username in node table ( eg. Control Panel West )
     * <p> 2nd attempt - Node Type Name ( eg. CANPAN )
     * <p> fallback empty string
     *
     * @param nn Node Number
     * @return Node name if available , else empty string
     */
    @Nonnull
    public String getNodeName( int nn ){
        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
            return nodeModel.getNodeName(nn);
        } catch (NullPointerException e) {
            return ("");
        }
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusNameService.class);
}
