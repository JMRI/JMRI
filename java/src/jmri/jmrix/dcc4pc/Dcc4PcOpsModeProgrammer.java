/* Dcc4PcOpsModeProgrammer.java */

package jmri.jmrix.dcc4pc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import java.beans.PropertyChangeListener;

/**
 * Provides an Ops mode proxy programing interface for a RailCom Reader.
 * This forwards the read request to the command station to forward on and 
 * handles sending back the CV reading results from the Rail Com message
 *
 * @see            jmri.Programmer
 * @author         Kevin Dickerson Copyright (C) 2012
 * @version        $Revision: 17977 $
*/

public class Dcc4PcOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements PropertyChangeListener
{

    int pAddress=0;
    int progState=0;
    jmri.RailCom rcTag;
    int value;
    int cv;
    jmri.ProgListener progListener = null;

    static protected final int Dcc4PCProgrammerTimeout = 2000;
    
    jmri.ProgrammerManager defaultManager;
    Programmer defaultProgrammer;
    
    public Dcc4PcOpsModeProgrammer(boolean pLongAddress, int pAddress, jmri.ProgrammerManager dp){
        defaultManager = dp;
        defaultProgrammer = defaultManager.getAddressedProgrammer(pLongAddress, pAddress);
        this.pAddress=pAddress;
        rcTag = jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag(""+pAddress);
    }

    /**
     * Send an ops-mode write request to the XPressnet.
     */
    synchronized public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        rcTag.setExpectedCv(cv);
        progListener = p;
        defaultProgrammer.writeCV(CV, val, new ProxyProgList());
    }

    synchronized public void readCV(int cv, ProgListener p) throws ProgrammerException {
        
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(cv);
        progListener = p;
        this.cv = cv;
        startLongTimer();
        defaultProgrammer.readCV(cv, new ProxyProgList());
        progListener.programmingOpReply(cv, jmri.ProgListener.OK);
    }
    
    static class ProxyProgList implements jmri.ProgListener{
        ProxyProgList(){}
        public void programmingOpReply(int value, int status){
            /*if(status!=NotImplemented){
                progListener.programmingOpReply(0, status);
            }
            log.debug("Actual Command station returned " + status + " " + value);*/
        }
    }

    public void confirmCV(int cv, int val, ProgListener p) throws ProgrammerException {
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(cv);
        synchronized (this) {
             progListener = p;
        }
        this.cv = cv;
        defaultProgrammer.confirmCV(cv, val, new ProxyProgList());
    }

    public void setMode(int mode) {
        defaultProgrammer.setMode(mode);
    }

    public int  getMode() {
        return defaultProgrammer.getMode();
    }

    public boolean hasMode(int mode) {
        return defaultProgrammer.hasMode(mode);
    }

    synchronized protected void timeout(){
        rcTag.removePropertyChangeListener(this);
        rcTag.setExpectedCv(-1);
        progListener.programmingOpReply(0, jmri.ProgListener.FailedTimeout);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if(e.getSource()!=rcTag){
            log.error("Unexpected source");
        }
        if(e.getPropertyName().equals("cvvalue")){
            int repliedCv = (Integer)e.getOldValue();
            log.info(e.getOldValue() + " " + e.getNewValue());
            if(repliedCv==cv){
                int value = (Integer) e.getNewValue();
                stopTimer();
                rcTag.removePropertyChangeListener(this);
                synchronized(this){
                    progListener.programmingOpReply(value, ProgListener.OK);
                }
            } else {
                log.error("Unexpected cv " + repliedCv + " returned, was expecting CV " + cv);
            }
        }
    }



    // initialize logging
    static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammer.class.getName());

}

/* @(#)XnetOpsModeProgrammer.java */
