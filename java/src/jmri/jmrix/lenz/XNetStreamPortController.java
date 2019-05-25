package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Abstract base for classes representing an XNet communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2004,2010,2014
 */
public class XNetStreamPortController extends jmri.jmrix.AbstractStreamPortController implements XNetPortController {

    private boolean timeSlot = true;

    public XNetStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new XNetSystemConnectionMemo(), in, out, pname);
    }

    public XNetStreamPortController() {
        super(new XNetSystemConnectionMemo());
    }

    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    @Override
    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public boolean status() {
        return (getInputStream()!=null && getOutputStream()!=null);
    }

    /**
     * Can the port accept additional characters?
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean okToSend() {
        return ( status() && hasTimeSlot() );
    }

    /**
     * Indicate whether the Command Station is currently providing a timeslot to this
     * port controller.
     *
     * @return true if the Command Station is currently providing a timeslot.
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
     * We need a way to say if the output buffer is empty or full this should
     * only be set to false by external processes.
     */
    @Override
    synchronized public void setOutputBufferEmpty(boolean s) {
    }

}
