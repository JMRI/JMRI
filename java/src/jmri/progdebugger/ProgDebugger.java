package jmri.progdebugger;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrix.loconet.hexfile.HexFileFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debugging implementation of Programmer interface.
 * <p>
 * Note that running a simulated LocoNet connection, {@link HexFileFrame#configure()} will substitute the
 * {@link jmri.progdebugger.ProgDebugger} instead of the {@link jmri.jmrix.loconet.LnOpsModeProgrammer},
 * overriding {@link #readCV(String, ProgListener)} and {@link #writeCV(String, int, ProgListener)}.
 * <p>
 * Remembers writes, and returns the last written value when a read to the same
 * CV is made.
 * <p>
 * Only supports the DCC single-number address space, should be updated to handle
 * any string address. As a temporary fix we simply discard the first part of any CV name
 * containing "." and use the rest.
 * TODO Fully support numberformat "113.12" in ProgDebugger (used in LOCONETLNCVMODE and LOCONETBDOPSWMODE)
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007, 2013
 */
public class ProgDebugger extends PropertyChangeSupport implements AddressedProgrammer {

    public ProgDebugger() {
        mode = ProgrammingMode.PAGEMODE;
    }

    public ProgDebugger(boolean pLongAddress, int pAddress) {
        longAddr = pLongAddress;
        address = pAddress;
        mode = ProgrammingMode.OPSBITMODE;
    }

    // write CV is recorded for later use
    private int _lastWriteVal = -1;
    private int _lastWriteCv = -1;
    //private String cvNumPrefix; // TODO use part 0 of composite CVnames for simulated replies?

    public int lastWrite() {
        return _lastWriteVal;
    }

    public int lastWriteCv() {
        return _lastWriteCv;
    }

    public int nOperations = 0;

    /**
     * Reset the CV to a value so one can detect if it's been written.
     * <p>
     * Does not change the "lastWrite" and "lastWriteCv" results.
     *
     * @param cv  the CV to reset
     * @param val the value
     */
    public void resetCv(int cv, int val) {
        mValues.put(cv, val);
    }

    /**
     * Get the CV value directly, without going through the usual indirect
     * protocol. Used, for example, while testing.
     * <p>
     * Does not change the "lastRead" and "lastReadCv" results.
     *
     * @param cv the CV to get
     * @return the value or -1
     */
    public int getCvVal(int cv) {
        // try to get something from hash table
        Integer saw = (mValues.get(cv));
        if (saw != null) {
            return saw;
        }
        log.warn("CV {} has no defined value", cv);
        return -1;
    }

    /**
     * See if a CV has been written.
     *
     * @param cv the CV to check
     * @return true if written, false otherwise
     */
    public boolean hasBeenWritten(int cv) {
        Integer saw = (mValues.get(cv));
        return (saw != null);
    }

    /**
     * Clear written status.
     *
     * @param cv the CV to clear
     */
    public void clearHasBeenWritten(int cv) {
        mValues.remove(cv);
    }

    // write CV values are remembered for later reads
    Hashtable<Integer, Integer> mValues = new Hashtable<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String decodeErrorCode(int i) {
        log.debug("decoderErrorCode {}", i);
        return "error " + i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV;
        // Check CVname contents for int parsing
        if (CVname.contains(".")) {
            String[] parts = CVname.split("\\.");
            //cvNumPrefix = parts[0];
            CVname = parts[1];
            // in LocoNet LOCONETLNCVMODE and LOCONETBDOPSWMODE the CV value (string) eg. "25.2"
            // contains a first part for Article ID/Board typeword. cvNumPrefix is discarded during debug/simulation.
            // See jmri.jmrix.loconet.LnOpsModeProgrammer.writeCV()
        }
        CV = Integer.parseInt(CVname);
        nOperations++;
        final ProgListener m = p;
        // log out the request
        log.info("write CV: {} to: {} mode: {}", CV, val, getMode());
        _lastWriteVal = val;
        _lastWriteCv = CV;
        // save for later retrieval
        mValues.put(CV, val);

        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
            ProgListener l = m;

            @Override
            public void run() {
                log.debug("write CV reply");
                if (l != null) {
                    notifyProgListenerEnd(l, val, 0);
                }
            }  // 0 is OK status
        };
        sendReturn(r);
    }

    // read CV values
    // note that the hashTable will be used if the CV has been written
    private int _nextRead = 123;

    public void nextRead(int r) {
        _nextRead = r;
    }

    private int _lastReadCv = -1;

    public int lastReadCv() {
        return _lastReadCv;
    }

    boolean confirmOK;  // cached result of last compare

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        final ProgListener m = p;

        nOperations++;
        // guess by comparing current value in val to has table
        Integer saw = mValues.get(CV);
        int result; // what was read
        if (saw != null) {
            result = saw;
            confirmOK = (result == val);
            log.info("confirm CV: {} mode: {} will return {} pass: {}", CV, getMode(), result, confirmOK);
        } else {
            result = -1;
            confirmOK = false;
            log.info("confirm CV: {} mode: {} will return -1 pass: false due to no previous value", CV, getMode());
        }
        _lastReadCv = CV;
        // return a notification via the queue to ensure end
        final int returnResult = result;  // final to allow passing to inner class
        Runnable r = new Runnable() {
            ProgListener l = m;
            int result = returnResult;

            @Override
            public void run() {
                log.debug("read CV reply");
                if (confirmOK) {
                    notifyProgListenerEnd(l, val, ProgListener.OK);
                } else {
                    notifyProgListenerEnd(l, result, ProgListener.ConfirmFailed);
                }
            }
        };
        sendReturn(r);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV;
        // Check CVname contents for int parsing
        if (CVname.contains(".")) {
            String[] parts = CVname.split("\\.");
            //cvNumPrefix = parts[0];
            CVname = parts[1];
            // in LocoNet LOCONETLNCVMODE and LOCONETBDOPSWMODE the CV value (string) e.g. "113.12"
            // contains a first part for Article ID/Board typeword. cvNumPrefix is discarded during debug/simulation.
            // See jmri.jmrix.loconet.LnOpsModeProgrammer.writeCV()
        }
        CV = Integer.parseInt(CVname);
        final ProgListener m = p;
        _lastReadCv = CV;
        nOperations++;

        int readValue = _nextRead;
        // try to get something from hash table
        Integer saw = mValues.get(CV);
        if (saw != null) {
            readValue = saw;
        }

        log.info("read CV: {} mode: {} will read {}", CV, getMode(), readValue);

        final int returnValue = readValue;
        // return a notification via the queue to ensure end
        Runnable r = new Runnable() {
            int retval = returnValue;
            ProgListener l = m;

            @Override
            public void run() {
                log.debug("read CV reply");
                notifyProgListenerEnd(l, retval, 0);
            }  // 0 is OK status
        };
        sendReturn(r);

    }

    // handle mode
    protected ProgrammingMode mode;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMode(ProgrammingMode m) {
        log.debug("Setting mode from {} to {}", mode, m);
        if (getSupportedModes().contains(m)) {
            ProgrammingMode oldMode = mode;
            mode = m;
            firePropertyChange("Mode", oldMode, m);
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ProgrammingMode getMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        if (address >= 0) {
            // addressed programmer
            return Arrays.asList(ProgrammingMode.OPSBITMODE,
                    ProgrammingMode.OPSBYTEMODE);
        } else {
            // global programmer
            return Arrays.asList(ProgrammingMode.PAGEMODE,
                    ProgrammingMode.DIRECTBITMODE,
                    ProgrammingMode.DIRECTBYTEMODE,
                    ProgrammingMode.DIRECTMODE);
        }
    }
    /**
     * By default, the highest test CV is 256 so that we can test composite
     * operations
     */
    int writeLimit = 256;
    int readLimit = 256;

    public void setTestReadLimit(int lim) {
        readLimit = lim;
    }

    public void setTestWriteLimit(int lim) {
        writeLimit = lim;
    }

    @Override
    public boolean getCanRead() {
        log.debug("getCanRead() returns true");
        return true;
    }

    @Override
    public boolean getCanRead(String addr) {
        log.debug("getCanRead({}) returns {}", addr, Integer.parseInt(addr) <= readLimit);
        return Integer.parseInt(addr) <= readLimit;
    }

    @Override
    public boolean getCanWrite() {
        log.debug("getCanWrite() returns true");
        return true;
    }

    @Override
    public boolean getCanWrite(String addr) {
        log.debug("getCanWrite({}) returns {}", addr, Integer.parseInt(addr) <= writeLimit);
        return Integer.parseInt(addr) <= writeLimit;
    }

    /**
     * {@inheritDoc}
     * <p>
     * By default, say that no verification is done.
     * @return Always WriteConfirmMode.NotVerified
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.NotVerified; }

    boolean longAddr = true;

    @Override
    public boolean getLongAddress() {
        return true;
    }

    int address = -1;

    @Override
    public int getAddressNumber() {
        return address;
    }

    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    static final boolean IMMEDIATERETURN = false;
    static final int DELAY = 10;

    /**
     * Arrange for the return to be invoked on the Swing thread.
     *
     * @param run the Runnable
     */
    void sendReturn(Runnable run) {
        if (IMMEDIATERETURN) {
            javax.swing.SwingUtilities.invokeLater(run);
        } else {
            javax.swing.Timer timer = new javax.swing.Timer(DELAY, null);
            java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
                javax.swing.Timer timer;
                Runnable run;

                java.awt.event.ActionListener init(javax.swing.Timer t, Runnable r) {
                    this.timer = t;
                    this.run = r;
                    return this;
                }

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    this.timer.stop();
                    javax.swing.SwingUtilities.invokeLater(run);
                }
            }.init(timer, run);
            timer.addActionListener(l);
            timer.start();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ProgDebugger.class);

}
