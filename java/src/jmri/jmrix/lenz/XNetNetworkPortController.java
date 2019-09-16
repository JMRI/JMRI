package jmri.jmrix.lenz;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing an XNet communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2004,2010,2011
 */
public abstract class XNetNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements XNetPortController {

    private boolean timeSlot = true;

    public XNetNetworkPortController() {
        super(new XNetSystemConnectionMemo());
        allowConnectionRecovery = true; // all classes derived from this class
        // can recover from a connection failure
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public abstract boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean okToSend(){
       return hasTimeSlot();
    }

    /**
     * Indicate whether the command station is currently providing a timeslot to this
     * port controller.
     *
     * @return true if the command station is currently providing a timeslot.
     */
    @Override
    public boolean hasTimeSlot(){
        return timeSlot;
    }

    /**
     * Set a variable indicating whether or not the command station is
     * providing a timeslot.
     * <p>
     * This method should be called with the paramter set to false if
     * a "Command Station No Longer Providing a timeslot for communications"
     * (01 05 04) is received.
     * <p>
     * This method should be called with the parameter set to true if
     * a "Command Station is providing a timeslot for communications again."
     * (01 07 06) is received.
     *
     * @param timeslot true if a timeslot is being sent, false otherwise.
     */
    @Override
    public void setTimeSlot(boolean timeslot){
       timeSlot = timeslot;
    }   

    /**
     * We need a way to say if the output buffer is empty or not.
     */
    @Override
    public void setOutputBufferEmpty(boolean s) {
    } // Maintained for compatibility with XNetPortController. Simply ignore calls !!!

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void dispose() {
        super.dispose();
        log.debug("Dispose called");
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
        this.getSystemConnectionMemo().getXNetTrafficController().connectPort(this);
    }

    private final static Logger log = LoggerFactory.getLogger(XNetNetworkPortController.class);

}
