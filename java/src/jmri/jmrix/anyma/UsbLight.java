package jmri.jmrix.anyma;

import static jmri.Light.OFF;

import jmri.implementation.AbstractVariableLight;
import jmri.jmrix.anyma.AnymaDMX_SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UsbLight.java
 * <p>
 * Implementation of the Light Object for anyma dmx
 * <P>
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLight extends AbstractVariableLight {

    AnymaDMX_SystemConnectionMemo _memo = null;

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in UsbLightManager
     */
    public UsbLight(String systemName, AnymaDMX_SystemConnectionMemo memo) {
        super(systemName);
        log.info("*	UsbLight constructor called");
        _memo = memo;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in UsbLightManager
     */
    public UsbLight(String systemName, String userName, AnymaDMX_SystemConnectionMemo memo) {
        super(systemName, userName);
        log.info("*	UsbLight constructor called");
        _memo = memo;
        initializeLight(systemName);
    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    private void initializeLight(String systemName) {
        log.info("*	UsbLight.initializeLight() called");
        // Extract the Channel from the name
        mChannel = _memo.getChannelFromSystemName(systemName);
        // Set initial state
        setState(OFF);
    }

    /**
     * System dependent instance variables
     */
    int mChannel = 0;                // channel within the node

    /**
     * Set the current state of this Light. This routine requests the hardware
     * to change. If this is really a change in state of this channel (tested in
     * UsbNode), a Transmit packet will be sent before this Node is next polled.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        log.info("*	UsbLight.doNewState() called");
//        UsbNode mNode = (UsbNode) _memo.getNodeFromSystemName(getSystemName(), _memo.getTrafficController());
//        if (mNode != null) {
//            if (newState == ON) {
//                mNode.setOutputChannel(mChannel, false);
//            } else if (newState == OFF) {
//                mNode.setOutputChannel(mChannel, true);
//            } else {
//                log.warn("illegal state requested for Light: " + getSystemName());
//            }
//        }
    }

    @Override
    protected void sendIntensity(double intensity) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //TODO: set the light intensity
        log.info("* sendIntensity({})", "" + intensity);
    }

    @Override
    protected void sendOnOffCommand(int newState) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //TODO: turn off the light!
        log.info("* sendOnOffCommand({})", newState);
    }

    @Override
    protected int getNumberOfSteps() {
        return 256;
    }

    private final static Logger log = LoggerFactory.getLogger(UsbLight.class);
}
