package jmri.jmrix.nce;

import jmri.NmraPacket;
import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NceLight.java
 *
 * Implementation of the Light Object for NCE
 * <p>
 * Based in part on SerialLight.java
 *
 * @author Dave Duchamp Copyright (C) 2010
 */
public class NceLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in NceLightManager
     * @param systemName system name for light
     * @param tc traffic controller for connection
     * @param mgr LightManager for light
     */
    public NceLight(String systemName, NceTrafficController tc, NceLightManager mgr) {
        super(systemName);
        this.tc = tc;
        this.mgr = mgr;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in NceLightManager
     * @param systemName system name for light
     * @param userName userName for light
     * @param tc traffic controller for connection
     * @param mgr LightManager for light
     */
    public NceLight(String systemName, String userName, NceTrafficController tc, NceLightManager mgr) {
        super(systemName, userName);
        this.tc = tc;
        this.mgr = mgr;
        initializeLight(systemName);
    }

    transient NceTrafficController tc;
    NceLightManager mgr;

    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        mBit = mgr.getBitFromSystemName(systemName);
        // Set initial state
        setState(OFF);
    }

    int mBit = 0;                // address bit

    /**
     * Set the current state of this Light This routine requests the hardware to
     * change.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        boolean state = true;
        if (newState == OFF) {
            state = false;
        }
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {

            byte[] bl = NceBinaryCommand.accDecoder(mBit, state);

            if (log.isDebugEnabled()) {
                log.debug("Command: "
                        + Integer.toHexString(0xFF & bl[0])
                        + " " + Integer.toHexString(0xFF & bl[1])
                        + " " + Integer.toHexString(0xFF & bl[2])
                        + " " + Integer.toHexString(0xFF & bl[3])
                        + " " + Integer.toHexString(0xFF & bl[4]));
            }

            NceMessage m = NceMessage.createBinaryMessage(tc, bl);

            tc.sendNceMessage(m, null);

        } else {

            byte[] bl = NmraPacket.accDecoderPkt(mBit, state);

            if (log.isDebugEnabled()) {
                log.debug("packet: "
                        + Integer.toHexString(0xFF & bl[0])
                        + " " + Integer.toHexString(0xFF & bl[1])
                        + " " + Integer.toHexString(0xFF & bl[2]));
            }

            NceMessage m = NceMessage.sendPacketMessage(tc, bl);

            tc.sendNceMessage(m, null);
        }

    }

    private final static Logger log = LoggerFactory.getLogger(NceLight.class);
}
