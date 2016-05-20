// AcelaLight.java

package jmri.jmrix.acela;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractLight;

/**
 * AcelaLight.java
 *
 * Implementation of the Light Object for Acela
 * <P>
 *  Based in part on SerialTurnout.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version     $Revision$
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in AcelaLightManager
     */
    public AcelaLight(String systemName) {
        super(systemName);
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in AcelaLightManager
     */
    public AcelaLight(String systemName, String userName) {
        super(systemName, userName);
        initializeLight(systemName);
    }
        
    /**
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Save system name
        mSystemName = systemName;
        // Extract the Bit from the name
        mBit = AcelaAddress.getBitFromSystemName(systemName);
        // Set initial state
        AcelaNode mNode = AcelaAddress.getNodeFromSystemName(mSystemName);

        if (mNode!=null) {
            int initstate;
            int initbit;
            initbit = mBit - mNode.getStartingOutputAddress();
            initstate = mNode.getOutputInit(initbit);
            if (initstate == 1) {
                setState( ON );
            } else {
                setState( OFF );
            }
        }
    }

    /**
     *  System dependent instance variables
     */
    String mSystemName = "";     // system name 
    int mBit = -1;                // global address from 0

    /**
     *  Return the current state of this Light
     */
    public int getState() { return mState; }

    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     *     If this is really a change in state of this 
     *         bit (tested in AcelaNode), a Transmit packet
     *         will be sent before this Node is next polled.
     */
    public void setState(int newState) {
        AcelaNode mNode = AcelaAddress.getNodeFromSystemName(mSystemName);

        if (mNode!=null) {
            if (newState==ON) {
                mNode.setOutputBit(mBit,true);
            } else if (newState==OFF) {
                mNode.setOutputBit(mBit,false);
            } else {
                log.warn("illegal state requested for Light: "+getSystemName());
            }
        }
	
        if (newState!=mState) {
            int oldState = mState;
            mState = newState;
            
            // notify listeners, if any
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
	}
    }

    static Logger log = LoggerFactory.getLogger(AcelaLight.class.getName());
}

/* @(#)AcelaLight.java */
