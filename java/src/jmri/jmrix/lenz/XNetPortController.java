package jmri.jmrix.lenz;

/**
 * Interface for XNetPortController objects.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public interface XNetPortController extends jmri.jmrix.PortAdapter {

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    boolean status();

    /**
     * Can the port accept additional characters?
     * <p>
     * This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     * @return true if OK to send, else false.
     */
    boolean okToSend();

    /**
     * We need a way to say if the output buffer is empty or not.
     * @param s true to set buffer empty, else false.
     */
    void setOutputBufferEmpty(boolean s);

    /**
     * Indicate the command station is currently providing a timeslot to this
     * port controller. 
     *
     * @return true if the command station is currently providing a timeslot.
     */
    boolean hasTimeSlot();

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
    void setTimeSlot(boolean timeslot);

}
