package jmri.jmrix.mrc;

/**
 * Layout interface, similar to command station
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface MrcInterface {

    void sendMrcMessage(MrcMessage msg);

    void addTrafficListener(int Mask, MrcTrafficListener l);

    void removeTrafficListener(int Mask, MrcTrafficListener l);

    void changeTrafficListener(int Mask, MrcTrafficListener l);

    boolean status();   // true if the implementation is operational

    /**
     * Mask value to request notification of all incoming messages
     */
    int ALL = ~0;

    /**
     * Mask value to request notification of messages effecting throttle changes
     */
    int THROTTLEINFO = 1;

    /**
     * (MrcInter Mask value to request notification of messages associated with
     * programming
     */
    int PROGRAMMING = 2;

    /**
     * Mask value to request notification of messages indicating changes in
     * turnout status
     */
    int TURNOUTS = 4;

    /**
     * Mask value to request notification of messages indicating changes in
     * sensor status
     */
    int SENSORS = 8;

    /**
     * Mask value to request notification of messages associated with layout
     * power
     */
    int POWER = 16;

    /**
     * Mask value to request notification of messages associated with layout
     * power
     */
    int CLOCK = 32;

    int POLL = 64;
}



