package jmri.jmrix.dcc4pc;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.RailCom;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provides an Ops mode proxy programming interface for a RailCom Reader. This
 * forwards the read request to the command station to forward on and handles
 * sending back the CV reading results from the RailCom message
 *
 * @see jmri.Programmer
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements PropertyChangeListener, AddressedProgrammer {

    int pAddress = 0;
    boolean pLongAddress;
    int progState = 0;
    RailCom rcTag;
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
        rcTag = (RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("" + pAddress);
    }

    /** 
     * {@inheritDoc}
     *
     * Send an ops-mode write request to the XPressnet.
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        rcTag.setExpectedCv(CV);
        progListener = p;
        defaultProgrammer.writeCV(CVname, val, new ProxyProgList());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        rcTag.addPropertyChangeListener(this);
        rcTag.setExpectedCv(CV);
        progListener = p;
        this.cv = CV;
        startLongTimer();
        defaultProgrammer.readCV(CVname, new ProxyProgList());
        notifyProgListenerEnd(progListener, CV, jmri.ProgListener.OK);  // this call seems seriously misplaced; is it an error?
    }

    static class ProxyProgList implements jmri.ProgListener {

        ProxyProgList() {
        }

        @Override
        public void programmingOpReply(int value, int status) {
            /*if(status!=NotImplemented){
                notifyProgListenerEnd(progListener, 0, status);
             }
             log.debug("Actual Command station returned " + status + " " + value);*/
        }
    }

    /** 
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     *
     * Types implemented here.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        return defaultProgrammer.getSupportedModes();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        rcTag.removePropertyChangeListener(this);
        rcTag.setExpectedCv(-1);
        notifyProgListenerEnd(progListener, 0, ProgListener.FailedTimeout);
    }

    /** 
     * {@inheritDoc}
     */
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
                    notifyProgListenerEnd(progListener, newValue, ProgListener.OK);
                }
            } else {
                log.error("Unexpected cv {} returned, was expecting CV {}", repliedCv, cv);
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return pLongAddress;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return pAddress;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcOpsModeProgrammer.class);

}
