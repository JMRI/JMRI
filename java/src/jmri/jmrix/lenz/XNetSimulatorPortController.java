package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010
  */
public abstract class XNetSimulatorPortController extends jmri.jmrix.AbstractSerialPortController implements XNetPortController {

    private boolean timeSlot = true;

    public XNetSimulatorPortController() {
        super(new XNetSystemConnectionMemo());
    }

    // base class. Implementations will provide InputStream and OutputStream
    // objects to XNetTrafficController classes, who in turn will deal in messages.    
    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
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
     * Indiciate the command station is currently providing a timeslot to this
     * port controller.
     *
     * @return true if the command station is currently providing a timeslot.
     */
    @Override
    public boolean hasTimeSlot(){
        return timeSlot;
    }

    /**
     * <p>
     * Set a variable indicating whether or not the command station is
     * providing a timeslot.
     * </p>
     * <p>
     * This method should be called with the paramter set to false if
     * a "Command Station No Longer Providing a timeslot for communications"
     * (01 05 04) is received.
     * </p>
     * <p>
     * This method should be called with the parameter set to true if
     * a "Command Station is providing a timeslot for communications again."
     * (01 07 06) is received.
     * </p>
     *
     * @param timeslot true if a timeslot is being sent, false otherwise.
     */
    @Override
    public void setTimeSlot(boolean timeslot){
       timeSlot = timeslot;
    }
    /**
     * We need a way to say if the output buffer is empty or not
     */
    public abstract void setOutputBufferEmpty(boolean s);

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    @Override
    public String[] validBaudRates() {
        return null;
    }


}



