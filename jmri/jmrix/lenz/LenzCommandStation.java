/*
 * LenzCommandStation.java
 */

package jmri.jmrix.lenz;


/**
 * Defines standard operations for Dcc command stations.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.7 $
 */
public class LenzCommandStation implements jmri.jmrix.DccCommandStation {
    
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
     * Provides the version string returned during the initial check.
     * This function is not yet implemented...
     **/
    public String getVersionString() { return "<unknown>"; }
    
    /**
     * Remember whether or not in service mode
     **/
    boolean mInServiceMode = false;
    
    /**
     * Generate a message to change turnout state
     */
    public XNetMessage getTurnoutCommandMsg(int pNumber, boolean pClose,
                                            boolean pThrow, boolean pOn) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0,0x52);
        
        // compute address byte fields
        int hiadr = (pNumber-1)/4;
        int loadr = ((pNumber-1)-hiadr*4)*2;
        
        // load On/Off with on
        loadr |= 0x10;
        if (!pOn) loadr |= 0x40;
        if (pThrow) loadr |= 0x01;
        
        // we don't know how to command both states right now!
        if (pClose & pThrow)
            log.error("XPressNet turnout logic can't handle both THROWN and CLOSED yet");
        
        // store and send
        l.setElement(1,hiadr);
        l.setElement(2,loadr);
        
        return l;
    }
    
    /**
     * If this is a turnout-type message, return address. Otherwise
     * return -1.
     * Note we only identify the command now; the reponse to a
     * request for status is not yet seen here.
     */
    public int getTurnoutMsgAddr(XNetMessage pMsg) {
        if (isTurnoutCommand(pMsg)) {
            int a1 = pMsg.getElement(1);
            int a2 = pMsg.getElement(2);
            return (((a1 & 0xff) * 4) + (a2 & 0x6)/2 + 1);
        }
        else return -1;
    }

    /**
     * Is this a command to change turnout state?
     */
    public boolean isTurnoutCommand(XNetMessage pMsg) {
        return pMsg.getOpCode()==0x05;
    }
    /**
     * If this is a throttle-type message, return address. Otherwise
     * return -1.
     * Note we only identify the command now; the reponse to a
     * request for status is not yet seen here.
     */
    public int getThrottleMsgAddr(XNetMessage pMsg) {
        if (isThrottleCommand(pMsg)) {
            int a1 = pMsg.getElement(3);
            int a2 = pMsg.getElement(4);
            return (((a1 & 0xff) * 4) + (a2 & 0x6)/2 + 1);
        }
        else return -1;
    }
    
    /**
     * Is this a throttle message?
     */
    public boolean isThrottleCommand(XNetMessage pMsg) {
        int message=pMsg.getOpCode();
        if( message==0x83 || message==0x84 || message==0xA3 || 
            message == 0xA4 ||message == 0xE2 || message == 0xE3 || 
           message == 0xE3) return true;
        return false;
    }
    
    // start of programming messages
    public XNetMessage getServiceModeResultsMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, 0x21);
        m.setElement(1, 0x10);
        return m;
    }
    
    public XNetMessage getExitProgModeMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, 0x21);
        m.setElement(1, 0x81);
        return m;
    }
    
    public XNetMessage getReadPagedCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, 0x22);
        m.setElement(1, 0x14);
        m.setElement(2, cv);
        return m;
    }
    
    public XNetMessage getReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, 0x22);
        m.setElement(1, 0x15);
        m.setElement(2, cv);
        return m;
    }
    
    public XNetMessage getWritePagedCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, 0x23);
        m.setElement(1, 0x17);
        m.setElement(2, cv);
        m.setElement(3, val);
        return m;
    }
    
    public XNetMessage getWriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, 0x23);
        m.setElement(1, 0x16);
        m.setElement(2, cv);
        m.setElement(3, val);
        return m;
    }
    
    public XNetMessage getReadRegisterMsg(int reg) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, 0x22);
        m.setElement(1, 0x11);
        m.setElement(2, reg);
        return m;
    }
    
    public XNetMessage getWriteRegisterMsg(int reg, int val) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, 0x23);
        m.setElement(1, 0x12);
        m.setElement(2, reg);
        m.setElement(3, val);
        return m;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LenzCommandStation.class.getName());
    
}


/* @(#)LenzCommandStation.java */
