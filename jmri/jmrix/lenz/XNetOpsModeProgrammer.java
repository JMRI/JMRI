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
 * @version        $Revision: 2.1 $
 */

public class XNetOpsModeProgrammer implements Programmer,XNetListener 
{

    int mAddressHigh;
    int mAddressLow;
    int progState=0;
    int value;
    jmri.ProgListener progListener = null;

    public XNetOpsModeProgrammer(int pAddress) {
	mAddressLow=LenzCommandStation.getDCCAddressLow(pAddress);
	mAddressHigh=LenzCommandStation.getDCCAddressHigh(pAddress);

        // register as a listener
        XNetTrafficController.instance().addXNetListener(~0,this);

    }

    /**
     * Send an ops-mode write request to the XPressnet.
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        XNetMessage msg=XNetMessage.getWriteOpsModeCVMsg(mAddressHigh,mAddressLow,CV,val);
	XNetTrafficController.instance().sendXNetMessage(msg,this);
        /* we need to save the programer and value so we can send messages 
        back to the screen when the programing screen when we recieve 
        something from the command station */
        progListener=p;
        value=val;
        progState=XNetProgrammer.REQUESTSENT;
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
           /* read is not yet implemented by XPressNet*/
           p.programmingOpReply(CV,jmri.ProgListener.NotImplemented);
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
           /* read is not yet implemented by XPressNet*/
           p.programmingOpReply(val,jmri.ProgListener.NotImplemented);
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


    synchronized public void message(XNetReply l) {
	if (progState == XNetProgrammer.NOTPROGRAMMING) {
           // We really don't care about any messages unless we send a 
           // request, so just ignore anything that comes in
           return;
        } else if (progState==XNetProgrammer.REQUESTSENT) {
            if(l.isOkMessage()) {
	  	  progListener.programmingOpReply(value,jmri.ProgListener.OK);
	    } else {
              /* this is an error */
              if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER &&
		((l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR ||
		  l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR))) {   
                     /* this is a communications error */
                     progListener.programmingOpReply(value,jmri.ProgListener.FailedTimeout);
	      } else if(l.getElement(0)==XNetConstants.CS_INFO &&
		        l.getElement(2)==XNetConstants.CS_NOT_SUPPORTED) {
		     	   progListener.programmingOpReply(value,jmri.ProgListener.NotImplemented);
              } else { 
                        /* this is an unknown error */
                   	progListener.programmingOpReply(value,jmri.ProgListener.UnknownError);
                   }
            progState=XNetProgrammer.NOTPROGRAMMING;
          }
	}
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetOpsModeProgrammer.class.getName());

}

/* @(#)XnetOpsModeProgrammer.java */
