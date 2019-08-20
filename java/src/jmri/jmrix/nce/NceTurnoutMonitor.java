package jmri.jmrix.nce;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls NCE Command Station for turnout discrepancies
 * <p>
 * This implementation reads the NCE Command Station (CS) memory that stores the
 * state of all accessories thrown by cabs or through the com port using the new
 * binary switch command. The accessory states are stored in 256 byte array
 * starting at address 0xEC00.
 * <p>
 * byte 0, bit 0 = ACCY 1, bit 1 = ACCY 2 byte 1, bit 0 = ACCY 9, bit 1 = ACCY
 * 10
 * <p>
 * byte 255, bit 0 = ACCY 2041, bit 3 = ACCY 2044 (last valid addr)
 * <p>
 * ACCY bit = 0 turnout thrown, 1 = turnout closed
 * <p>
 * Block reads (16 bytes) of the NCE CS memory are performed to minimize impact
 * to the NCE CS. Data from the CS is then compared to the JMRI turnout
 * (accessory) state and if a discrepancy is discovered, the JMRI turnout state
 * is modified to match the CS.
 *
 * @author Daniel Boudreau (C) 2007
 */
public class NceTurnoutMonitor implements NceListener, java.beans.PropertyChangeListener {

    // scope constants
    public static final int CS_ACCY_MEMORY = 0xEC00; // Address of start of CS accessory memory
    private static final int NUM_BLOCK = 16; // maximum number of memory blocks
    private static final int BLOCK_LEN = 16; // number of bytes in a block
    private static final int REPLY_LEN = BLOCK_LEN; // number of bytes read
    private static final int NCE_ACCY_THROWN = 0; // NCE internal accessory "REV"
    private static final int NCE_ACCY_CLOSED = 1; // NCE internal accessory "NORM"
    static final int POLL_TIME = 200; // Poll NCE memory every 200 msec plus xmt time (~70 msec)

    // object state
    private int currentBlock; // used as state in scan over active blocks
    private int numTurnouts = 0; // number of NT turnouts known by NceTurnoutMonitor
    private int numActiveBlocks = 0;
    private boolean feedbackChange = false; // true if feedback for a turnout has changed

    // cached work fields
    boolean[] newTurnouts = new boolean[NUM_BLOCK]; // used to sync poll turnout memory
    boolean[] activeBlock = new boolean[NUM_BLOCK]; // When true there are active turnouts in the memory block
    boolean[] validBlock = new boolean[NUM_BLOCK]; // When true received block from CS
    byte[] csAccMemCopy = new byte[NUM_BLOCK * BLOCK_LEN]; // Copy of NCE CS accessory memory
    byte[] dataBuffer = new byte[NUM_BLOCK * BLOCK_LEN]; // place to store reply messages

    private boolean recData = false; // when true, valid receive data

    Thread nceTurnoutMonitorThread;
    boolean turnoutUpdateValid = true; // keep the thread running
    private boolean sentWarnMessage = false; // used to report about early 2007 EPROM problem

    // debug final
    private NceTrafficController tc = null;

    public NceTurnoutMonitor(NceTrafficController t) {
        super();
        this.tc = t;
    }

    private long lastPollTime = 0;

    public NceMessage pollMessage() {

        if (tc.getCommandOptions() < NceTrafficController.OPTION_2006) {
            return null; //Only 2007 CS EPROMs support polling
        }
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            return null; //Can't poll USB!
        }
        if (NceTurnout.getNumNtTurnouts() == 0) {
            return null; //No work!
        }
        long currentTime = java.util.Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastPollTime < 2 * POLL_TIME) {
            return null;
        } else {
            lastPollTime = currentTime;
        }

        // User can change a turnout's feedback to MONITORING, therefore we need to rescan
        // also see if the number of turnouts now differs from the last scan
        if (feedbackChange || numTurnouts != NceTurnout.getNumNtTurnouts()) {
            feedbackChange = false;
            numTurnouts = NceTurnout.getNumNtTurnouts();

            // Determine what turnouts have been defined and what blocks have active turnouts
            for (int block = 0; block < NUM_BLOCK; block++) {

                newTurnouts[block] = true; // Block may be active, but new turnouts may have been loaded
                if (activeBlock[block] == false) { // no need to scan once known to be active

                    for (int i = 0; i < 128; i++) { // Check 128 turnouts per block
                        int NTnum = 1 + i + (block * 128);
                        Turnout mControlTurnout = tc.getAdapterMemo().getTurnoutManager()
                                .getBySystemName(tc.getAdapterMemo().getSystemPrefix() + "T" + NTnum);
                        if (mControlTurnout != null) {
                            // remove listener in case we're already listening
                            mControlTurnout.removePropertyChangeListener(this);

                            if (mControlTurnout.getFeedbackMode() == Turnout.MONITORING) {
                                activeBlock[block] = true; // turnout found, block is active forever
                                numActiveBlocks++;
                                break; // don't check rest of block
                            } else {
                                // turnout feedback isn't monitoring, but listen in case it changes
                                mControlTurnout.addPropertyChangeListener(this);
                                log.trace("add turnout to listener NT{} Feed back mode: {}", NTnum,
                                        mControlTurnout.getFeedbackMode());
                            }
                        }
                    }
                }

            }
        }

        // See if there's any poll messages needed
        if (numActiveBlocks <= 0) {
            return null; // to avoid immediate infinite loop
        }

        // Set up a separate thread to notify state changes in turnouts
        // This protects pollMessage (xmt) and reply threads if there's lockup!
        if (nceTurnoutMonitorThread == null) {
            nceTurnoutMonitorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    turnoutUpdate();
                }
            });
            nceTurnoutMonitorThread.setName("NCE Turnout Monitor");
            nceTurnoutMonitorThread.setPriority(Thread.MIN_PRIORITY);
            nceTurnoutMonitorThread.start();
        }

        // now try to build a poll message if there are any defined turnouts to scan
        while (true) { // will break out when next block to poll is found
            currentBlock++;
            if (currentBlock >= NUM_BLOCK) {
                currentBlock = 0;
            }

            if (activeBlock[currentBlock]) {
                log.trace("found turnouts block " + currentBlock);

                // Read NCE CS memory
                int nceAccAddress = CS_ACCY_MEMORY + currentBlock * BLOCK_LEN;
                byte[] bl = NceBinaryCommand.accMemoryRead(nceAccAddress);
                NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_LEN);
                return m;
            }
        }
    }

    @Override
    public void message(NceMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("unexpected message");
        }
    }

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY") // notify not naked, command station is shared state
    @Override
    public void reply(NceReply r) {
        if (r.getNumDataElements() == REPLY_LEN) {

            log.trace("memory poll reply received for memory block {}: {}", currentBlock, r);
            // Copy receive data into buffer and process later
            for (int i = 0; i < REPLY_LEN; i++) {
                dataBuffer[i + currentBlock * BLOCK_LEN] = (byte) r.getElement(i);
            }
            validBlock[currentBlock] = true;
            recData = true;
            //wake up turnout monitor thread
            synchronized (this) {
                notify();
            }
        } else {
            log.warn("wrong number of read bytes for memory poll");
        }
    }

    // Thread to process turnout changes, protects receive and xmt threads
    // there are two loops, one to update turnout CommandedState
    // and the second to update turnout KnownState
    private void turnoutUpdate() {
        while (turnoutUpdateValid) {
            // if nothing to do, sleep
            if (!recData) {
                synchronized (this) {
                    try {
                        wait(POLL_TIME * 5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                }
                // process rcv buffer and update turnouts
            } else {
                recData = false;
                // scan all valid replys from CS
                for (int block = 0; block < NUM_BLOCK; block++) {
                    if (validBlock[block]) {
                        // Compare NCE CS memory to local copy, change commanded state if
                        // necessary 128 turnouts checked per NCE CS memory read (block)
                        for (int byteIndex = 0; byteIndex < REPLY_LEN; byteIndex++) {
                            // CS memory byte
                            byte recMemByte = dataBuffer[byteIndex + block * BLOCK_LEN];
                            if (recMemByte != csAccMemCopy[byteIndex + block * BLOCK_LEN] ||
                                    newTurnouts[block] == true) {

                                // search this byte for active turnouts
                                for (int i = 0; i < 8; i++) {
                                    int NTnum = 1 + i + byteIndex * 8 + (block * 128);

                                    // Nasty bug in March 2007 EPROM, accessory
                                    // bit 3 is shared by two accessories and 7
                                    // MSB isn't used and the bit map is skewed
                                    // by one bit, ie accy num 2 is in bit 0,
                                    // should have been in bit 1.
                                    if (NceConnectionStatus.isNceEpromMarch2007()) {
                                        // bit 3 is shared by two accessories!!!!
                                        if (i == 3) {
                                            monitorActionCommanded(NTnum - 3,
                                                    recMemByte, i);
                                        }

                                        NTnum++; // skew fix
                                        if (i == 7) {
                                            break; // bit 7 is not used!!!
                                        }
                                    }
                                    monitorActionCommanded(NTnum, recMemByte, i);
                                }
                            }
                        }
                        // this wait is used to add some animation to the panel displays
                        // it does not slow down the rate that this thread can process
                        // turnout changes, it only delays the response by the POLL_TIME
                        synchronized (this) {
                            try {
                                wait(POLL_TIME);
                            } catch (InterruptedException e) {
                            }
                        }
                        // now process again but for turnout KnownState
                        for (int byteIndex = 0; byteIndex < REPLY_LEN; byteIndex++) {
                            // CS memory byte
                            byte recMemByte = dataBuffer[byteIndex + block * BLOCK_LEN];
                            if (recMemByte != csAccMemCopy[byteIndex + block * BLOCK_LEN] ||
                                    newTurnouts[block] == true) {

                                // load copy into local memory
                                csAccMemCopy[byteIndex + block * BLOCK_LEN] = recMemByte;

                                // search this byte for active turnouts
                                for (int i = 0; i < 8; i++) {
                                    int NTnum = 1 + i + byteIndex * 8 + (block * 128);

                                    // Nasty bug in March 2007 EPROM, accessory
                                    // bit 3 is shared by two accessories and 7
                                    // MSB isn't used and the bit map is skewed
                                    // by one bit, ie accy num 2 is in bit 0,
                                    // should have been in bit 1.
                                    if (NceConnectionStatus.isNceEpromMarch2007()) {
                                        if (!sentWarnMessage) {
                                            log.warn(
                                                    "The installed NCE Command Station EPROM has problems when using turnout MONITORING feedback");
                                            sentWarnMessage = true;
                                        }
                                        // bit 3 is shared by two accessories!!!!
                                        if (i == 3) {
                                            monitorActionKnown(NTnum - 3,
                                                    recMemByte, i);
                                        }

                                        NTnum++; // skew fix
                                        if (i == 7) {
                                            break; // bit 7 is not used!!!
                                        }
                                    }
                                    monitorActionKnown(NTnum, recMemByte, i);
                                }
                            }
                        }
                        newTurnouts[block] = false;
                    }
                }
            }
        }
    }

    // update turnout's CommandedState if necessary
    private void monitorActionCommanded(int NTnum, int recMemByte, int bit) {

        NceTurnout rControlTurnout = (NceTurnout) tc.getAdapterMemo().getTurnoutManager()
                .getBySystemName(tc.getAdapterMemo().getSystemPrefix() + "T" + NTnum);
        if (rControlTurnout == null) {
            log.debug("Nce turnout number: {} system prefix: {} doesn't exist", NTnum,
                    tc.getAdapterMemo().getSystemPrefix());
            return;
        }

        int tCommandedState = rControlTurnout.getCommandedState();

        // don't update commanded state if turnout locked unless the turnout state is unknown
        if (rControlTurnout.getLocked(Turnout.CABLOCKOUT) && tCommandedState != Turnout.UNKNOWN) {
            return;
        }

        int nceAccyThrown = NCE_ACCY_THROWN;
        int nceAccyClosed = NCE_ACCY_CLOSED;
        if (rControlTurnout.getInverted()) {
            nceAccyThrown = NCE_ACCY_CLOSED;
            nceAccyClosed = NCE_ACCY_THROWN;
        }

        log.trace("turnout exists NT{} state: {} Feed back mode: {}", NTnum, tCommandedState,
                rControlTurnout.getFeedbackMode());

        // Show the byte read from NCE CS
        log.trace("memory byte: " + Integer.toHexString(recMemByte & 0xFF));

        // test for closed or thrown, normally 0 = closed, 1 = thrown
        int nceAccState = (recMemByte >> bit) & 0x01;
        if (nceAccState == nceAccyThrown && tCommandedState != Turnout.THROWN) {

            log.debug("turnout discrepancy, NT{} CommandedState is now THROWN", NTnum);

            // change JMRI's knowledge of the turnout state to match observed
            rControlTurnout.setCommandedStateFromCS(Turnout.THROWN);
        }

        if (nceAccState == nceAccyClosed && tCommandedState != Turnout.CLOSED) {

            log.debug("turnout discrepancy, NT{} CommandedState is now CLOSED", NTnum);

            // change JMRI's knowledge of the turnout state to match observed
            rControlTurnout.setCommandedStateFromCS(Turnout.CLOSED);
        }
    }

    // update turnout's KnownState if necessary
    private void monitorActionKnown(int NTnum, int recMemByte, int bit) {

        NceTurnout rControlTurnout = (NceTurnout) tc.getAdapterMemo().getTurnoutManager()
                .getBySystemName(tc.getAdapterMemo().getSystemPrefix() + "T" + NTnum);

        if (rControlTurnout == null) {
            return;
        }

        int tKnownState = rControlTurnout.getKnownState();
        int tCommandedState = rControlTurnout.getCommandedState();

        int nceAccyThrown = NCE_ACCY_THROWN;
        int nceAccyClosed = NCE_ACCY_CLOSED;
        if (rControlTurnout.getInverted()) {
            nceAccyThrown = NCE_ACCY_CLOSED;
            nceAccyClosed = NCE_ACCY_THROWN;
        }

        log.trace("turnout exists NT{} state: {} Feed back mode: {}", NTnum, tKnownState,
                rControlTurnout.getFeedbackMode());

        // Show the byte read from NCE CS
        log.trace("memory byte: {}", Integer.toHexString(recMemByte & 0xFF));

        // test for closed or thrown, normally 0 = closed, 1 = thrown
        int nceAccState = (recMemByte >> bit) & 0x01;
        if (nceAccState == nceAccyThrown && tKnownState != Turnout.THROWN) {

            if (rControlTurnout.getLocked(Turnout.CABLOCKOUT) && tCommandedState == Turnout.CLOSED) {

                log.debug("Turnout NT{} is locked, will negate THROW turnout command from layout", NTnum);
                rControlTurnout.forwardCommandChangeToLayout(Turnout.CLOSED);

                if (rControlTurnout.getReportLocked()) {
                    log.info("Turnout NT{} is locked, JMRI has canceled THROW turnout command from cab", NTnum);
                }

            } else {

                log.debug("turnout discrepancy, NT{} KnownState is now THROWN", NTnum);
                // change JMRI's knowledge of the turnout state to match observed
                rControlTurnout.setKnownStateFromCS(Turnout.THROWN);
            }
        }

        if (nceAccState == nceAccyClosed && tKnownState != Turnout.CLOSED) {

            if (rControlTurnout.getLocked(Turnout.CABLOCKOUT) && tCommandedState == Turnout.THROWN) {

                log.debug("Turnout NT{} is locked, will negate CLOSE turnout command from layout", NTnum);
                rControlTurnout.forwardCommandChangeToLayout(Turnout.THROWN);

                if (rControlTurnout.getReportLocked()) {
                    log.info("Turnout NT{} is locked, JMRI has canceled CLOSE turnout command from cab", NTnum);
                }

            } else {

                log.debug("turnout discrepancy, NT" + NTnum + " KnownState is now CLOSED");
                // change JMRI's knowledge of the turnout state to match observed
                rControlTurnout.setKnownStateFromCS(Turnout.CLOSED);
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("feedbackchange")) {
            if (((Integer) e.getNewValue()) == Turnout.MONITORING) {
                feedbackChange = true;
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceTurnoutMonitor.class);

}
