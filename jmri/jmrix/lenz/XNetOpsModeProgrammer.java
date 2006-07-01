/* XNetOpsModeProgrammer.java */

package jmri.jmrix.lenz;

import java.beans.*;

import jmri.*;

/**
 * Provides an Ops mode programing interface for XPressNet
 * Currently only Byte mode is implemented, though XPressNet also supports
 * bit mode writes for POM
 *
 * @see            jmri.Programmer
 * @author         Paul Bender Copyright (C) 2003
 * @version        $Revision: 1.1 $
 */

public class XNetOpsModeProgrammer implements Programmer,XNetListener 
{

    int mAddressHigh;
    int mAddressLow;
    public XNetOpsModeProgrammer(int pAddress) {
        if(pAddress < 100)
        {
                mAddressHigh=0x00;
                mAddressLow=pAddress;
        }
        else
        {
                int temp=pAddress + 0xC000;
                temp=temp & 0xFF00;
                temp=temp/256;
                mAddressHigh=temp;
                temp=pAddress+0xC000;
                temp = temp &0x00FF;
                mAddressLow=temp;
        }
    }

    /**
     * Send an ops-mode write request to the XPressnet.
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        XNetMessage msg=XNetTrafficController.instance().getCommandStation().getWriteOpsModeCVMsg(mAddressHigh,mAddressLow,CV,val);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
        /* In the long run, this is probably NOT what we want to do, but 
        we're writing an XPressNet message that returns no feedback if
        the operation fails */
        p.programmingOpReply(val,jmri.ProgListener.OK);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
           /* read is not yet implemented by XPressNet*/
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
           /* read is not yet implemented by XPressNet*/
    }

    public void setMode(int mode) {
        if (mode!=Programmer.OPSBYTEMODE) {
            reportBadMode(mode);
        }
    }

    void reportBadMode(int mode) {
        log.error("Can't switch to mode "+mode);
    }

    public int  getMode() {
        return Programmer.OPSBYTEMODE;
    }

    public boolean hasMode(int mode) {
        return (mode==Programmer.OPSBYTEMODE);
    }

    /**
     * Can this ops-mode programmer read back values?  For now, no,
     * but maybe later.
     * @return always false for now
     */
    public boolean getCanRead() {
        return false;
    }

    public String decodeErrorCode(int i) {
                    return("");
    }

    public void addPropertyChangeListener(PropertyChangeListener p) {
    }

    public void removePropertyChangeListener(PropertyChangeListener p) {
    }


    public void message(XNetMessage l) {
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetOpsModeProgrammer.class.getName());

}

/* @(#)XnetOpsModeProgrammer.java */
