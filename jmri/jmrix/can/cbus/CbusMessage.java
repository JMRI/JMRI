/*
 * CbusMessage.java
 *
 */

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

/**
 * Class to allow use of CBUS concepts to access the underlying
 * can message
 *
 * @author          Andrew Crosland Copyright (C) 2008
 * @version         $Revision: 1.2 $
 */
public class CbusMessage {
    
    public static int getOpcode(CanMessage m) {
        return m.getElement(0);
    }
    public static int getDataLength(CanMessage m) {
        return m.getElement(0)>>5;
    }
    public static int getNodeNumber(CanMessage m) {
        if (isEvent(m))
            return m.getElement(1)*256 + m.getElement(2);
        else
            return 0;
    }
    public static int getEvent(CanMessage m) {
        if (isEvent(m))
            return m.getElement(3)*256 + m.getElement(4);
        else
            return 0;
    }
    public static int getEventType(CanMessage m) {
        if ((m.getElement(0) & 1) == 1)
            return CbusConstants.EVENT_OFF;
        else
            return CbusConstants.EVENT_ON;
    }
    public static boolean isEvent(CanMessage m) {
        if ((m.getElement(0) == 0x90) || (m.getElement(0) == 0x91))
            return true;
        else
            return false;
    }

    public static int getOpcode(CanReply r) {
        return r.getElement(0);
    }
    public static int getDataLength(CanReply r) {
        return r.getElement(0)>>5;
    }
    public static int getNodeNumber(CanReply r) {
        if (isEvent(r))
            return r.getElement(1)*256 + r.getElement(2);
        else
            return 0;
    }
    public static int getEvent(CanReply r) {
        if (isEvent(r))
            return r.getElement(3)*256 + r.getElement(4);
        else
            return 0;
    }
    public static int getEventType(CanReply r) {
        if ((r.getElement(0) & 1) == 1)
            return CbusConstants.EVENT_OFF;
        else
            return CbusConstants.EVENT_ON;
    }
    public static boolean isEvent(CanReply r) {
        if ((r.getElement(0) == 0x90) || (r.getElement(0) == 0x91))
            return true;
        else
            return false;
    }
}
