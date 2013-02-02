// CbusProgrammer.java

package jmri.jmrix.can.cbus;

import org.apache.log4j.Logger;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficController;


/**
 * Implements the jmri.Programmer interface via commands for CBUS.
 *
 * @author			Bob Jacobsen  Copyright (C) 2008
 * @version			$Revision$
 */
public class CbusProgrammer extends AbstractProgrammer implements CanListener {

    public CbusProgrammer() { 
        Exception e = new Exception("Dummy method called");
        e.printStackTrace();
        // throw e;
    }
    
    public CbusProgrammer(int nodenumber, TrafficController tc) {
        this.nodenumber = nodenumber;
        // need a longer LONG_TIMEOUT
        LONG_TIMEOUT=180000;
        this.tc=tc;
    }
    
    TrafficController tc;

    int nodenumber;
    /**
     * Switch to a new programming mode.  Note that CBUS has only
     * one mode. If you attempt to switch to
     * any others, the new mode will set & notify, then
     * set back to the original.  This lets the listeners
     * know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        if (mode != Programmer.CBUSNODEVARMODE) {
            notifyPropertyChange("Mode", Programmer.CBUSNODEVARMODE, mode);
            notifyPropertyChange("Mode", mode, Programmer.CBUSNODEVARMODE);
        }
    }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged or register mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.CBUSNODEVARMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }
    public int getMode() { return Programmer.CBUSNODEVARMODE; }

    public boolean getCanRead() { return true; }

    // notify property listeners - see AbstractProgrammer for more

    @SuppressWarnings("unchecked")
	protected void notifyPropertyChange(String name, int oldval, int newval) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this) {
            v = (Vector<PropertyChangeListener>) propListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    // members for handling the programmer interface

    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    boolean  programmerReadOperation = false;  // true reading, false if writing
    int operationValue;	 // remember the value being read/written for confirmative reply
    int operationVariableNumber; // remember the variable number being read/written

    // programming interface
    synchronized public void writeCV(int varnum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("write "+varnum+" listens "+p);
        useProgrammer(p);
        programmerReadOperation = false;
        // set state
        progState = NOTPROGRAMMING;  // no reply to write
        operationValue = val;
        operationVariableNumber = varnum;

        // format and send the write message.
        int[] frame = new int[]{0x96, (nodenumber/256)&0xFF, nodenumber&0xFF, operationVariableNumber&0xFF, operationValue&0xFF};
        CanMessage m = new CanMessage(frame, tc.getCanid());
        tc.sendCanMessage(m, this);

        // no reply, so don't want for reply
        progState = NOTPROGRAMMING;
        notifyProgListenerEnd(operationValue, jmri.ProgListener.OK);
    }

    synchronized public void confirmCV(int varnum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(varnum, p);
    }

    synchronized public void readCV(int varnum, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("readCV "+varnum+" listens "+p);
        useProgrammer(p);
        programmerReadOperation = true;

        progState = COMMANDSENT;
        operationVariableNumber = varnum;

        // start the error timer
        startLongTimer();

        // format and send the read message.
        int[] frame = new int[]{0x71, (nodenumber/256)&0xFF, nodenumber&0xFF, operationVariableNumber&0xFF};
        CanMessage m = new CanMessage(frame, tc.getCanid());
        tc.sendCanMessage(m, this);
    }

    private jmri.ProgListener programmerUser = null;  // null if don't have one

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (programmerUser != null && programmerUser != p) {
            if (log.isDebugEnabled()) log.debug("programmer already in use by "+programmerUser);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            programmerUser = p;
            return;
        }
    }

    public void message(CanMessage m) {
        log.debug("message received and ignored: "+m.toString());
    }

    synchronized public void reply(jmri.jmrix.can.CanReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for reply
            if (m.getElement(0)==0x97 && 
                        (m.getElement(1) == ((nodenumber/256)&0xFF)) && 
                        (m.getElement(2) == (nodenumber&0xFF)) ) {
                // this is the OK reply
                // see why waiting
                if (programmerReadOperation) {
                // read was in progress - get return value
                    operationValue = m.getElement(3)&0xFF;
                }
                // if this was a read, we retrieved the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(operationValue, jmri.ProgListener.OK);
            }
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            cleanup();
            notifyProgListenerEnd(operationValue, jmri.ProgListener.FailedTimeout);
        }
    }

    /**
     * Internal method to send a cleanup message (if needed) on timeout.
     * <P>
     * Here, it sends a request to exit from programming mode.  But
     * subclasses, e.g. ops mode, may redefine that.
     */
    void cleanup() {
        // tc.sendEasyDccMessage(EasyDccMessage.getExitProgMode(), this);
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = programmerUser;
        programmerUser = null;
        temp.programmingOpReply(value, status);
    }
    
    static Logger log = Logger.getLogger(CbusProgrammer.class.getName());
}


/* @(#)CbusProgrammer.java */
