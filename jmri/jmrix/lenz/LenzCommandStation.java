/*
 * LenzCommandStation.java
 */

package jmri.jmrix.lenz;


/**
 * Defines the standard/common routines used in multiple classes related 
 * to the a Lenz Command Station, on an XPressNet network.
 *
 * @author			Bob Jacobsen Copyright (C) 2001 Portions by Paul Bender Copyright (C) 2003
 * @version			$Revision: 2.3 $
 */
public class LenzCommandStation implements jmri.jmrix.DccCommandStation {

    /* The First group of routines is for obtaining the Software and
       hardware version of the Command station */

    /**
     *  We need to add a few data members for saving the version 
     *  information we get from the layout.
     **/

     private int cmdStationType = -1;
     private float cmdStationSoftwareVersion = -1;

    /**
     * return the CS Type
     **/
     public int getCommandStationType() { return cmdStationType; }

    /**
     * set the CS Type
     **/
     public void setCommandStationType(int t) { cmdStationType = t; }

    /**
     * Set the CS type based on an XPressNet Message
     **/
     public void setCommandStationType(XNetReply l) {
       if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
       {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
              {
                cmdStationType=l.getElement(3);
              }
       }
    }

    /**
     * return the CS Software Version
     **/
     public float getCommandStationSoftwareVersion() { return cmdStationSoftwareVersion; }

    /**
     * set the CS Software Version
     **/
     public void setCommandStationSoftwareVersion(float v) { cmdStationSoftwareVersion = v; }

    /**
     * Set the CS Software Version based on an XPressNet Message
     **/
     public void setCommandStationSoftwareVersion(XNetReply l) {
       if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
       {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
              {
                cmdStationSoftwareVersion=(l.getElementBCD(2).floatValue())/10;
              }
       }
    }

    /**
     * Generate the message to request the Command Station 
     * Hardware/Software Version
     **/
     public XNetMessage getCSVersionRequestMessage() {
	XNetMessage msg=new XNetMessage(3);   
        msg.setElement(0,XNetConstants.CS_REQUEST);
        msg.setElement(1,XNetConstants.CS_VERSION);
        msg.setParity(); // Set the parity bit
        return msg;
     } 

    /**
     * Provides the version string returned during the initial check.
     * This function is not yet implemented...
     **/
    public String getVersionString() { return "<unknown>"; }

    /**
     * Generate the message to request the Command Station 
     * Status
     **/
     public XNetMessage getCSStatusRequestMessage() {
	XNetMessage msg=new XNetMessage(3);   
        msg.setElement(0,XNetConstants.CS_REQUEST);
        msg.setElement(1,XNetConstants.CS_STATUS);
        msg.setParity(); // Set the parity bit
        return msg;
     } 

    /**
     * Generate the message to set the Command Station 
     * to Auto or Manual restart mode.
     **/
     public XNetMessage getCSAutoStartMessage(boolean autoMode) {
	XNetMessage msg=new XNetMessage(4);   
        msg.setElement(0,XNetConstants.CS_SET_POWERMODE);
        msg.setElement(1,XNetConstants.CS_SET_POWERMODE);
	if(autoMode) msg.setElement(2,XNetConstants.CS_POWERMODE_AUTO);
	   else msg.setElement(2,XNetConstants.CS_POWERMODE_MANUAL);
        msg.setParity(); // Set the parity bit
        return msg;
     } 
    
    /* 
     * The next group of messages has to do with determining if the
     * command station has, and is currently in service mode 
     */

    /**
     * Lenz does use a service mode
     */
    public boolean getHasServiceMode() {return true;}


    /**
     * If this command station has a service mode, is the command
     * station currently in that mode?
     */
    public boolean getInServiceMode() { return mInServiceMode; }
    
    /**
     * Remember whether or not in service mode
     **/
    boolean mInServiceMode = false;

    // A few utility functions
    
    /**
     * Get the Lower byte of a locomotive address from the decimal 
     * locomotive address 
     */
     public static int getDCCAddressLow(int address) {
        /* For addresses below 100, we just return the address, otherwise,
        we need to return the upper byte of the address after we add the
        offset 0xC000. The first address used for addresses over 99 is 0xC064*/
        if(address < 100)
        {
                return(address);
        }
        else
        {
                int temp=address + 0xC000;
                temp=temp & 0x00FF;
                return temp;
        }
     }

    /**
     * Get the Upper byte of a locomotive address from the decimal 
     * locomotive address 
     */
     public static int getDCCAddressHigh(int address) {
        /* this isn't actually the high byte, For addresses below 100, we
        just return 0, otherwise, we need to return the upper byte of the
        address after we add the offset 0xC000 The first address used for
        addresses over 99 is 0xC064*/
        if(address < 100)
        {
                return(0x00);
        }
        else
        {
                int temp=address + 0xC000;
                temp=temp & 0xFF00;
                temp=temp/256;
                return temp;
        }

     }

    /*
     * We need to register for logging
     */
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LenzCommandStation.class.getName());
    
}


/* @(#)LenzCommandStation.java */
