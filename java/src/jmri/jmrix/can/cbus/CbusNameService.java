package jmri.jmrix.can.cbus;

import java.util.HashSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventBeanData;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to lookup CBUS event names via the event table
 * <p>
 * Node names from the Node Manager
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNameService {
    
    private final CanSystemConnectionMemo _memo;
    
    /**
     * Create a new instance for the default connection
     */
    public CbusNameService(){
        super();
        _memo = null;
    }
    
    /**
     * Create a new instance for a given connection
     * @param memo System Connection
     */
    public CbusNameService(CanSystemConnectionMemo memo){
        super();
        _memo=memo;
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
        CbusEventTableDataModel evMod = getEventModel();
        if (evMod!=null) {
            String addevbuf = evMod.getEventString(nn,en);
            if ( !addevbuf.isEmpty() ) {
                return addevbuf;
            }
        }
        return new CbusEvent(nn,en).toString();
    }

    /**
     * Return a formatted String attempting to locate the event name.
     * <p>
     * get the event name, empty string if event not on event table, or if event name is empty
     *
     * @param nn Node Number
     * @param en Event Number
     * @return Event name if available , else empty string
     */
    @Nonnull
    public String getEventName( int nn, int en ){
        CbusEventTableDataModel evMod = getEventModel();
        if (evMod!=null) {
            return evMod.getEventName(nn,en);
        } else {
            return ("");
        }
    }

    /**
     * Return a formatted String after attempting to locate the node name.
     * <p> 1st attempt - Node Username in node table ( eg. Control Panel West )
     * <p> 2nd attempt - Node Type Name ( eg. CANPAN )
     * <p> fallback empty string
     *
     * @param nn Node Number
     * @return Node name if available , else empty string
     */
    @Nonnull
    public String getNodeName( int nn ){
        CbusNodeTableDataModel model = getNodeModel();
        if (model!=null) {
            return model.getNodeName(nn);
        }
        return "";
    }
    
    /**
     * Get the Sensor Turnout and Light user names associated with event on
     * @param nn Node Number
     * @param en Event Number
     * @param state Event State, either on or off
     * @return Sensor Turnout and Light Beans associated with the CBUS Event.
     * @see jmri.NamedBean
     */
    @Nonnull
    public CbusEventBeanData getJmriBeans(int nn, int en, @Nonnull CbusEvent.EvState state){
        CbusEventTableDataModel evMod = getEventModel();
        if (evMod!=null) {
            return evMod.getEventBeans(nn,en,state);
        } else {
            return new CbusEventBeanData( new HashSet<>(), new HashSet<>());
        }
    }
    
    @CheckForNull
    private CbusNodeTableDataModel getNodeModel(){
        log.debug("memo: {}",_memo);
        return jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
    }
    
    @CheckForNull
    private CbusEventTableDataModel getEventModel(){
        return jmri.InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
    }

    private static final Logger log = LoggerFactory.getLogger(CbusNameService.class);
}
