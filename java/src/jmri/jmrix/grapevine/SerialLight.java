package jmri.jmrix.grapevine;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light interface using Grapevine signal ports.
 * <p>
 * The "On" state results in sending a "green" setting to the hardware port;
 * the "Off" state results in sending a "dark" setting to the hardware.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 */
public class SerialLight extends AbstractLight {

    private GrapevineSystemConnectionMemo memo = null;

    /**
     * Create a Light object, with only system name.
     *
     * @param systemName system name including prefix, previously validated in SerialLightManager
     * @param _memo the associated SystemConnectionMemo
     */
    public SerialLight(String systemName, GrapevineSystemConnectionMemo _memo) {
        super(systemName);
        memo = _memo;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     *
     * @param systemName system name including prefix, previously validated in SerialLightManager
     * @param userName free form name
     * @param _memo the associated SystemConnectionMemo
     */
    public SerialLight(String systemName, String userName, GrapevineSystemConnectionMemo _memo) {
        super(systemName, userName);
        memo = _memo;
        initializeLight(systemName);
    }

    /**
     * Set up system dependent instance variables and set system independent
     * instance variables to default values.
     * <p>
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName, memo.getSystemPrefix()); // bit one is address zero
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num % 100) - 1; // 0-23
        bank = (num / 100) - 1;  // 0 - 3

        // Set initial state to OFF internally and on layout
        setState(OFF);
    }

    /**
     * System dependent instance variables
     */
    int output;         // output connector number, 0-23
    int bank;           // bank number, 0-3

    /**
     * Set the current state of this Light. This routine requests the hardware to
     * change. If this is really a change in state of this bit (tested in
     * SerialNode), a Transmit packet will be sent before this Node is next
     * polled.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        SerialNode mNode = SerialAddress.getNodeFromSystemName(getSystemName(), memo.getTrafficController());
        if (mNode != null) {
            if (newState == ON) {
                sendMessage(true);
            } else if (newState == OFF) {
                sendMessage(false);
            } else {
                log.warn("illegal state requested for Light: {}", getSystemName());
            }
        }
    }

    protected void sendMessage(boolean on) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(getSystemName(), memo.getTrafficController());
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for {}, command ignored", getSystemName());
            return;
        }
        boolean high = (output >= 12);
        int tOut = output;
        if (high) {
            tOut = output - 12;
        }
        if ((bank < 0) || (bank > 4)) {
            log.error("invalid bank {}  for Light {}", bank, getSystemName());
            bank = 0;
        }
        SerialMessage m = new SerialMessage(high ? 8 : 4);
        int i = 0;
        if (high) {
            m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 1
            m.setElement(i++, 122);   // shift command
            m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 2
            m.setElement(i++, 0x10);  // bank 1
            m.setParity(i - 4);
        }
        m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 1
        m.setElement(i++, (tOut << 3) | (on ? 0 : 4));  // on is green, off is dark
        m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 2
        m.setElement(i++, bank << 4); // bank is most significant bits
        m.setParity(i - 4);
        memo.getTrafficController().sendSerialMessage(m, null);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLight.class);

}
