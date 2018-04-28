package jmri.jmrix.dcc4pc;

import java.beans.PropertyChangeListener;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Ops mode proxy programming interface for a RailCom Reader. This
 * forwards the read request to the command station to forward on and handles
 * sending back the CV reading results from the Rail Com message
 *
 * @see jmri.Programmer
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements PropertyChangeListener, AddressedProgrammer {

    int pAddress = 0;
    boolean pLongAddress;
    int progState = 0;
    jmri.RailCom rcTag;
    int value;
    int cv;
    jmri.ProgListener progListener = null;

    static protected final int DCC4PC_PROGRAMMER_TIMEOUT = 2000;

    AddressedProgrammerManager defaultManager;
    Programmer defaultProgrammer;

    public Dcc4PcOpsModeProgrammer(boolean pLongAddress, int pAddress, AddressedProgrammerManager dp) {
        defaultManager = dp;
        defaultProgrammer = defaultManager.getAddressedProgrammer(pLongAddress, pAddress);
        this.pAddress = pAddress;
        this.pLongAddress = pLongAddress;
        rcTag = jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + pAddress);
    }

    /**
     * Send an ops-mode write request to the XPressnet.
     */
    @Override
    synchronized public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        rcTag.setExpectedCv(cv);
        progListener = p;
        defaultProgrammer.writeCV(CV, val, new ProxyProgList());
    }

    @Override
    synchronized public void readCV(int cv, ProgListener p) throws ProgrammerException {
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(cv);
        progListener = p;
        this.cv = cv;
        startLongTimer();
        defaultProgrammer.readCV(cv, new ProxyProgList());
        progListener.programmingOpReply(cv, jmri.ProgListener.OK);
    }

    static class ProxyProgList implements jmri.ProgListener {

        ProxyProgList() {
        }

        @Override
        public void programmingOpReply(int value, int status) {
            /*if(status!=NotImplemented){
             progListener.programmingOpReply(0, status);
             }
             log.debug("Actual Command station returned " + status + " " + value);*/
        }
    }

    @Override
    public void confirmCV(String cvName, int val, ProgListener p) throws ProgrammerException {
        int cvValue = Integer.parseInt(cvName);
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(cvValue);
        synchronized (this) {
            progListener = p;
        }
        this.cv = cvValue;
        defaultProgrammer.confirmCV(cvName, val, new ProxyProgList());
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        return defaultProgrammer.getSupportedModes();
    }

    @Override
    synchronized protected void timeout() {
        rcTag.removePropertyChangeListener(this);
        rcTag.setExpectedCv(-1);
        progListener.programmingOpReply(0, jmri.ProgListener.FailedTimeout);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getSource() != rcTag) {
            log.error("Unexpected source");
        }
        if (e.getPropertyName().equals("cvvalue")) {
            int repliedCv = (Integer) e.getOldValue();
            log.info("{} {}", e.getOldValue(), e.getNewValue());
            if (repliedCv == cv) {
                int newValue = (Integer) e.getNewValue();
                stopTimer();
                rcTag.removePropertyChangeListener(this);
                synchronized (this) {
                    progListener.programmingOpReply(newValue, ProgListener.OK);
                }
            } else {
                log.error("Unexpected cv {} returned, was expecting CV {}", repliedCv, cv);
            }
        }
    }

    @Override
    public boolean getLongAddress() {
        return pLongAddress;
    }

    @Override
    public int getAddressNumber() {
        return pAddress;
    }

    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammer.class);

}
