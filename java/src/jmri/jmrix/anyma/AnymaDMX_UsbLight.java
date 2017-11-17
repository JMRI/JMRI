package jmri.jmrix.anyma;

import static jmri.Light.OFF;

import jmri.implementation.AbstractVariableLight;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnymaDMX_UsbLight.java
 * <p>
 * Implementation of the Light Object for anyma dmx
 * <P>
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */
public class AnymaDMX_UsbLight extends AbstractVariableLight {

    AnymaDMX_SystemConnectionMemo _memo = null;


    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in UsbLightManager
     */
    public AnymaDMX_UsbLight(String systemName, AnymaDMX_SystemConnectionMemo memo) {
        super(systemName);
        log.debug("*    UsbLight constructor called");
        _memo = memo;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * @param systemName the system name
     * @param userName   the user name
     * @param memo       the memo 'systemName' was previously validated in
     *                   UsbLightManager
     */
    public AnymaDMX_UsbLight(String systemName, String userName, AnymaDMX_SystemConnectionMemo memo) {
        super(systemName, userName);
        log.debug("*    UsbLight constructor called");
        _memo = memo;
        initializeLight(systemName);
    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    private void initializeLight(String systemName) {
        log.debug("*    UsbLight.initializeLight() called");
        // Extract the Channel from the name
        mChannel = _memo.getChannelFromSystemName(systemName) - 1;
        // Set initial state
        setState(OFF);
    }

    /**
     * System dependent instance variables
     */
    private int mChannel = 0;                // channel within the node (0-511)

    /**
     * Set the current state of this Light. This routine requests the hardware
     * to change. If this is really a change in state of this channel (tested in
     * UsbNode), a Transmit packet will be sent before this Node is next polled.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        log.debug("*    UsbLight.doNewState({}, {}) called", oldState, newState);
        AnymaDMX_TrafficController trafficController = _memo.getTrafficController();
        if (trafficController != null) {
            if (newState == ON) {
                trafficController.setChannelValue(mChannel, (byte) 0xFF);
            } else if (newState == OFF) {
                trafficController.setChannelValue(mChannel, (byte) 0x00);
            } else {
                log.warn("illegal state requested for Light: " + getSystemName());
            }
        }
    }

    @Override
    protected void sendIntensity(double intensity) {
        log.debug("*    sendIntensity({})", "" + intensity);
        AnymaDMX_TrafficController trafficController = _memo.getTrafficController();
        if (trafficController != null) {
            byte value = (byte) MathUtil.pin(intensity * 255.0, 0.0, 255.0);
            trafficController.setChannelValue(mChannel, value);
        }
    }

    @Override
    protected void sendOnOffCommand(int newState) {
        log.debug("*    sendOnOffCommand({})", newState);
        AnymaDMX_TrafficController trafficController = _memo.getTrafficController();
        if (trafficController != null) {
            if (newState == ON) {
                trafficController.setChannelValue(mChannel, (byte) 0xFF);
            } else if (newState == OFF) {
                trafficController.setChannelValue(mChannel, (byte) 0x00);
            } else {
                log.warn("illegal state requested for Light: " + getSystemName());
            }
        }
    }

    @Override
    protected int getNumberOfSteps() {
        return 256;
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_UsbLight.class);
}
