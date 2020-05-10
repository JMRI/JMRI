package jmri.jmrix.can.cbus;

import javax.annotation.CheckForNull;
import jmri.jmrix.can.CanMessage;

/**
 * Interface for CBUS Sensors, Turnouts and Lights to report CBUS Events.
 * @author Steve Young (c) 2020
 */
public interface CbusEventInterface {
    
    /**
     * Get event for primary Bean On Action.
     * e.g. without inversion, Light On.
     * @return Event for the Action, may be null
     */
    @CheckForNull
    public abstract CanMessage getBeanOnMessage();
    
    /**
     * Get event for primary Bean Off Action.
     * e.g. without Inversion Light Off.
     * @return Event for the Action, may be null
     */
    @CheckForNull
    public abstract CanMessage getBeanOffMessage();
    
    /**
     * Check if CanMessage is an event.
     * @param m CAN Frame to test.
     * @return Passed CanMessage if event, else null.
     */
    @CheckForNull
    public default CanMessage checkEvent(CanMessage m) {
        if ( CbusMessage.isEvent(m) ){
            return m;
        }
        return null;
    }
    
}
