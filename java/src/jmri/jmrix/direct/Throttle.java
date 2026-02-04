package jmri.jmrix.direct;

import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SystemConnectionMemo;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to a Direct serial
 * connection.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class Throttle extends AbstractThrottle {

    private final CommandStation tcl;

    /**
     * Constructor.
     * @param address loco address.
     * @param tc system connection traffic controller.
     * @param memo the system connection.
     */
    public Throttle(DccLocoAddress address, CommandStation tc, SystemConnectionMemo memo) {
        super(memo);
        tcl = tc;

        // cache settings.
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;
    }

    @Deprecated (since="5.13.3", forRemoval=true)
    public Throttle(DccLocoAddress address, CommandStation tc) {
        this(address, tc, jmri.InstanceManager.getDefault(SystemConnectionMemo.class));
    }

    DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address.getNumber(), address.isLongAddress(),
                getFunction(0), getFunction(1), getFunction(2), getFunction(3), getFunction(4));

        tcl.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address.getNumber(), address.isLongAddress(),
                getFunction(5), getFunction(6), getFunction(7), getFunction(8));

        tcl.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address.getNumber(), address.isLongAddress(),
                getFunction(9), getFunction(10), getFunction(11), getFunction(12));

        tcl.sendPacket(result, 1);
    }

    /**
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        int value = (int) ((127 - 1) * speed); // -1 for rescale to avoid estop
        if (value > 0) {
            value = value + 1; // skip estop
        }
        if (value > 127) {
            value = 127;       // max possible speed
        }
        if (value < 0) {
            value = 1;         // emergency stop
        }
        String step = "" + value;

        Message m = new Message(1 + step.length());
        int i = 0;  // message index counter
        if (isForward) {
            m.setElement(i++, '>');
        } else {
            m.setElement(i++, '<');
        }

        for (int j = 0; j < step.length(); j++) {
            m.setElement(i++, step.charAt(j));
        }
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
        // tcl.sendMessage(m, null);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized(this) {
            setSpeedSetting(speedSetting);  // send the command
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    @Override
    public void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(Throttle.class);

}
